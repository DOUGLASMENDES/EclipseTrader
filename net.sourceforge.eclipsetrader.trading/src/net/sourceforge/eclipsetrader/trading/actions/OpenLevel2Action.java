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

package net.sourceforge.eclipsetrader.trading.actions;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.trading.views.Level2View;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 */
public class OpenLevel2Action implements IViewActionDelegate
{
    private Security security;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        if (security != null)
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            PreferenceStore preferences = new PreferenceStore(Level2View.getPreferenceStoreLocation(security).toOSString());
            try {
                preferences.load();
            } catch(Exception e) {
            }

            // Builds a random secondary id, if a new view needs to be opened
            String secondaryId = preferences.getString("secondaryId");
            if (secondaryId.equals(""))
            {
                String values = "abcdefghijklmnopqrstuvwxyz";
                for (int i = 0; i < 8; i++)
                    secondaryId += values.charAt((int)(Math.random() * values.length()));
            }
            
            try {
                Level2View view = (Level2View)page.showView(Level2View.VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
                view.setSecurity(security);
            } catch (PartInitException e) {
                CorePlugin.logException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        this.security = null;
        if (selection instanceof SecuritySelection)
            this.security = ((SecuritySelection)selection).getSecurity();
        action.setEnabled(this.security != null);
    }
}
