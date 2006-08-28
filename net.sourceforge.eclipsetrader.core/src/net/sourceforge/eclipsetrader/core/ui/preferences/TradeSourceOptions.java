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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.OrderRoute;
import net.sourceforge.eclipsetrader.core.db.feed.TradeSource;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class TradeSourceOptions
{
    protected Combo provider;
    protected Combo exchange;
    protected Text symbol;
    protected Combo account;
    protected Spinner quantity;

    public TradeSourceOptions()
    {
    }

    public Composite createControls(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText("Trading provider");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        provider = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
        provider.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        provider.setVisibleItemCount(10);
        provider.add(""); //$NON-NLS-1$
        provider.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                providerSelection();
                updateEnablement();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Exchange");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        exchange = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
        exchange.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        exchange.setVisibleItemCount(10);
        exchange.add(""); //$NON-NLS-1$
        exchange.setEnabled(false);

        label = new Label(content, SWT.NONE);
        label.setText("Symbol");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        symbol = new Text(content, SWT.BORDER);
        symbol.setLayoutData(new GridData(100, SWT.DEFAULT));

        label = new Label(content, SWT.NONE);
        label.setText("Account");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        account = new Combo(content, SWT.SINGLE | SWT.READ_ONLY);
        account.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        account.setVisibleItemCount(10);
        account.add(""); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText("Quantity");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        quantity = new Spinner(content, SWT.BORDER);
        quantity.setMinimum(1);
        quantity.setMaximum(999999999);
        quantity.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            java.util.List elements = Arrays.asList(extensionPoint.getConfigurationElements());
            Collections.sort(elements, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    if ((arg0 instanceof IConfigurationElement) && (arg1 instanceof IConfigurationElement))
                    {
                        String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
                        String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
                        return s0.compareTo(s1);
                    }
                    return 0;
                }
            });

            for (Iterator iter = elements.iterator(); iter.hasNext(); )
            {
                IConfigurationElement element = (IConfigurationElement)iter.next();
                String id = element.getAttribute("id"); //$NON-NLS-1$
                String name = element.getAttribute("name"); //$NON-NLS-1$
                provider.add(name);
                provider.setData(String.valueOf(provider.getItemCount() - 1), id);
            }
        }
        
        List list = CorePlugin.getRepository().allAccounts();
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
            account.setData(String.valueOf(account.getItemCount() - 1), s.getId());
        }

        return content;
    }
    
    public Composite createControls(Composite parent, TradeSource tradeSource)
    {
        Composite content = createControls(parent);
        
        if (tradeSource != null)
        {
            String[] items = provider.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (provider.getData(String.valueOf(i)) != null && provider.getData(String.valueOf(i)).equals(tradeSource.getTradingProviderId()))
                {
                    provider.select(i);
                    break;
                }
            }
            providerSelection();

            items = exchange.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (exchange.getData(String.valueOf(i)) != null && exchange.getData(String.valueOf(i)).equals(tradeSource.getExchange()))
                {
                    exchange.select(i);
                    break;
                }
            }

            symbol.setText(tradeSource.getSymbol());

            items = account.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (account.getData(String.valueOf(i)) != null && account.getData(String.valueOf(i)).equals(tradeSource.getAccountId()))
                {
                    account.select(i);
                    break;
                }
            }
            
            quantity.setSelection(tradeSource.getQuantity());
        }
        
        updateEnablement();

        return content;
    }

    protected void providerSelection()
    {
        exchange.removeAll();
        exchange.add(""); //$NON-NLS-1$

        String providerId = (String)provider.getData(String.valueOf(provider.getSelectionIndex()));
        if (providerId != null)
        {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.TRADING_PROVIDERS_EXTENSION_POINT);
            if (extensionPoint != null)
            {
                java.util.List plugins = Arrays.asList(extensionPoint.getConfigurationElements());
                for (Iterator iter = plugins.iterator(); iter.hasNext(); )
                {
                    IConfigurationElement element = (IConfigurationElement)iter.next();
                    if (element.getAttribute("id").equals(providerId)) //$NON-NLS-1$
                    {
                        ITradingProvider source = CorePlugin.createTradeSourcePlugin(element.getAttribute("id"));
                        for (Iterator iter2 = source.getRoutes().iterator(); iter2.hasNext(); )
                        {
                            OrderRoute route = (OrderRoute)iter2.next();
                            exchange.add(route.toString());
                            exchange.setData(route.toString(), route);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    void updateEnablement()
    {
        boolean enable = (provider.getData(String.valueOf(provider.getSelectionIndex())) != null);
        exchange.setEnabled(enable && exchange.getItemCount() > 1);
        symbol.setEnabled(enable);
        account.setEnabled(enable);
        quantity.setEnabled(enable);
    }

    public TradeSource getSource()
    {
        if (provider.getSelectionIndex() <= 0)
            return null;
        
        TradeSource newFeed = new TradeSource();
        newFeed.setTradingProviderId((String)provider.getData(String.valueOf(provider.getSelectionIndex())));
        if (exchange.getSelectionIndex() >= 1)
            newFeed.setExchange(((OrderRoute)exchange.getData(exchange.getText())).getId());
        newFeed.setSymbol(symbol.getText());
        if (account.getSelectionIndex() >= 1)
            newFeed.setAccountId((Integer)account.getData(String.valueOf(account.getSelectionIndex())));
        newFeed.setQuantity(quantity.getSelection());
        
        return newFeed;
    }
}
