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

package net.sourceforge.eclipsetrader.news.actions;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.news.views.NewsView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ShowSecurityNewsAction implements IViewActionDelegate
{
//    private String defaultText;
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
            try {
                page.showView(NewsView.VIEW_ID, String.valueOf(security.getId()), IWorkbenchPage.VIEW_ACTIVATE);
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
//        if (defaultText == null)
//            defaultText = action.getText();
        
        this.security = null;
        if (selection instanceof SecuritySelection)
        {
            this.security = ((SecuritySelection)selection).getSecurity();
//            action.setText(defaultText + " for " + security.getDescription());
        }
//        else
//            action.setText(defaultText);
        action.setEnabled(this.security != null);
    }
}
