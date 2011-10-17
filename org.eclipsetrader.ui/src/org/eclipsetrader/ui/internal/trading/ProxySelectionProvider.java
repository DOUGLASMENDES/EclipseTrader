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

package org.eclipsetrader.ui.internal.trading;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class ProxySelectionProvider implements ISelectionProvider, ISelectionChangedListener {

    private ISelectionProvider selectionProvider;
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public ProxySelectionProvider() {
    }

    public void setSelectionProvider(ISelectionProvider selectionProvider) {
        if (this.selectionProvider != null) {
            this.selectionProvider.removeSelectionChangedListener(this);
        }

        this.selectionProvider = selectionProvider;

        if (this.selectionProvider != null) {
            this.selectionProvider.addSelectionChangedListener(this);
            selectionChanged(new SelectionChangedEvent(this.selectionProvider, this.selectionProvider.getSelection()));
        }
    }

    public void dispose() {
        if (this.selectionProvider != null) {
            this.selectionProvider.removeSelectionChangedListener(this);
        }
        listeners.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    @Override
    public ISelection getSelection() {
        return selectionProvider != null ? selectionProvider.getSelection() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((ISelectionChangedListener) l[i]).selectionChanged(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
