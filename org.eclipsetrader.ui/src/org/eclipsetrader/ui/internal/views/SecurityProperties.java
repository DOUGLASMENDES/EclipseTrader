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

package org.eclipsetrader.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.ui.internal.UIActivator;

public class SecurityProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private SecuritySelectionControl providers;

    public SecurityProperties() {
        setTitle("Securities");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        initializeDialogUnits(content);

        providers = new SecuritySelectionControl(content);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providers.setInput(UIActivator.getDefault().getRepositoryService().getSecurities());

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        TickersView resource = (TickersView) getElement().getAdapter(TickersView.class);

        TickerViewItem[] columns = resource.getViewItems();
        ISecurity[] selectedColumns = new ISecurity[columns.length];
        for (int i = 0; i < selectedColumns.length; i++) {
            selectedColumns[i] = columns[i].getSecurity();
        }

        providers.setSelectedColumns(selectedColumns);

        super.performDefaults();
    }

    protected void applyChanges() {
        TickersView resource = (TickersView) getElement().getAdapter(TickersView.class);
        if (resource != null) {
            List<TickerViewItem> c = new ArrayList<TickerViewItem>();
            for (ISecurity column : providers.getSelection()) {
                c.add(new TickerViewItem(column));
            }
            resource.setViewItems(c.toArray(new TickerViewItem[c.size()]));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        applyChanges();
        super.performApply();
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
}
