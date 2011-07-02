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

package org.eclipsetrader.ui.internal.securities.properties;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MarketsProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private Label marketsLabel;
    private CheckboxTableViewer markets;

    public MarketsProperties() {
        setTitle("Markets");
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

        marketsLabel = new Label(content, SWT.NONE);
        marketsLabel.setText("Markets:");
        marketsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        markets = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        markets.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData) markets.getControl().getLayoutData()).heightHint = markets.getTable().getItemHeight() * 4 + markets.getTable().getBorderWidth() * 2;
        markets.setLabelProvider(new LabelProvider());
        markets.setContentProvider(new ArrayContentProvider());
        markets.setSorter(new ViewerSorter());
        markets.setInput(getMarkets());

        markets.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    Object[] elements = markets.getCheckedElements();
                    for (int i = 0; i < elements.length; i++) {
                        if (elements[i] != event.getElement()) {
                            markets.setChecked(elements[i], false);
                        }
                    }
                }
            }
        });

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);

        for (IMarket market : getMarkets()) {
            markets.setChecked(market, market.hasMember(security));
        }

        super.performDefaults();
    }

    protected void applyChanges() {
        ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
        for (IMarket market : getMarkets()) {
            if (markets.getChecked(market)) {
                market.addMembers(new ISecurity[] {
                    security
                });
            }
            else {
                market.removeMembers(new ISecurity[] {
                    security
                });
            }
        }
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
        if (getControl() != null) {
            applyChanges();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();
        super.performApply();
    }

    protected IMarket[] getMarkets() {
        BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
        if (serviceReference != null) {
            IMarketService marketService = (IMarketService) context.getService(serviceReference);
            return marketService.getMarkets();
        }
        return new IMarket[0];
    }

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }
}
