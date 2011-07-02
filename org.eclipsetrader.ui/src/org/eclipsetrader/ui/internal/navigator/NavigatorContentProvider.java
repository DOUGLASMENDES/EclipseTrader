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

package org.eclipsetrader.ui.internal.navigator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.ViewEvent;

public class NavigatorContentProvider implements ITreeContentProvider {

    private TreeViewer viewer;

    private IViewChangeListener viewChangeListener = new IViewChangeListener() {

        @Override
        public void viewChanged(final ViewEvent event) {
            if (!viewer.getControl().isDisposed()) {
                try {
                    viewer.getControl().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!viewer.getControl().isDisposed()) {
                                viewer.refresh();
                            }
                        }
                    });
                } catch (SWTException e) {
                    if (e.code != SWT.ERROR_DEVICE_DISPOSED) {
                        throw e;
                    }
                }
            }
        }
    };

    public NavigatorContentProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (oldInput != null) {
            NavigatorView view = (NavigatorView) oldInput;
            for (IStructuredContentProvider contentProvider : view.getContentProviders()) {
                contentProvider.inputChanged(viewer, oldInput, newInput);
            }
            view.removeViewChangeListener(viewChangeListener);
        }

        this.viewer = (TreeViewer) viewer;

        if (newInput != null) {
            NavigatorView view = (NavigatorView) newInput;
            for (IStructuredContentProvider contentProvider : view.getContentProviders()) {
                contentProvider.inputChanged(viewer, oldInput, newInput);
            }
            view.addViewChangeListener(viewChangeListener);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return ((IView) inputElement).getItems();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IViewItem) {
            return ((IViewItem) element).getItemCount() != 0;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IViewItem) {
            return ((IViewItem) parentElement).getItems();
        }
        return new Object[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof IViewItem) {
            return ((IViewItem) element).getParent();
        }
        return null;
    }
}
