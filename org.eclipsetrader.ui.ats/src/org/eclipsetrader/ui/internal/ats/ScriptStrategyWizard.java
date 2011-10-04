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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ScriptStrategyWizard extends Wizard implements INewWizard {

    private Image image;
    private NamePage namePage;
    private InstrumentsPage instrumentsPage;
    private BarsPage barsPage;
    private IWorkbench workbench;

    public ScriptStrategyWizard() {
        ImageDescriptor descriptor = ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getResource("icons/wizban/newfile_wiz.gif"));
        image = descriptor.createImage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        addPage(namePage = new NamePage());
        addPage(instrumentsPage = new InstrumentsPage());
        addPage(barsPage = new BarsPage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
     */
    @Override
    public String getWindowTitle() {
        return "New Script Strategy";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
     */
    @Override
    public Image getDefaultPageImage() {
        return image;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final IRepository repository = namePage.getRepository();

        final ScriptStrategy resource = new ScriptStrategy(namePage.getScriptName());
        resource.setInstruments(instrumentsPage.getInstruments());
        resource.setBarsTimeSpan(barsPage.getValues());

        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();

        ServiceReference<IRepositoryService> serviceReference = bundleContext.getServiceReference(IRepositoryService.class);
        final IRepositoryService service = bundleContext.getService(serviceReference);

        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.moveAdaptable(new IAdaptable[] {
                    resource
                }, repository);
                return Status.OK_STATUS;
            }
        }, null);

        bundleContext.ungetService(serviceReference);

        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
        try {
            IStoreObject storeObject = (IStoreObject) resource.getAdapter(IStoreObject.class);
            IDialogSettings dialogSettings = Activator.getDefault().getDialogSettingsForView(storeObject.getStore().toURI());

            page.showView(ScriptEditor.VIEW_ID, dialogSettings.getName(), IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error opening script editor", e); //$NON-NLS-1$
            Activator.log(status);
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#dispose()
     */
    @Override
    public void dispose() {
        if (image != null) {
            image.dispose();
        }
        super.dispose();
    }
}
