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

package org.eclipsetrader.ui.internal.securities.wizards;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;

public class SecurityWizard extends Wizard implements INewWizard {

    private Image image;
    private NamePage namePage;
    private IdentifierPage identifierPage;
    private MarketsPage marketsPage;

    public SecurityWizard() {
        ImageDescriptor descriptor = ImageDescriptor.createFromURL(UIActivator.getDefault().getBundle().getResource("icons/wizban/newfile_wiz.gif"));
        image = descriptor.createImage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
     */
    @Override
    public String getWindowTitle() {
        return "New Common Stock";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#getDefaultPageImage()
     */
    @Override
    public Image getDefaultPageImage() {
        return image;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        addPage(namePage = new NamePage());
        addPage(identifierPage = new IdentifierPage());
        addPage(marketsPage = new MarketsPage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final IRepository repository = namePage.getRepository();
        final Stock resource = new Stock(namePage.getSecurityName(), identifierPage.getFeedIdentifier(), namePage.getCurrency());
        final IMarket[] markets = marketsPage.getSelectedMarkets();

        final IRepositoryService service = UIActivator.getDefault().getRepositoryService();
        service.runInService(new IRepositoryRunnable() {

            @Override
            public IStatus run(IProgressMonitor monitor) throws Exception {
                service.moveAdaptable(new IAdaptable[] {
                    resource
                }, repository);
                for (int i = 0; i < markets.length; i++) {
                    markets[i].addMembers(new ISecurity[] {
                        resource
                    });
                }
                return Status.OK_STATUS;
            }
        }, null);

        return true;
    }
}
