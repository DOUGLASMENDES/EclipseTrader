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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class WatchListViewContentProvider implements IStructuredContentProvider {
	private static final int FADE_TIMER = 500;

	private TableViewer viewer;
	private WatchListView watchListView;

	private Runnable fadeUpdateRunnable = new Runnable() {
		public void run() {
        	if (!viewer.getControl().isDisposed() && watchListView != null) {
            	viewer.getControl().setRedraw(false);
            	try {
            		Set<String> propertyNames = new HashSet<String>();
        			for (WatchListViewItem viewItem : watchListView.getItems()) {
        				for (WatchListViewColumn viewColumn : watchListView.getColumns()) {
        					Integer timer = viewItem.getUpdateTime(viewColumn.getDataProviderFactory().getId());
        					if (timer != null) {
        						if (timer > 0) {
        							timer--;
        							propertyNames.add(viewColumn.getDataProviderFactory().getId());
        							viewItem.setUpdateTime(viewColumn.getDataProviderFactory().getId(), timer);
        						}
        					}
        				}
    					if (propertyNames.size() != 0) {
    						((StructuredViewer) viewer).update(viewItem, propertyNames.toArray(new String[propertyNames.size()]));
    						propertyNames.clear();
    					}
        			}
            	} finally {
                	viewer.getControl().setRedraw(true);
            	}
       			viewer.getControl().getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);
        	}
		}
	};

	public WatchListViewContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (watchListView != null && inputElement == watchListView)
			return watchListView.getItems();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		this.watchListView = null;

		if (newInput instanceof WatchListView)
			this.watchListView = (WatchListView) newInput;

		if (viewer != null && !viewer.getControl().isDisposed())
			viewer.getControl().getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);
	}
}
