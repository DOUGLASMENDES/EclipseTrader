/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.wizards;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class WatchlistWizard extends Wizard implements INewWizard
{
    private Watchlist watchlist;
    private GeneralPage generalPage = new GeneralPage();
    private ColumnsPage columnsPage = new ColumnsPage();
    private ItemsPage itemsPage = new ItemsPage();

    public WatchlistWizard()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        setWindowTitle("New Watchlist Wizard");
        addPage(new CommonWizardPage(generalPage));
        addPage(new CommonWizardPage(columnsPage));
        addPage(new CommonWizardPage(itemsPage));
    }

    public Watchlist open()
    {
        WizardDialog dlg = create();
        dlg.open();
        return watchlist;
    }
    
    public WizardDialog create()
    {
        setWindowTitle("New Watchlist Wizard");
        
        addPage(new CommonWizardPage(generalPage));
        addPage(new CommonWizardPage(columnsPage));
        addPage(new CommonWizardPage(itemsPage));

        WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), this);
        dlg.create();
        
        return dlg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        watchlist = new Watchlist();
        
        watchlist.setDescription(generalPage.getText());
        watchlist.setColumns(columnsPage.getColumns());
        watchlist.setItems(itemsPage.getItems());

        CorePlugin.getRepository().save(watchlist);
        
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(WatchlistView.VIEW_ID, String.valueOf(watchlist.getId()), IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            CorePlugin.logException(e);
        }

        return true;
    }
}
