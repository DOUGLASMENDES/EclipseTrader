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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchListColumn;

public class ColumnsPage extends WizardPage {
    private ColumnsViewer providers;
    private IColumn[] defaultColumns;

    public ColumnsPage() {
        super("columns", "Columns", null);
        setDescription("Select the columns to display");
    }

    public void setDefaultColumns(IColumn[] defaultColumns) {
        this.defaultColumns = defaultColumns;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        setControl(content);

        initializeDialogUnits(content);

        CoreActivator activator = CoreActivator.getDefault();

        providers = new ColumnsViewer(content);
        providers.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        providers.setInput(activator.getDataProviderFactories());
        providers.setSelectedColumns(defaultColumns);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }

    public IWatchListColumn[] getColumns() {
        List<IWatchListColumn> c = new ArrayList<IWatchListColumn>();
        for (IColumn column : providers.getSelection()) {
            if (column instanceof IWatchListColumn)
                c.add((IWatchListColumn) column);
            else
                c.add(new WatchListColumn(column));
        }
        return c.toArray(new IWatchListColumn[c.size()]);
    }
}
