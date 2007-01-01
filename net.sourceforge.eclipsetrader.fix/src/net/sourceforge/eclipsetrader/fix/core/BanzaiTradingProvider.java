/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on Banzai example application
 * Copyright (c) quickfixengine.org  All rights reserved. 
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.fix.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderRoute;
import net.sourceforge.eclipsetrader.core.db.OrderSide;
import net.sourceforge.eclipsetrader.core.db.OrderStatus;
import net.sourceforge.eclipsetrader.core.db.OrderType;
import net.sourceforge.eclipsetrader.core.db.OrderValidity;
import net.sourceforge.eclipsetrader.core.db.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.examples.banzai.TwoWayMap;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.BusinessRejectReason;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.CxlType;
import quickfix.field.DeliverToCompID;
import quickfix.field.ExecID;
import quickfix.field.HandlInst;
import quickfix.field.LastShares;
import quickfix.field.LocateReqd;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.SenderCompID;
import quickfix.field.SessionRejectReason;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

public class BanzaiTradingProvider implements Application, ITradingProvider, IExecutableExtensionFactory
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.fix";
    private static BanzaiTradingProvider instance;
    String name = "";
    boolean isAvailable = true;
    boolean isMissingField;
    MessageFactory messageFactory;
    Initiator initiator;
    Map sessionMap = new HashMap();
    Map ordersMap = new HashMap();
    HashMap execIDs = new HashMap();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    static private TwoWayMap sideMap = new TwoWayMap();
    static private TwoWayMap typeMap = new TwoWayMap();
    static private TwoWayMap tifMap = new TwoWayMap();
    private Log log = LogFactory.getLog(getClass());

    public BanzaiTradingProvider()
    {
        if (instance == null)
            instance = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    public synchronized Object create() throws CoreException
    {
        if (instance == null)
            instance = this;
        return instance;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getSides()
     */
    public List getSides()
    {
        OrderSide[] items = {
            OrderSide.BUY,
            OrderSide.SELL
        };
        return Arrays.asList(items);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getTypes()
     */
    public List getTypes()
    {
        OrderType[] items = {
            OrderType.LIMIT
        };
        return Arrays.asList(items);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getValidity()
     */
    public List getValidity()
    {
        OrderValidity[] items = {
            OrderValidity.DAY,
            OrderValidity.GOOD_TILL_CANCEL,
            OrderValidity.GOOD_TILL_DATE
        };
        return Arrays.asList(items);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getRoutes()
     */
    public List getRoutes()
    {
        List items = new ArrayList();
        for (Iterator iter = sessionMap.keySet().iterator(); iter.hasNext(); )
        {
            String key = (String)iter.next();
            items.add(new OrderRoute(key, key));
        }
        return items;
    }

    /* (non-Javadoc)
     * @see quickfix.Application#onCreate(quickfix.SessionID)
     */
    public void onCreate(SessionID sessionID)
    {
    }

    /* (non-Javadoc)
     * @see quickfix.Application#onLogon(quickfix.SessionID)
     */
    public void onLogon(SessionID sessionID)
    {
        sessionMap.put(sessionID.toString(), sessionID);
        log.info("onLogon: " + sessionID.toString());
    }

    /* (non-Javadoc)
     * @see quickfix.Application#onLogout(quickfix.SessionID)
     */
    public void onLogout(SessionID sessionID)
    {
        sessionMap.remove(sessionID.toString());
        log.info("onLogout: " + sessionID.toString());
    }

    /* (non-Javadoc)
     * @see quickfix.Application#toAdmin(quickfix.Message, quickfix.SessionID)
     */
    public void toAdmin(Message arg0, SessionID arg1)
    {
    }

    /* (non-Javadoc)
     * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
     */
    public void toApp(Message arg0, SessionID arg1) throws DoNotSend
    {
    }

    /* (non-Javadoc)
     * @see quickfix.Application#fromAdmin(quickfix.Message, quickfix.SessionID)
     */
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon
    {
    }

    /* (non-Javadoc)
     * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
     */
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType
    {
        try
        {
            MsgType msgType = new MsgType();
            if (isAvailable)
            {
                if (isMissingField)
                {
                    // For OpenFIX certification testing
                    sendBusinessReject(message, BusinessRejectReason.CONDITIONALLY_REQUIRED_FIELD_MISSING, "Conditionally required field missing");
                }
                else if (message.getHeader().isSetField(DeliverToCompID.FIELD))
                {
                    // This is here to support OpenFIX certification
                    sendSessionReject(message, SessionRejectReason.COMPID_PROBLEM);
                }
                else if (message.getHeader().getField(msgType).valueEquals("8"))
                    executionReport(message, sessionID);
                else if (message.getHeader().getField(msgType).valueEquals("9"))
                    cancelReject(message, sessionID);
                else
                    sendBusinessReject(message, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE, "Unsupported Message Type");
            }
            else
                sendBusinessReject(message, BusinessRejectReason.APPLICATION_NOT_AVAILABLE, "Application not available");
        }
        catch (Exception e) {
            log.error(e);
        }
    }

    private void sendSessionReject(Message message, int rejectReason) throws FieldNotFound, SessionNotFound
    {
        Message reply = createMessage(message, MsgType.REJECT);
        reverseRoute(message, reply);
        String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        reply.setInt(SessionRejectReason.FIELD, rejectReason);
        Session.sendToTarget(reply);
    }

    private void sendBusinessReject(Message message, int rejectReason, String rejectText) throws FieldNotFound, SessionNotFound
    {
        Message reply = createMessage(message, MsgType.BUSINESS_MESSAGE_REJECT);
        reverseRoute(message, reply);
        String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        reply.setInt(BusinessRejectReason.FIELD, rejectReason);
        reply.setString(Text.FIELD, rejectText);
        Session.sendToTarget(reply);
    }

    private Message createMessage(Message message, String msgType) throws FieldNotFound
    {
        return messageFactory.create(message.getHeader().getString(BeginString.FIELD), msgType);
    }

    private void reverseRoute(Message message, Message reply) throws FieldNotFound
    {
        reply.getHeader().setString(SenderCompID.FIELD, message.getHeader().getString(TargetCompID.FIELD));
        reply.getHeader().setString(TargetCompID.FIELD, message.getHeader().getString(SenderCompID.FIELD));
    }

    private void executionReport(Message message, SessionID sessionID) throws FieldNotFound
    {
        ExecID execID = (ExecID) message.getField(new ExecID());
        if (alreadyProcessed(execID, sessionID))
            return;

        String id = message.getField(new ClOrdID()).getValue();
        Order order = (Order) ordersMap.get(id);
        if (order == null)
            return;

        LastShares lastShares = new LastShares(0);

        try {
            OrderQty orderQty = (OrderQty) message.getField(new OrderQty());
            order.setQuantity((int) orderQty.getValue());
        }
        catch (FieldNotFound e) {
        }

        try {
            Price price = (Price) message.getField(new Price());
            order.setPrice(price.getValue());
        }
        catch (FieldNotFound e) {
        }
        
        try {
            message.getField(lastShares);
        }
        catch (FieldNotFound e) {
        }

        if (lastShares.getValue() > 0)
        {
            order.setFilledQuantity((int) message.getField(new CumQty()).getValue());
            order.setAveragePrice(message.getField(new AvgPx()).getValue());
        }

        OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());
        if (ordStatus.valueEquals(OrdStatus.REJECTED))
            order.setStatus(OrderStatus.REJECTED);
        else if (ordStatus.valueEquals(OrdStatus.CANCELED) || ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY))
            order.setStatus(OrderStatus.CANCELED);
        else
        {
            if (order.getFilledQuantity() == order.getQuantity())
                order.setStatus(OrderStatus.FILLED);
            else
                order.setStatus(OrderStatus.PARTIAL);
        }

        CorePlugin.getRepository().save(order);
        
        if (OrderStatus.FILLED.equals(order.getStatus()) && order.getAccount() != null)
        {
            Account account = order.getAccount();

            Transaction transaction = null;
            for (Iterator iter = account.getTransactions().iterator(); iter.hasNext(); )
            {
                Transaction t = (Transaction)iter.next();
                if (order.getOrderId().equals(t.getParams().get("orderId")))
                {
                    transaction = t;
                    break;
                }
            }
            if (transaction == null)
            {
                transaction = new Transaction();
                transaction.setSecurity(order.getSecurity());
                transaction.getParams().put("orderId", order.getOrderId());
            }
            transaction.setDate(order.getDate());
            if (OrderSide.SELL.equals(order.getSide()) || OrderSide.SELLSHORT.equals(order.getSide()))
                transaction.setQuantity(- order.getFilledQuantity());
            else
                transaction.setQuantity(order.getFilledQuantity());
            transaction.setPrice(order.getAveragePrice());

            if (!account.getTransactions().contains(transaction))
                account.getTransactions().add(transaction);
            
            CorePlugin.getRepository().save(account);
        }
    }

    private void cancelReject(Message message, SessionID sessionID) throws FieldNotFound
    {
        String id = message.getField(new ClOrdID()).getValue();
        Order order = (Order) ordersMap.get(id);
        if (order == null)
            return;

        String originalId = (String) order.getParams().get("originalId");
        if (originalId != null)
            order = (Order) ordersMap.get(originalId);
    }

    private boolean alreadyProcessed(ExecID execID, SessionID sessionID)
    {
        HashSet set = (HashSet) execIDs.get(sessionID);
        if (set == null)
        {
            set = new HashSet();
            set.add(execID);
            execIDs.put(sessionID, set);
            return false;
        }
        else
        {
            if (set.contains(execID))
                return true;
            set.add(execID);
            return false;
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendNew(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendNew(Order order)
    {
        if (sessionMap.size() != 0)
        {
            order.setPluginId(PLUGIN_ID);
            order.setProvider(this);
            order.setOrderId(df.format(Calendar.getInstance().getTime()).substring(2));
            ordersMap.put(order.getOrderId(), order);

            String beginString = ((SessionID)sessionMap.get(order.getExchange().getId())).getBeginString();
            if (beginString.equals("FIX.4.0"))
                send40(order);
            else if (beginString.equals("FIX.4.1"))
                send41(order);
            else if (beginString.equals("FIX.4.2"))
                send42(order);

            order.setStatus(OrderStatus.NEW);
        }
        else
            order.setStatus(OrderStatus.REJECTED);

        CorePlugin.getRepository().save(order);
    }

    protected void send40(Order order)
    {
        quickfix.fix40.NewOrderSingle newOrderSingle = new quickfix.fix40.NewOrderSingle(
                new ClOrdID(order.getOrderId()), 
                new HandlInst('1'), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()), 
                new OrderQty(order.getQuantity()), 
                typeToFIXType(order.getType()));

        send(populateOrder(order, newOrderSingle), (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    protected void send41(Order order)
    {
        quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                new ClOrdID(order.getOrderId()), 
                new HandlInst('1'), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()), 
                typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        send(populateOrder(order, newOrderSingle), (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    protected void send42(Order order)
    {
        quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(order.getOrderId()), 
                new HandlInst('1'), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()), 
                new TransactTime(), 
                typeToFIXType(order.getType()));
        newOrderSingle.set(new OrderQty(order.getQuantity()));

        send(populateOrder(order, newOrderSingle), (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendCancelRequest(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendCancelRequest(Order order)
    {
        SessionID sessionId = (SessionID)sessionMap.get(order.getExchange().getId());
        if (sessionId != null)
        {
            String beginString = sessionId.getBeginString();
            if (beginString.equals("FIX.4.0"))
                cancel40(order);
            else if (beginString.equals("FIX.4.1"))
                cancel41(order);
            else if (beginString.equals("FIX.4.2"))
                cancel42(order);
        }
        else
        {
            order.setStatus(OrderStatus.CANCELED);
            CorePlugin.getRepository().save(order);
        }
    }

    public void cancel40(Order order)
    {
        String id = df.format(Calendar.getInstance().getTime()).substring(2);
        quickfix.fix40.OrderCancelRequest message = new quickfix.fix40.OrderCancelRequest(
                new OrigClOrdID(order.getOrderId()), 
                new ClOrdID(id), 
                new CxlType('F'), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()), 
                new OrderQty(order.getQuantity()));

        send(message, (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    public void cancel41(Order order)
    {
        String id = df.format(Calendar.getInstance().getTime()).substring(2);
        quickfix.fix41.OrderCancelRequest message = new quickfix.fix41.OrderCancelRequest(
                new OrigClOrdID(order.getOrderId()), 
                new ClOrdID(id), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()));
        message.setField(new OrderQty(order.getQuantity()));

        send(message, (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    public void cancel42(Order order)
    {
        String id = df.format(Calendar.getInstance().getTime()).substring(2);
        quickfix.fix42.OrderCancelRequest message = new quickfix.fix42.OrderCancelRequest(
                new OrigClOrdID(order.getOrderId()), 
                new ClOrdID(id), 
                new Symbol(order.getSecurity().getCode()), 
                sideToFIXSide(order.getSide()), 
                new TransactTime());
        message.setField(new OrderQty(order.getQuantity()));

        send(message, (SessionID)sessionMap.get(order.getExchange().getId()));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendReplaceRequest(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendReplaceRequest(Order order)
    {
    }

    public quickfix.Message populateOrder(Order order, quickfix.Message newOrderSingle)
    {
        OrderType type = order.getType();
        if (OrderType.LIMIT.equals(type))
            newOrderSingle.setField(new Price(order.getPrice()));
        else if (OrderType.STOP.equals(type))
            newOrderSingle.setField(new StopPx(order.getStopPrice()));
        else if (OrderType.STOPLIMIT.equals(type))
        {
            newOrderSingle.setField(new Price(order.getPrice()));
            newOrderSingle.setField(new StopPx(order.getStopPrice()));
        }

        if (OrderSide.SELLSHORT.equals(order.getSide()))
            newOrderSingle.setField(new LocateReqd(false));

        newOrderSingle.setField(tifToFIXTif(order.getValidity()));

        return newOrderSingle;
    }

    private void send(quickfix.Message message, SessionID sessionID)
    {
        try {
            Session.sendToTarget(message, sessionID);
        }
        catch (SessionNotFound e) {
            System.out.println(e);
        }
    }

    protected Side sideToFIXSide(OrderSide side)
    {
        return (Side) sideMap.getFirst(side);
    }

    public OrdType typeToFIXType(OrderType type)
    {
        return (OrdType) typeMap.getFirst(type);
    }

    public TimeInForce tifToFIXTif(OrderValidity tif)
    {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));
        sideMap.put(OrderSide.SELLSHORT, new Side(Side.SELL_SHORT));

        typeMap.put(OrderType.MARKET, new OrdType(OrdType.MARKET));
        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));
        typeMap.put(OrderType.STOP, new OrdType(OrdType.STOP));
        typeMap.put(OrderType.STOPLIMIT, new OrdType(OrdType.STOP_LIMIT));

        tifMap.put(OrderValidity.DAY, new TimeInForce(TimeInForce.DAY));
        tifMap.put(OrderValidity.IMMEDIATE_OR_CANCEL, new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL));
        tifMap.put(OrderValidity.AT_OPENING, new TimeInForce(TimeInForce.AT_THE_OPENING));
        tifMap.put(OrderValidity.GOOD_TILL_CANCEL, new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));

    }
}
