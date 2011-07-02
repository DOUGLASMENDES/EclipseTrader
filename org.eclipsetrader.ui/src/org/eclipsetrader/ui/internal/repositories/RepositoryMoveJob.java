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

package org.eclipsetrader.ui.internal.repositories;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;

public class RepositoryMoveJob extends Job {

    private IAdaptable[] objects;
    private IRepositoryService service;
    private IRepository destinationRepository;

    public RepositoryMoveJob(IRepositoryService service, IAdaptable[] objects, IRepository destinationRepository) {
        super("Repository Move");

        this.service = service;
        this.objects = objects;
        this.destinationRepository = destinationRepository;

        setUser(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(getName(), objects.length * 10);

        for (int i = 0; i < objects.length; i++) {
            final IAdaptable adaptable = objects[i];
            monitor.subTask(adaptable.toString());
            service.runInService(new IRepositoryRunnable() {

                @Override
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    service.moveAdaptable(new IAdaptable[] {
                        adaptable
                    }, destinationRepository);
                    return Status.OK_STATUS;
                }
            }, new SubProgressMonitor(monitor, 10));
            monitor.worked(10);
        }

        monitor.done();

        return Status.OK_STATUS;
    }
}
