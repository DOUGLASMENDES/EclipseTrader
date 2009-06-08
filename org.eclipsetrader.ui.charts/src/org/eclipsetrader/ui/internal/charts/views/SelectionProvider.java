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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class SelectionProvider implements ISelectionProvider {
	private ListenerList selectionListeners = new ListenerList(ListenerList.IDENTITY);
	private ISelection selection;

	public SelectionProvider() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    	selectionListeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    	selectionListeners.remove(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
	    return selection;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection newSelection) {
    	if (this.selection != newSelection) {
        	this.selection = newSelection;
    		fireSelectionChangedEvent(new SelectionChangedEvent(this, newSelection));
    	}
    }

    protected void fireSelectionChangedEvent(SelectionChangedEvent event) {
    	Object[] l = selectionListeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
    		try {
    			((ISelectionChangedListener) l[i]).selectionChanged(event);
    		} catch(Throwable e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.SelectionProvider_ExceptionMessage, e);
				ChartsUIActivator.log(status);
    		}
    	}
    }
}
