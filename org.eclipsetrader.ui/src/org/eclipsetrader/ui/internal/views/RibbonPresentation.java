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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.views.IColumn;
import org.eclipsetrader.core.views.IViewItem;

public class RibbonPresentation implements IWatchListViewerPresentation {
	private BoxViewer viewer;

	public RibbonPresentation(Composite parent, IDialogSettings dialogSettings) {
		viewer = createViewer(parent);
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#dispose()
     */
    public void dispose() {
    	viewer.getControl().dispose();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#getViewer()
	 */
	public StructuredViewer getViewer() {
		return viewer;
	}

	protected BoxViewer createViewer(Composite parent) {
		BoxViewer viewer = new BoxViewer(parent);
		viewer.setUseHashlookup(true);

		viewer.setLabelProvider(new ViewItemLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
            	IAdaptable[] v1 = ((IViewItem) e1).getValues();
            	IAdaptable[] v2 = ((IViewItem) e2).getValues();
            	return compareValues(v1, v2, 0);
            }
		});

		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.internal.views.IWatchListViewerPresentation#updateColumns(org.eclipsetrader.core.views.IColumn[])
	 */
	public void updateColumns(IColumn[] columns) {
	}

    @SuppressWarnings("unchecked")
    protected int compareValues(IAdaptable[] v1, IAdaptable[] v2, int sortColumn) {
    	if (sortColumn < 0 || sortColumn >= v1.length || sortColumn >= v2.length)
    		return 0;
    	if (v1[sortColumn] == null || v2[sortColumn] == null)
    		return 0;

    	Object o1 = v1[sortColumn].getAdapter(Comparable.class);
    	Object o2 = v2[sortColumn].getAdapter(Comparable.class);
    	if (o1 != null && o2 != null)
    		return ((Comparable) o1).compareTo(o2);

    	o1 = v1[sortColumn].getAdapter(Number.class);
    	o2 = v2[sortColumn].getAdapter(Number.class);
    	if (o1 != null && o2 != null) {
    		if (((Number) o1).doubleValue() < ((Number) o2).doubleValue())
    			return -1;
    		if (((Number) o1).doubleValue() > ((Number) o2).doubleValue())
    			return 1;
    		return 0;
    	}

    	return 0;
    }
}
