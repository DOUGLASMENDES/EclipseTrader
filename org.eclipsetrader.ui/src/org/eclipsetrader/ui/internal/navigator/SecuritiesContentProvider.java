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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipsetrader.core.repositories.IRepositoryChangeListener;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.RepositoryChangeEvent;
import org.eclipsetrader.ui.internal.UIActivator;

public class SecuritiesContentProvider implements IStructuredContentProvider {

    final IRepositoryService repository;
    Viewer viewer;

    IRepositoryChangeListener resourceListener = new IRepositoryChangeListener() {

        @Override
        public void repositoryResourceChanged(RepositoryChangeEvent event) {
            NavigatorView view = (NavigatorView) viewer.getInput();
            view.update();

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

    public SecuritiesContentProvider() {
        repository = getRepositoryService();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return repository.getSecurities();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        repository.removeRepositoryResourceListener(resourceListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (oldInput != null) {
            repository.removeRepositoryResourceListener(resourceListener);
        }

        this.viewer = viewer;

        if (newInput != null) {
            repository.addRepositoryResourceListener(resourceListener);
        }
    }

    protected IRepositoryService getRepositoryService() {
        return UIActivator.getDefault().getRepositoryService();
    }
}
