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

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.simulation.SimulationRunner;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RunSimulationHandler extends AbstractHandler {

    private IWorkbenchSite site;

    public RunSimulationHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        site = HandlerUtil.getActiveSite(event);

        if (selection != null && !selection.isEmpty()) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object target = iter.next();
                if (target instanceof IAdaptable) {
                    target = ((IAdaptable) target).getAdapter(IScriptStrategy.class);
                }
                if (target instanceof IScriptStrategy) {
                    IScriptStrategy strategy = (IScriptStrategy) target;

                    SimulationParametersDialog dlg = new SimulationParametersDialog(site.getShell());
                    if (dlg.open() == Dialog.OK) {
                        Date begin = dlg.getBeginDate();
                        Date end = dlg.getEndDate();
                        scheduleJob(strategy, begin, end);
                    }
                }
            }
        }
        return null;
    }

    private void scheduleJob(final IScriptStrategy strategy, final Date begin, final Date end) {
        String title = NLS.bind("{0} Simulation", new Object[] {
            strategy.getName()
        });

        Job job = new Job(title) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                BundleContext bundleContext = UIActivator.getDefault().getBundle().getBundleContext();
                ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);

                IRepositoryService repositoryService = bundleContext.getService(serviceReference);
                try {
                    final SimulationRunner runner = new SimulationRunner(repositoryService, strategy, begin, end);
                    runner.runWithProgress(monitor);

                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                ReportViewPart viewPart = (ReportViewPart) site.getPage().showView(ReportViewPart.VIEW_ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
                                viewPart.setReport(runner.getReport());
                            } catch (PartInitException e) {
                                Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error opening report view", e); //$NON-NLS-1$
                                UIActivator.log(status);
                            }
                        }
                    });
                } catch (Exception e) {
                    Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, "Error running simulation", e);
                    return status;
                } finally {
                    bundleContext.ungetService(serviceReference);
                }

                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }
}
