/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.views.WatchListColumn;
import org.eclipsetrader.core.internal.views.WatchListView;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IWatchListColumn;

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
    	WatchListView resource = (WatchListView) getElement().getAdapter(WatchListView.class);
		providers.setSelectedColumns(resource.getColumns());

		super.performDefaults();
    }

    protected void applyChanges() {
    	WatchListView resource = (WatchListView) getElement().getAdapter(WatchListView.class);
		if (resource != null) {
			List<IWatchListColumn> c = new ArrayList<IWatchListColumn>();
			for (IColumn column : providers.getSelection()) {
				if (column instanceof IWatchListColumn)
					c.add((IWatchListColumn) column);
				else
					c.add(new WatchListColumn(column));
			}
			resource.setColumns(c.toArray(new IWatchListColumn[c.size()]));
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
    	if (getControl() != null)
    		applyChanges();
	    return super.performOk();
    }
}
