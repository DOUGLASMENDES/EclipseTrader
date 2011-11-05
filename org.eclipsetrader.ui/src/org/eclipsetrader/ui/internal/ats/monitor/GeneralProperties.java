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

package org.eclipsetrader.ui.internal.ats.monitor;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.internal.ats.TradingSystem;
import org.eclipsetrader.core.internal.ats.TradingSystemProperties;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class GeneralProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private ComboViewer broker;
    private ComboViewer account;
    private Spinner backfill;
    private Button autostart;

    private final ISelectionChangedListener changeListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            handleBrokerSelectionChange(selection);
        }
    };

    public GeneralProperties() {
        setTitle("General");
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
        label.setText("Broker:");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        broker = new ComboViewer(content, SWT.READ_ONLY);
        broker.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        broker.setContentProvider(new ArrayContentProvider());
        broker.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IBroker) element).getName();
            }
        });
        broker.setSorter(new ViewerSorter());

        label = new Label(content, SWT.NONE);
        label.setText("Account:");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        account = new ComboViewer(content, SWT.READ_ONLY);
        account.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        account.setContentProvider(new ArrayContentProvider());
        account.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((IAccount) element).getDescription();
            }
        });
        account.setSorter(new ViewerSorter());
        account.getControl().setEnabled(false);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        label = new Label(content, SWT.NONE);
        label.setText("Initial Backfill Size");
        backfill = new Spinner(content, SWT.BORDER);
        backfill.setValues(0, 0, 99999, 0, 1, 1);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        autostart = new Button(content, SWT.CHECK);
        autostart.setText("Start Automatically");
        autostart.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

        performDefaults();

        broker.addSelectionChangedListener(changeListener);

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        TradingSystem system = (TradingSystem) getElement().getAdapter(TradingSystem.class);
        TradingSystemProperties properties = system.getProperties();

        BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();
        ServiceReference<ITradingService> serviceReference = bundleContext.getServiceReference(ITradingService.class);
        ITradingService tradingService = bundleContext.getService(serviceReference);

        autostart.setSelection(properties.isAutostart());

        broker.setInput(tradingService.getBrokers());
        if (properties.getBroker() != null) {
            broker.setSelection(new StructuredSelection(properties.getBroker()));
        }
        else {
            broker.setSelection(StructuredSelection.EMPTY);
        }
        handleBrokerSelectionChange((IStructuredSelection) broker.getSelection());

        if (properties.getAccount() != null) {
            account.setSelection(new StructuredSelection(properties.getAccount()));
        }
        else {
            account.setSelection(StructuredSelection.EMPTY);
        }

        backfill.setSelection(properties.getBackfill());

        bundleContext.ungetService(serviceReference);

        super.performDefaults();
    }

    protected void applyChanges() {
        TradingSystem system = (TradingSystem) getElement().getAdapter(TradingSystem.class);
        TradingSystemProperties properties = system.getProperties();

        properties.setAutostart(autostart.getSelection());

        IStructuredSelection selection = (IStructuredSelection) broker.getSelection();
        properties.setBroker((IBroker) selection.getFirstElement());
        selection = (IStructuredSelection) account.getSelection();
        properties.setAccount((IAccount) selection.getFirstElement());

        properties.setBackfill(backfill.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
        if (getErrorMessage() != null) {
            setErrorMessage(null);
        }
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

    private void handleBrokerSelectionChange(IStructuredSelection selection) {
        IBroker connector = (IBroker) selection.getFirstElement();
        if (connector != null) {
            IAccount[] accounts = connector.getAccounts();
            ISelection oldSelection = account.getSelection();
            account.setInput(accounts);
            account.setSelection(oldSelection);
            if (account.getSelection().isEmpty() && accounts.length != 0) {
                account.setSelection(new StructuredSelection(accounts[0]));
            }
            account.getControl().setEnabled(accounts.length > 0);
        }
        else {
            account.setInput(new Object[0]);
            account.getControl().setEnabled(false);
        }
    }
}
