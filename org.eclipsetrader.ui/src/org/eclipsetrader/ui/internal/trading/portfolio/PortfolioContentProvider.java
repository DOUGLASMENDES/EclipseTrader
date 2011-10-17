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

package org.eclipsetrader.ui.internal.trading.portfolio;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.ViewEvent;

public class PortfolioContentProvider implements ITreeContentProvider, IViewChangeListener {

    Display display;
    TreeViewer viewer;
    PortfolioView input;

    public PortfolioContentProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (input != null) {
            input.removeViewChangeListener(this);
            input.dispose();
        }

        this.display = viewer.getControl().getDisplay();
        this.viewer = (TreeViewer) viewer;

        input = (PortfolioView) newInput;
        if (input != null) {
            input.addViewChangeListener(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewChangeListener#viewChanged(org.eclipsetrader.core.views.ViewEvent)
     */
    @Override
    public void viewChanged(ViewEvent event) {
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                if (viewer != null && !viewer.getControl().isDisposed()) {
                    viewer.refresh();
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement == null) {
            return new Object[0];
        }
        return ((IView) inputElement).getItems();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        return ((IViewItem) parentElement).getItems();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return ((IViewItem) element).getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        return ((IViewItem) element).getItemCount() != 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        if (input != null) {
            input.removeViewChangeListener(this);
            input.dispose();
        }
    }
}
