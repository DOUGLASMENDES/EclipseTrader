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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;

public class ViewContentProvider implements ITreeContentProvider {

    private static final int FADE_TIMER = 500;

    private Viewer viewer;
    private Object[] rootItems;

    private IViewChangeListener listener = new IViewChangeListener() {

        @Override
        public void viewChanged(ViewEvent event) {
            final ViewItemDelta[] deltas = event.getDelta();
            if (!viewer.getControl().isDisposed()) {
                try {
                    viewer.getControl().getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (viewer.getControl().isDisposed()) {
                                return;
                            }
                            boolean needRefresh = false;
                            for (ViewItemDelta delta : deltas) {
                                if (delta.getKind() == ViewItemDelta.ADDED || delta.getKind() == ViewItemDelta.REMOVED) {
                                    needRefresh = true;
                                    break;
                                }
                            }
                            for (ViewItemDelta delta : deltas) {
                                if (delta.getKind() == ViewItemDelta.CHANGED) {
                                    IAdaptable[] oldValues = delta.getOldValues();
                                    IAdaptable[] newValues = delta.getNewValues();
                                    IViewItem viewItem = delta.getViewItem();
                                    ISessionData data = (ISessionData) viewItem.getAdapter(ISessionData.class);
                                    if (data != null) {
                                        int[] timers = (int[]) data.getData(ViewItemLabelProvider.K_FADE_LEVELS);
                                        if (timers == null || timers.length != newValues.length) {
                                            timers = new int[newValues.length];
                                            data.setData(ViewItemLabelProvider.K_FADE_LEVELS, timers);
                                        }
                                        if (oldValues.length != newValues.length) {
                                            for (int i = 0; i < timers.length; i++) {
                                                timers[i] = 6;
                                            }
                                            if (!needRefresh) {
                                                ((StructuredViewer) viewer).update(viewItem, null);
                                            }
                                        }
                                        else {
                                            for (int i = 0; i < timers.length; i++) {
                                                if (!(oldValues[i] == newValues[i] || oldValues[i] != null && oldValues[i].equals(newValues[i]))) {
                                                    timers[i] = 6;
                                                }
                                            }
                                            if (!needRefresh) {
                                                ((StructuredViewer) viewer).update(viewItem, null);
                                            }
                                        }
                                    }
                                }
                            }
                            if (needRefresh) {
                                viewer.refresh();
                            }
                        }
                    });
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    };

    private Runnable fadeUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            if (!viewer.getControl().isDisposed()) {
                viewer.getControl().setRedraw(false);
                try {
                    for (Object item : rootItems) {
                        IViewItem viewItem = (IViewItem) item;
                        ISessionData data = (ISessionData) viewItem.getAdapter(ISessionData.class);
                        if (data != null) {
                            int[] timers = (int[]) data.getData(ViewItemLabelProvider.K_FADE_LEVELS);
                            if (timers != null) {
                                boolean needUpdate = false;
                                for (int i = 0; i < timers.length; i++) {
                                    if (timers[i] > 0) {
                                        timers[i]--;
                                        needUpdate = true;
                                    }
                                }
                                if (needUpdate) {
                                    ((StructuredViewer) viewer).update(item, null);
                                }
                            }
                        }
                    }
                } finally {
                    viewer.getControl().setRedraw(true);
                }
                viewer.getControl().getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);
            }
        }
    };

    public ViewContentProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IView) {
            rootItems = ((IView) inputElement).getItems();
        }
        else {
            rootItems = new Object[0];
        }
        return rootItems;
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
        this.viewer = viewer;

        if (oldInput instanceof IView) {
            ((IView) oldInput).removeViewChangeListener(listener);
        }

        if (newInput instanceof IView) {
            ((IView) newInput).addViewChangeListener(listener);
        }

        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().getDisplay().timerExec(FADE_TIMER, fadeUpdateRunnable);
        }
    }

    protected String[] getUpdatedProperties(IViewItem viewItem, IAdaptable[] oldValues, IAdaptable[] newValues) {
        ISessionData data = (ISessionData) viewItem.getAdapter(ISessionData.class);
        if (data != null) {
            int[] timers = (int[]) data.getData(ViewItemLabelProvider.K_FADE_LEVELS);
            if (timers == null || timers.length != newValues.length) {
                timers = new int[newValues.length];
                data.setData(ViewItemLabelProvider.K_FADE_LEVELS, timers);
            }
            if (oldValues.length != newValues.length) {
                for (int i = 0; i < timers.length; i++) {
                    timers[i] = 6;
                }
                return null;
            }
            else {
                String[] properties = new String[timers.length];
                for (int i = 0; i < timers.length; i++) {
                    if (!(oldValues[i] == newValues[i] || oldValues[i] != null && oldValues[i].equals(newValues[i]))) {
                        timers[i] = 6;
                        properties[i] = String.valueOf(i);
                    }
                }
                return properties;
            }
        }
        return null;
    }
}
