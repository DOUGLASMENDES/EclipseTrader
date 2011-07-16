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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.borsaitalia.internal.Activator;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MarketsPage extends WizardPage {

    private CheckboxTableViewer markets;

    public MarketsPage() {
        super("markets", Messages.MarketsPage_Name, null); //$NON-NLS-1$
        setDescription(Messages.MarketsPage_Description);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        setControl(content);
        initializeDialogUnits(content);

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.MarketsPage_Markets);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        markets = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        markets.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData) markets.getControl().getLayoutData()).heightHint = markets.getTable().getItemHeight() * 4 + markets.getTable().getBorderWidth() * 2;
        markets.setLabelProvider(new LabelProvider());
        markets.setContentProvider(new ArrayContentProvider());
        markets.setSorter(new ViewerSorter());
        markets.setInput(getMarkets());
    }

    protected IMarket[] getMarkets() {
        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
        if (serviceReference != null) {
            IMarketService marketService = (IMarketService) context.getService(serviceReference);
            return marketService.getMarkets();
        }
        return new IMarket[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }

    public IMarket[] getSelectedMarkets() {
        Object[] checkedElements = markets.getCheckedElements();
        IMarket[] selectedMarkets = new IMarket[checkedElements.length];
        System.arraycopy(checkedElements, 0, selectedMarkets, 0, checkedElements.length);
        return selectedMarkets;
    }
}
