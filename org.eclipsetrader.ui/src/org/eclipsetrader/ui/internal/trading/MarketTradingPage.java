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

package org.eclipsetrader.ui.internal.trading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.trading.MarketBroker;
import org.eclipsetrader.core.internal.trading.MarketBrokerAdapterFactory;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class MarketTradingPage extends PropertyPage implements IWorkbenchPropertyPage {

    ComboViewer brokerCombo;

    public MarketTradingPage() {
        setTitle(Messages.MarketTradingPage_Title);
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.MarketTradingPage_BrokerLabel);
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        brokerCombo = new ComboViewer(content, SWT.READ_ONLY);
        brokerCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        brokerCombo.getCombo().setVisibleItemCount(15);
        brokerCombo.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (!(element instanceof IBroker)) {
                    return Messages.MarketTradingPage_None_Element;
                }
                return ((IBroker) element).getName();
            }
        });
        brokerCombo.setSorter(new ViewerSorter() {

            @Override
            public int category(Object element) {
                if (element instanceof IBroker) {
                    return 1;
                }
                return 0;
            }
        });
        brokerCombo.setContentProvider(new ArrayContentProvider());

        List<Object> brokers = new ArrayList<Object>();
        brokers.add(new Object());
        if (getTradingService() != null) {
            brokers.addAll(Arrays.asList(getTradingService().getBrokers()));
        }
        brokerCombo.setInput(brokers.toArray());
        brokerCombo.setSelection(new StructuredSelection(brokers.get(0)));

        IMarket market = (IMarket) getElement().getAdapter(IMarket.class);
        if (market != null) {
            IBroker broker = (IBroker) market.getAdapter(IBroker.class);
            if (broker != null) {
                if (!brokers.contains(broker)) {
                    brokers.add(broker);
                    brokerCombo.setInput(brokers.toArray());
                }
                brokerCombo.setSelection(new StructuredSelection(broker));
            }
        }

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (isControlCreated()) {
            IMarket market = (IMarket) getElement().getAdapter(IMarket.class);
            MarketBrokerAdapterFactory adapter = (MarketBrokerAdapterFactory) AdapterManager.getDefault().getAdapter(market, MarketBrokerAdapterFactory.class);

            Object s = ((IStructuredSelection) brokerCombo.getSelection()).getFirstElement();
            if (s instanceof IBroker) {
                MarketBroker marketBroker = new MarketBroker(market);
                marketBroker.setConnector(s instanceof IBroker ? (IBroker) s : null);
                adapter.addOverride(marketBroker);
            }
            else {
                adapter.clearOverride(market);
            }
        }
        return super.performOk();
    }

    protected IFeedService getFeedService() {
        if (Activator.getDefault() == null) {
            return null;
        }
        try {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
            IFeedService service = (IFeedService) context.getService(serviceReference);
            context.ungetService(serviceReference);
            return service;
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.MarketTradingPage_ErrorReadingFeedService, e);
            Activator.getDefault().getLog().log(status);
        }
        return null;
    }

    protected ITradingService getTradingService() {
        if (Activator.getDefault() == null) {
            return null;
        }
        try {
            BundleContext context = Activator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
            ITradingService service = (ITradingService) context.getService(serviceReference);
            context.ungetService(serviceReference);
            return service;
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, Messages.MarketTradingPage_ErrorReadingTradingService, e);
            Activator.getDefault().getLog().log(status);
        }
        return null;
    }
}
