/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.feed.TradeSource;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class OrderFormView extends ViewPart
{
    Combo security;
    Combo provider;
    Combo exchange;
    Combo side;
    Combo type;
    Spinner quantity;
    Spinner price;
    Button send;
    Combo account;
    Combo validity;
    Text expire;
    Label total;
    static Map sideLabels = new HashMap();
    static Map typeLabels = new HashMap();
    static Map validityLabels = new HashMap();
    static Map statusLabels = new HashMap();
    private NumberFormat pf = NumberFormat.getInstance();
    private NumberFormat nf = NumberFormat.getInstance();
    SimpleDateFormat dateFormat = CorePlugin.getDateFormat();
    SimpleDateFormat dateParse = CorePlugin.getDateParse();
    private Logger logger = Logger.getLogger(getClass());
    DropTargetListener dropTargetListener = new DropTargetListener() {
        public void dragEnter(DropTargetEvent event)
        {
            event.detail = DND.DROP_COPY;
            event.currentDataType = null;
            
            TransferData[] data = event.dataTypes;
            if (event.currentDataType == null)
            {
                for (int i = 0; i < data.length; i++)
                {
                    if (SecurityTransfer.getInstance().isSupportedType(data[i]))
                    {
                        event.currentDataType = data[i];
                        break;
                    }
                }
            }
        }

        public void dragOver(DropTargetEvent event)
        {
            event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
        }

        public void dragOperationChanged(DropTargetEvent event)
        {
        }

        public void dragLeave(DropTargetEvent event)
        {
        }

        public void dropAccept(DropTargetEvent event)
        {
        }

        public void drop(DropTargetEvent event)
        {
            if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType))
            {
                Security[] securities = (Security[]) event.data;
                setSecurity(securities[0]);
            }
        }
    };

    public OrderFormView()
    {
        pf.setGroupingUsed(true);
        pf.setMinimumIntegerDigits(1);
        pf.setMinimumFractionDigits(4);
        pf.setMaximumFractionDigits(4);

        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(11, false));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Account");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        
        account = new Combo(content, SWT.READ_ONLY);
        account.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 10, 1));
        account.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                accountSelection();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Security");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        Composite row = new Composite(content, SWT.NONE);
        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        row.setLayout(gridLayout);
        row.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 10, 1));

        security = new Combo(row, SWT.READ_ONLY);
        security.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        security.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                securitySelection();
            }
        });

        label = new Label(row, SWT.NONE);
        label.setText("Provider");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        provider = new Combo(row, SWT.READ_ONLY);
        provider.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        provider.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                providerSelection();
                validitySelection();
            }
        });

        label = new Label(row, SWT.NONE);
        label.setText("Exchange");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        exchange = new Combo(row, SWT.READ_ONLY);
        exchange.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        label = new Label(content, SWT.NONE);
        label.setText("Side");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        
        side = new Combo(content, SWT.READ_ONLY);
        side.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        side.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                sideSelection();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Type");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        type = new Combo(content, SWT.READ_ONLY);
        type.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        type.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                typeSelection();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Quantity");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        quantity = new Spinner(content, SWT.BORDER);
        quantity.setMinimum(1);
        quantity.setMaximum(999999999);
        quantity.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        quantity.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateTotal();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Price");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        price = new Spinner(content, SWT.BORDER);
        price.setMinimum(0);
        price.setMaximum(999999999);
        price.setDigits(4);
        price.setIncrement(5);
        price.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        price.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateTotal();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Validity");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        validity = new Combo(content, SWT.READ_ONLY);
        validity.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        validity.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                validitySelection();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 6, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Total");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        total = new Label(content, SWT.NONE);
        total.setText("0,00");
        total.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        
        send = new Button(content, SWT.PUSH);
        send.setText("Send Order");
        send.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
        send.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                sendOrder();
            }
        });

        DropTarget target = new DropTarget(content, DND.DROP_COPY);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        target.addDropListener(dropTargetListener);
        
        List list = new ArrayList(CorePlugin.getRepository().allSecurities());
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Security item = (Security)iter.next();
            security.add(item.getDescription());
        }
        security.setData(list);
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            list = Arrays.asList(extensionPoint.getConfigurationElements());
            Collections.sort(list, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    return ((IConfigurationElement)arg0).getAttribute("name").compareTo(((IConfigurationElement)arg1).getAttribute("name"));
                }
            });
            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                IConfigurationElement item = (IConfigurationElement)iter.next();
                provider.add(item.getAttribute("name"));
            }
            provider.setData(list);
        }
        else
            provider.setData(new ArrayList());

        side.select(0);
        
        list = CorePlugin.getRepository().allAccounts();
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Account)arg0).getDescription().compareTo(((Account)arg1).getDescription());
            }
        });
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Account s = (Account)iter.next();
            account.add(s.getDescription());
        }
        account.setData(list);
        
        exchange.setEnabled(exchange.getItemCount() != 0);
        updateButtonEnablement();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose()
    {
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus()
    {
    }
    
    void setSecurity(Security newSecurity)
    {
        security.setText(newSecurity.getDescription());
        securitySelection();
        
        TradeSource source = newSecurity.getTradeSource();
        if (source != null)
        {
            List list = ((List)provider.getData());
            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                IConfigurationElement item = (IConfigurationElement)iter.next();
                if (source.getTradingProviderId().equals(item.getAttribute("id")))
                    provider.setText(item.getAttribute("name"));
            }
            providerSelection();
            
            list = ((List)account.getData());
            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                Account item = (Account)iter.next();
                if (source.getAccountId() != null && source.getAccountId().equals(item.getId()))
                    account.setText(item.getDescription());
            }
            accountSelection();
            
            String[] items = exchange.getItems();
            for (int i = 0; i < items.length; i++)
            {
                String id = (String)exchange.getData(String.valueOf(i));
                if (source.getExchange().equals(id))
                {
                    exchange.select(i);
                    break;
                }
            }
            quantity.setSelection(source.getQuantity());
            updateTotal();
        }
    }

    void sendOrder()
    {
        try {
            IConfigurationElement item = (IConfigurationElement)((List)provider.getData()).get(provider.getSelectionIndex());
            ITradingProvider plugin = (ITradingProvider)item.createExecutableExtension("class"); //$NON-NLS-1$

            Order order = new Order();
            order.setSecurity((Security)((List)security.getData()).get(security.getSelectionIndex()));
            if (account.getSelectionIndex() != -1)
                order.setAccount((Account)((List)account.getData()).get(account.getSelectionIndex()));
            order.setSide((OrderSide)side.getData(side.getText()));
            order.setType((OrderType)type.getData(type.getText()));
            order.setQuantity(quantity.getSelection());
            if (!OrderType.MARKET.equals(order.getType()))
                order.setPrice(price.getSelection() / Math.pow(10, price.getDigits()));
            if (exchange.isEnabled())
                order.setExchange((OrderRoute)exchange.getData(exchange.getText()));
            order.setValidity((OrderValidity)validity.getData(validity.getText()));
            if (expire != null)
                order.setExpire(dateFormat.parse(expire.getText()));
            
            plugin.sendNew(order);
        
        } catch(Exception e) {
            logger.error(e, e);
        }
    }
    
    void providerSelection()
    {
        List list = (List)provider.getData();
        int index = provider.getSelectionIndex();
        if (index >= 0 && index < list.size())
        {
            IConfigurationElement item = (IConfigurationElement)list.get(index);
            
            ITradingProvider source = CorePlugin.createTradeSourcePlugin(item.getAttribute("id"));
            populateValues(side, source.getSides(), sideLabels);
            populateValues(type, source.getTypes(), typeLabels);
            populateValues(validity, source.getValidity(), validityLabels);
            
            exchange.removeAll();
            for (Iterator iter = source.getRoutes().iterator(); iter.hasNext(); )
            {
                OrderRoute route = (OrderRoute)iter.next();
                exchange.add(route.toString());
                exchange.setData(route.toString(), route);
            }
            exchange.setEnabled(exchange.getItemCount() != 0);
            if (exchange.getItemCount() != 0)
                exchange.select(0);

            exchange.getParent().getParent().layout();
        }
        
        updateTotal();
        updateButtonEnablement();
    }
    
    void securitySelection()
    {
        List list = (List)security.getData();
        int index = security.getSelectionIndex();
        if (index >= 0 && index < list.size())
        {
            Security item = (Security)list.get(index);
            if (item.getQuote() != null)
                price.setSelection((int)(item.getQuote().getLast() * Math.pow(10, price.getDigits())));
            else
                price.setSelection(0);
        }
        else
            price.setSelection(0);

        updateTotal();
        updateButtonEnablement();
    }
    
    void sideSelection()
    {
        updateTotal();
    }
    
    void typeSelection()
    {
        OrderType value = (OrderType)type.getData(type.getText());
        price.setEnabled(OrderType.LIMIT.equals(value));
        updateTotal();
    }
    
    void updateButtonEnablement()
    {
        boolean enabled = true;
        
        if (security.getSelectionIndex() == -1)
            enabled = false;
        if (provider.getSelectionIndex() == -1)
            enabled = false;
        if (exchange.isEnabled() && exchange.getSelectionIndex() == -1)
            enabled = false;
        
        send.setEnabled(enabled);
    }
    
    void accountSelection()
    {
        updateTotal();
    }
    
    void updateTotal()
    {
        double value = quantity.getSelection() * (price.getSelection() / Math.pow(10, price.getDigits()));
        total.setText(nf.format(value));
        total.getParent().layout();
    }
    
    void validitySelection()
    {
        OrderValidity v = (OrderValidity)validity.getData(validity.getText());
        if (v.equals(OrderValidity.GOOD_TILL_DATE))
        {
            if (expire == null)
            {
                expire = new Text(validity.getParent(), SWT.BORDER);
                expire.setLayoutData(new GridData(80, SWT.DEFAULT));
                expire.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e)
                    {
                        try {
                            Date d = dateParse.parse(expire.getText());
                            expire.setText(dateFormat.format(d));
                        } catch(Exception e1) {
                        }
                    }
                });
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 30);
                expire.setText(dateFormat.format(c.getTime()));
                expire.moveBelow(validity);

                ((GridData)validity.getLayoutData()).horizontalSpan = 1;
                
                validity.getParent().layout();
            }
        }
        else if (expire != null)
        {
            expire.dispose();
            expire = null;
            ((GridData)validity.getLayoutData()).horizontalSpan = 2;
            validity.getParent().layout();
        }
    }
    
    void populateValues(Combo combo, List list, Map labels)
    {
        int index = combo.getSelectionIndex();
        
        combo.removeAll();
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Object value = iter.next();
            String text = (String)labels.get(value);
            if (text != null)
            {
                combo.add(text);
                combo.setData(text, value);
            }
        }
        
        if (index != -1 && index < combo.getItemCount())
            combo.select(index);
        else
            combo.select(0);
    }

    static {
        sideLabels.put(OrderSide.BUY, "Buy");
        sideLabels.put(OrderSide.SELL, "Sell");
        sideLabels.put(OrderSide.SELLSHORT, "Sell Short");
        sideLabels.put(OrderSide.BUYCOVER, "Buy Cover");

        typeLabels.put(OrderType.LIMIT, "Limit");
        typeLabels.put(OrderType.MARKET, "Market");
        typeLabels.put(OrderType.STOP, "Stop");
        typeLabels.put(OrderType.STOPLIMIT, "Stop Limit");

        validityLabels.put(OrderValidity.DAY, "Day");
        validityLabels.put(OrderValidity.IMMEDIATE_OR_CANCEL, "Imm. or Cancel");
        validityLabels.put(OrderValidity.AT_OPENING, "At Opening");
        validityLabels.put(OrderValidity.AT_CLOSING, "At Closing");
        validityLabels.put(OrderValidity.GOOD_TILL_CANCEL, "Good Till Cancel");
        validityLabels.put(OrderValidity.GOOD_TILL_DATE, "Good Till Date");

        statusLabels.put(OrderStatus.NEW, "New");
        statusLabels.put(OrderStatus.PARTIAL, "Partial");
        statusLabels.put(OrderStatus.FILLED, "Filled");
        statusLabels.put(OrderStatus.CANCELED, "Canceled");
        statusLabels.put(OrderStatus.REJECTED, "Rejected");
        statusLabels.put(OrderStatus.PENDING_CANCEL, "Pending Cancel");
        statusLabels.put(OrderStatus.PENDING_NEW, "Pending New");
    }
}
