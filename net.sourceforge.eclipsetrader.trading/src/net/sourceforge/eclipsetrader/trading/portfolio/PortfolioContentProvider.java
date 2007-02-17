/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.portfolio;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

public class PortfolioContentProvider implements ITreeContentProvider
{
    StructuredViewer viewer;
    IStructuredViewerListener listener = new IStructuredViewerListener() {

        public void refresh(final Object element)
        {
            try {
                viewer.getControl().getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!viewer.getControl().isDisposed())
                            viewer.refresh(element);
                    }
                });
            } catch(SWTException e) {
                if (e.code != SWT.ERROR_WIDGET_DISPOSED)
                    throw e;
            }
        }

        public void update(final Object element, final String[] properties)
        {
            try {
                viewer.getControl().getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!viewer.getControl().isDisposed())
                            viewer.update(element, properties);
                    }
                });
            } catch(SWTException e) {
                if (e.code != SWT.ERROR_WIDGET_DISPOSED)
                    throw e;
            }
        }
    };

    public PortfolioContentProvider()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        return ((PortfolioInput)inputElement).getElements();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof AccountGroupTreeNode)
            return ((AccountGroupTreeNode)parentElement).getChildren();
        if (parentElement instanceof AccountTreeNode)
            return ((AccountTreeNode)parentElement).getChildren();
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element)
    {
        if (element instanceof AccountGroupTreeNode)
            return ((AccountGroupTreeNode)element).getParent();
        if (element instanceof AccountTreeNode)
            return ((AccountTreeNode)element).getParent();
        if (element instanceof PositionTreeNode)
            return ((PositionTreeNode)element).getParent();
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element)
    {
        if (element instanceof AccountGroupTreeNode)
            return ((AccountGroupTreeNode)element).hasChildren();
        if (element instanceof AccountTreeNode)
            return ((AccountTreeNode)element).hasChildren();
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        this.viewer = (StructuredViewer)viewer;
        
        if (oldInput instanceof PortfolioInput)
            ((PortfolioInput)oldInput).dispose();
        
        if (newInput instanceof PortfolioInput)
            ((PortfolioInput)newInput).setListener(listener);
    }
}
