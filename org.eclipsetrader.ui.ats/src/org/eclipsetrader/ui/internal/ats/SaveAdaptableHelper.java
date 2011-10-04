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

package org.eclipsetrader.ui.internal.ats;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SaveAdaptableHelper {

    public static IStatus save(IAdaptable adaptableElement) {
        return save(adaptableElement, null);
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public static IStatus save(final IAdaptable adaptableElement, final IRepository repository) {
        final AtomicReference<IStatus> result = new AtomicReference<IStatus>();

        BundleContext context = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
        if (serviceReference != null) {
            final IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
            BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

                @Override
                public void run() {
                    IStatus status = service.runInService(new IRepositoryRunnable() {

                        @Override
                        public IStatus run(IProgressMonitor monitor) throws Exception {
                            IStoreObject storeObject = (IStoreObject) adaptableElement.getAdapter(IStoreObject.class);
                            if (repository != null && repository != storeObject.getStore().getRepository()) {
                                service.moveAdaptable(new IAdaptable[] {
                                    adaptableElement
                                }, repository);
                            }
                            else {
                                service.saveAdaptable(new IAdaptable[] {
                                    adaptableElement
                                });
                            }
                            return Status.OK_STATUS;
                        }
                    }, null);
                    result.set(status);
                }
            });
        }

        return result.get();
    }
}
