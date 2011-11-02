/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.brokers.paper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;

public class PaperBroker implements IBroker {

    private final String id;
    private final String name;
    private final IMarketService marketService;
    private final IRepositoryService repositoryService;

    private MarketPricingEnvironment pricingEnvironment;

    private List<OrderMonitor> pendingOrders = new ArrayList<OrderMonitor>();
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    private final Log log = LogFactory.getLog(getClass());

    private IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            for (PricingDelta delta : event.getDelta()) {
                if (delta.getNewValue() instanceof ITrade) {
                    IMarket market = marketService.getMarketForSecurity(event.getSecurity());
                    if (market == null || market.isOpen()) {
                        processTrade(event.getSecurity(), (ITrade) delta.getNewValue());
                    }
                }
            }
        }
    };

    public PaperBroker(String id, String name, IMarketService marketService, IRepositoryService repositoryService) {
        this.id = id;
        this.name = name;
        this.marketService = marketService;
        this.repositoryService = repositoryService;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#connect()
     */
    @Override
    public void connect() {
        if (pricingEnvironment != null) {
            pricingEnvironment.removePricingListener(pricingListener);
            pricingEnvironment.dispose();
        }

        pricingEnvironment = new MarketPricingEnvironment(marketService);

        List<OrderDelta> list = new ArrayList<OrderDelta>();
        for (OrderMonitor monitor : pendingOrders) {
            pricingEnvironment.addSecurity(monitor.getOrder().getSecurity());
            list.add(new OrderDelta(OrderDelta.KIND_ADDED, monitor));
        }
        fireUpdateNotifications(list.toArray(new OrderDelta[list.size()]));

        pricingEnvironment.addPricingListener(pricingListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#disconnect()
     */
    @Override
    public void disconnect() {
        if (pricingEnvironment != null) {
            pricingEnvironment.removePricingListener(pricingListener);
            pricingEnvironment.dispose();
            pricingEnvironment = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#canTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean canTrade(ISecurity security) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getSecurityFromSymbol(java.lang.String)
     */
    @Override
    public ISecurity getSecurityFromSymbol(String symbol) {
        ISecurity security = null;

        if (repositoryService != null) {
            ISecurity[] securities = repositoryService.getSecurities();
            for (int i = 0; i < securities.length; i++) {
                IFeedIdentifier identifier = securities[i].getIdentifier();
                if (identifier != null && symbol.equals(identifier.getSymbol())) {
                    security = securities[i];
                    break;
                }
            }
        }

        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public String getSymbolFromSecurity(ISecurity security) {
        return security.getIdentifier() != null ? security.getIdentifier().getSymbol() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedSides()
     */
    @Override
    public IOrderSide[] getAllowedSides() {
        return new IOrderSide[] {
            IOrderSide.Buy, IOrderSide.Sell,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedTypes()
     */
    @Override
    public IOrderType[] getAllowedTypes() {
        return new IOrderType[] {
            IOrderType.Limit, IOrderType.Market,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedValidity()
     */
    @Override
    public IOrderValidity[] getAllowedValidity() {
        return new IOrderValidity[] {
            IOrderValidity.Day,
        };
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedRoutes()
     */
    @Override
    public IOrderRoute[] getAllowedRoutes() {
        return new IOrderRoute[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getOrders()
     */
    @Override
    public IOrderMonitor[] getOrders() {
        synchronized (pendingOrders) {
            return pendingOrders.toArray(new IOrderMonitor[pendingOrders.size()]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#prepareOrder(org.eclipsetrader.core.trading.IOrder)
     */
    @Override
    public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
        if (order.getAccount() != null && !(order.getAccount() instanceof Account)) {
            throw new BrokerException("Invalid account");
        }

        OrderMonitor monitor = new OrderMonitor(this, order) {

            @Override
            public void cancel() throws BrokerException {
                setStatus(IOrderStatus.Canceled);

                if (log.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder("Order Cancelled:");
                    sb.append(" instrument=" + getOrder().getSecurity().getName());
                    sb.append(", type=" + getOrder().getType());
                    sb.append(", side=" + getOrder().getSide());
                    sb.append(", qty=" + getOrder().getQuantity());
                    if (getOrder().getPrice() != null) {
                        sb.append(", price=" + getOrder().getPrice());
                    }
                    if (getOrder().getReference() != null) {
                        sb.append(", reference=" + getOrder().getReference());
                    }
                    log.info(sb.toString());
                }

                fireUpdateNotifications(new OrderDelta[] {
                    new OrderDelta(OrderDelta.KIND_UPDATED, this),
                });
            }

            @Override
            public void submit() throws BrokerException {
                synchronized (pendingOrders) {
                    pendingOrders.add(this);
                }

                SimpleDateFormat idFormatter = new SimpleDateFormat("yyMMddHHmmssSSS");
                setId(idFormatter.format(new Date()));
                setStatus(IOrderStatus.PendingNew);

                if (log.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder("Order Submitted:");
                    sb.append(" instrument=" + getOrder().getSecurity().getName());
                    sb.append(", type=" + getOrder().getType());
                    sb.append(", side=" + getOrder().getSide());
                    sb.append(", qty=" + getOrder().getQuantity());
                    if (getOrder().getPrice() != null) {
                        sb.append(", price=" + getOrder().getPrice());
                    }
                    if (getOrder().getReference() != null) {
                        sb.append(", reference=" + getOrder().getReference());
                    }
                    log.info(sb.toString());
                }

                fireUpdateNotifications(new OrderDelta[] {
                    new OrderDelta(OrderDelta.KIND_UPDATED, this),
                });
            }
        };

        if (pricingEnvironment != null) {
            pricingEnvironment.addSecurity(order.getSecurity());
        }

        fireUpdateNotifications(new OrderDelta[] {
            new OrderDelta(OrderDelta.KIND_ADDED, monitor),
        });

        return monitor;
    }

    protected void processTrade(ISecurity security, ITrade trade) {
        List<OrderDelta> deltas = new ArrayList<OrderDelta>();

        OrderMonitor[] monitors;
        synchronized (pendingOrders) {
            monitors = pendingOrders.toArray(new OrderMonitor[pendingOrders.size()]);
        }
        for (int i = 0; i < monitors.length; i++) {
            if (monitors[i].getStatus() != IOrderStatus.PendingNew && monitors[i].getStatus() != IOrderStatus.Partial) {
                continue;
            }
            IOrder order = monitors[i].getOrder();
            if (order.getSecurity() == security) {
                if (order.getType() == IOrderType.Market) {
                    fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
                    deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                }
                else if (order.getType() == IOrderType.Limit) {
                    if (order.getSide() == IOrderSide.Buy && trade.getPrice() <= order.getPrice()) {
                        fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
                        deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                    }
                    else if (order.getSide() == IOrderSide.Sell && trade.getPrice() >= order.getPrice()) {
                        fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
                        deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
                    }
                }
            }
        }

        if (deltas.size() != 0) {
            fireUpdateNotifications(deltas.toArray(new OrderDelta[deltas.size()]));
        }
    }

    protected void fillOrder(OrderMonitor monitor, IOrder order, Long size, Double price) {
        double totalPrice = monitor.getFilledQuantity() != null ? monitor.getFilledQuantity() * monitor.getAveragePrice() : 0.0;
        long filledQuantity = monitor.getFilledQuantity() != null ? monitor.getFilledQuantity() : 0L;
        long remainQuantity = order.getQuantity() - filledQuantity;

        long quantity = size != null && size < remainQuantity ? size : remainQuantity;
        filledQuantity += quantity;
        totalPrice += quantity * price;

        monitor.setFilledQuantity(filledQuantity);
        monitor.setAveragePrice(totalPrice / filledQuantity);

        if (quantity != 0) {
            if (order.getSide() == IOrderSide.Buy || order.getSide() == IOrderSide.BuyCover) {
                monitor.addTransaction(new StockTransaction(monitor.getOrder().getSecurity(), quantity, price));
            }
            if (order.getSide() == IOrderSide.Sell || order.getSide() == IOrderSide.SellShort) {
                monitor.addTransaction(new StockTransaction(monitor.getOrder().getSecurity(), -quantity, price));
            }
        }

        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("Order Filled:");
            sb.append(" instrument=" + order.getSecurity().getName());
            sb.append(", type=" + order.getType());
            sb.append(", side=" + order.getSide());
            sb.append(", qty=" + order.getQuantity());
            if (order.getPrice() != null) {
                sb.append(", price=" + order.getPrice());
            }
            sb.append(", fillQty=" + monitor.getFilledQuantity());
            sb.append(", avgPrice=" + monitor.getAveragePrice());
            if (order.getReference() != null) {
                sb.append(", reference=" + order.getReference());
            }
            log.info(sb.toString());
        }

        if (monitor.getFilledQuantity().equals(order.getQuantity())) {
            monitor.setStatus(IOrderStatus.Filled);
            monitor.fireOrderCompletedEvent();

            Account account = (Account) monitor.getOrder().getAccount();
            if (account != null) {
                account.processCompletedOrder(monitor);
            }
        }
        else {
            monitor.setStatus(IOrderStatus.Partial);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void removeOrderChangeListener(IOrderChangeListener listener) {
        listeners.remove(listener);
    }

    protected void fireUpdateNotifications(OrderDelta[] deltas) {
        if (deltas.length != 0) {
            OrderChangeEvent event = new OrderChangeEvent(this, deltas);
            Object[] l = listeners.getListeners();
            for (int i = 0; i < l.length; i++) {
                try {
                    ((IOrderChangeListener) l[i]).orderChanged(event);
                } catch (Throwable e) {
                    Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", e); //$NON-NLS-1$
                    Activator.log(status);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
     */
    @Override
    public IAccount[] getAccounts() {
        return Activator.getDefault().getRepository().getAccounts();
    }

    public void load(File file) throws JAXBException {
        if (!file.exists()) {
            return;
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(OrderMonitor[].class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                Activator.log(status);
                return true;
            }
        });
        JAXBElement<OrderMonitor[]> element = unmarshaller.unmarshal(new StreamSource(file), OrderMonitor[].class);
        if (element != null) {
            Calendar today = Calendar.getInstance();
            Calendar order = Calendar.getInstance();
            for (OrderMonitor monitor : element.getValue()) {
                order.setTime(monitor.getOrder().getDate());
                if (order.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    pendingOrders.add(monitor);
                }
            }
        }
    }

    public void save(File file) throws JAXBException, IOException {
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(OrderMonitor[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                Activator.log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        OrderMonitor[] elements = pendingOrders.toArray(new OrderMonitor[pendingOrders.size()]);
        JAXBElement<OrderMonitor[]> element = new JAXBElement<OrderMonitor[]>(new QName("list"), OrderMonitor[].class, elements);
        marshaller.marshal(element, new FileWriter(file));
    }
}
