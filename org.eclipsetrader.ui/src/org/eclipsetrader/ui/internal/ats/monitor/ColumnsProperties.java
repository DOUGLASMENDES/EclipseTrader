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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.ui.internal.ats.ViewColumn;
import org.eclipsetrader.ui.internal.views.ColumnsViewer;

public class ColumnsProperties extends PropertyPage implements IWorkbenchPropertyPage {

    private ColumnsViewer providers;

    public ColumnsProperties() {
        setTitle("Columns");
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

        providers = new ColumnsViewer(content);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providers.setInput(CoreActivator.getDefault().getDataProviderFactories());

        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        TradingSystemsViewModel resource = (TradingSystemsViewModel) getElement().getAdapter(TradingSystemsViewModel.class);

        List<ViewColumn> columns = resource.getDataProviders();
        Column[] selectedColumns = new Column[columns.size()];
        for (int i = 0; i < selectedColumns.length; i++) {
            selectedColumns[i] = new Column(columns.get(i).getName(), columns.get(i).getDataProviderFactory());
        }

        providers.setSelectedColumns(selectedColumns);

        super.performDefaults();
    }

    protected void applyChanges() {
        TradingSystemsViewModel resource = (TradingSystemsViewModel) getElement().getAdapter(TradingSystemsViewModel.class);

        List<ViewColumn> c = new ArrayList<ViewColumn>();
        for (IColumn column : providers.getSelection()) {
            c.add(new ViewColumn(column.getName(), column.getDataProviderFactory()));
        }
        resource.setDataProviders(c);
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
