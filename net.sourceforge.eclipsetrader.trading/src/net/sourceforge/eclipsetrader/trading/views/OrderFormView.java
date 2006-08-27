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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Order;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
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
    Label total;
    private NumberFormat pf = NumberFormat.getInstance();
    private NumberFormat nf = NumberFormat.getInstance();
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
        validity.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        validity.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                validitySelection();
            }
        });
        
        send = new Button(content, SWT.PUSH);
        send.setText("Send Order");
        send.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        send.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                sendOrder();
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
        
        side.add("Buy");
        side.setData("Buy", new Integer(Order.SIDE_BUY));
        side.add("Sell");
        side.setData("Sell", new Integer(Order.SIDE_SELL));
        side.select(0);
        
        type.add("Limit");
        type.setData("Limit", new Integer(Order.TYPE_LIMIT));
        type.add("Market");
        type.setData("Market", new Integer(Order.TYPE_MARKET));
        type.select(0);
        
        validity.add("Day");
        validity.setData("Day", new Integer(Order.VALID_DAY));
        validity.select(0);
        
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
            order.setSide(((Integer)side.getData(side.getText())).intValue());
            order.setType(((Integer)type.getData(type.getText())).intValue());
            order.setQuantity(quantity.getSelection());
            if (order.getType() != Order.TYPE_MARKET)
                order.setPrice(price.getSelection() / Math.pow(10, price.getDigits()));
            if (exchange.isEnabled())
                order.setExchange((String)exchange.getData(exchange.getText()));
            order.setValidity(((Integer)validity.getData(validity.getText())).intValue());
            
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
            IConfigurationElement[] childs = item.getChildren();
            exchange.removeAll();
            for (int i = 0; i < childs.length; i++)
            {
                if (childs[i].getName().equals("route"))
                {
                    exchange.add(childs[i].getAttribute("name"));
                    exchange.setData(childs[i].getAttribute("name"), childs[i].getAttribute("id"));
                }
            }
            
            exchange.setEnabled(exchange.getItemCount() != 0);
            if (exchange.getItemCount() != 0)
                exchange.select(0);
            
            exchange.getParent().getParent().layout();
            
            String value = item.getAttribute("sellShort");
            updateOption(side, "Sell Short", Order.SIDE_SELLSHORT, value != null && value.equals("true"));

            value = item.getAttribute("immediateOrCancel");
            updateOption(validity, "Imm. or cancel", Order.VALID_IMMEDIATE_OR_CANCEL, "true".equals(value));
            value = item.getAttribute("opening");
            updateOption(validity, "At Opening", Order.VALID_OPENING, "true".equals(value));
            value = item.getAttribute("closing");
            updateOption(validity, "At Closing", Order.VALID_CLOSING, "true".equals(value));
        }
        
        updateTotal();
        updateButtonEnablement();
    }
    
    void updateOption(Combo combo, String label, int value, boolean enabled)
    {
        if (enabled)
        {
            if (combo.indexOf(label) == -1)
            {
                combo.add(label);
                combo.setData(label, new Integer(value));
            }
        }
        else
        {
            String text = combo.getText();
            
            if (combo.indexOf(label) != -1)
                combo.remove(label);
            combo.setData(label, null);
            
            combo.setText(text);
            if (combo.getSelectionIndex() == -1)
                combo.select(0);
        }
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
        Integer value = (Integer)side.getData(String.valueOf(side.getSelectionIndex()));
        if (value == null)
            value = new Integer(-1);
        
        switch(value.intValue())
        {
        }

        updateTotal();
    }
    
    void typeSelection()
    {
        Integer value = (Integer)type.getData(String.valueOf(type.getSelectionIndex()));
        if (value == null)
            value = new Integer(-1);

        switch(value.intValue())
        {
            case Order.TYPE_LIMIT:
                price.setEnabled(true);
                break;
            default:
                price.setEnabled(false);
                break;
        }

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
    }
}
