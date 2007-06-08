/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Cottet - Initial Release
 *     Marco Maccaferri - Additiona settings and layout changes
 */

package net.sourceforge.eclipsetrader.core.ui.preferences;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DataPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Spinner securityHistoryRange;
    private Button deleteCanceledOrders;
    private Spinner canceledOrdersDays;
    private Button deleteFilledOrders;
    private Spinner filledOrdersDays;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        IPreferenceStore preferences = CorePlugin.getDefault().getPreferenceStore(); 

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.DataPreferencesPage_HistoryRange);
        label.setLayoutData(new GridData(200, SWT.DEFAULT));
        securityHistoryRange = new Spinner(content, SWT.BORDER);
        securityHistoryRange.setMinimum(1);
        securityHistoryRange.setMaximum(50);
        securityHistoryRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        securityHistoryRange.setToolTipText(Messages.DataPreferencesPage_HistoryRangeTooltip);
        securityHistoryRange.setSelection(preferences.getInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));
        label = new Label(content, SWT.NONE);
        label.setText(Messages.DataPreferencesPage_Years);
        
        deleteCanceledOrders = new Button(content, SWT.CHECK);
        deleteCanceledOrders.setText(Messages.DataPreferencesPage_DeleteCanceledOrders);
        deleteCanceledOrders.setSelection(preferences.getBoolean(CorePlugin.PREFS_DELETE_CANCELED_ORDERS));
        deleteCanceledOrders.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                canceledOrdersDays.setEnabled(deleteCanceledOrders.getSelection());
            }
        });
        canceledOrdersDays = new Spinner(content, SWT.BORDER);
        canceledOrdersDays.setMinimum(1);
        canceledOrdersDays.setMaximum(365);
        canceledOrdersDays.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        canceledOrdersDays.setSelection(preferences.getInt(CorePlugin.PREFS_DELETE_CANCELED_ORDERS_DAYS));
        canceledOrdersDays.setEnabled(deleteCanceledOrders.getSelection());
        label = new Label(content, SWT.NONE);
        label.setText(Messages.DataPreferencesPage_Days);
        
        deleteFilledOrders = new Button(content, SWT.CHECK);
        deleteFilledOrders.setText(Messages.DataPreferencesPage_DeleteFilledOrders);
        deleteFilledOrders.setSelection(preferences.getBoolean(CorePlugin.PREFS_DELETE_FILLED_ORDERS));
        deleteFilledOrders.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                filledOrdersDays.setEnabled(deleteFilledOrders.getSelection());
            }
        });
        filledOrdersDays = new Spinner(content, SWT.BORDER);
        filledOrdersDays.setMinimum(1);
        filledOrdersDays.setMaximum(365);
        filledOrdersDays.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        filledOrdersDays.setSelection(preferences.getInt(CorePlugin.PREFS_DELETE_FILLED_ORDERS_DAYS));
        filledOrdersDays.setEnabled(deleteFilledOrders.getSelection());
        label = new Label(content, SWT.NONE);
        label.setText(Messages.DataPreferencesPage_Days);
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore preferences = CorePlugin.getDefault().getPreferenceStore(); 
        preferences.setValue(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE, securityHistoryRange.getSelection());
        preferences.setValue(CorePlugin.PREFS_DELETE_CANCELED_ORDERS, deleteCanceledOrders.getSelection());
        preferences.setValue(CorePlugin.PREFS_DELETE_CANCELED_ORDERS_DAYS, canceledOrdersDays.getSelection());
        preferences.setValue(CorePlugin.PREFS_DELETE_FILLED_ORDERS, deleteFilledOrders.getSelection());
        preferences.setValue(CorePlugin.PREFS_DELETE_FILLED_ORDERS_DAYS, filledOrdersDays.getSelection());
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        IPreferenceStore preferences = CorePlugin.getDefault().getPreferenceStore(); 
        
        securityHistoryRange.setSelection(preferences.getDefaultInt(CorePlugin.PREFS_HISTORICAL_PRICE_RANGE));

        deleteCanceledOrders.setSelection(preferences.getDefaultBoolean(CorePlugin.PREFS_DELETE_CANCELED_ORDERS));
        canceledOrdersDays.setSelection(preferences.getDefaultInt(CorePlugin.PREFS_DELETE_CANCELED_ORDERS_DAYS));
        canceledOrdersDays.setEnabled(deleteCanceledOrders.getSelection());
        
        deleteFilledOrders.setSelection(preferences.getDefaultBoolean(CorePlugin.PREFS_DELETE_FILLED_ORDERS));
        filledOrdersDays.setSelection(preferences.getDefaultInt(CorePlugin.PREFS_DELETE_FILLED_ORDERS_DAYS));
        filledOrdersDays.setEnabled(deleteFilledOrders.getSelection());
    }
}
