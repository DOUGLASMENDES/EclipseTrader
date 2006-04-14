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

package net.sourceforge.eclipsetrader.charts.actions;

import net.sourceforge.eclipsetrader.charts.dialogs.IndicatorSettingsDialog;
import net.sourceforge.eclipsetrader.charts.dialogs.ObjectSettingsDialog;
import net.sourceforge.eclipsetrader.charts.events.IndicatorSelection;
import net.sourceforge.eclipsetrader.charts.events.ObjectSelection;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 */
public class EditObjectAction implements IViewActionDelegate
{
    private ISelection selection;

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
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPart part = window.getActivePage().getActivePart();
        
        if (part instanceof ChartView)
        {
            if (selection instanceof IndicatorSelection)
            {
                ChartIndicator indicator = ((IndicatorSelection)selection).getIndicator();
                IndicatorSettingsDialog dlg = new IndicatorSettingsDialog(indicator, window.getShell());
                dlg.open();
            }
            else if (selection instanceof ObjectSelection)
            {
                ChartObject object = ((ObjectSelection)selection).getObject();
                ObjectSettingsDialog dlg = new ObjectSettingsDialog(object, window.getShell());
                dlg.open();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        this.selection = selection;
        if (selection instanceof IndicatorSelection || selection instanceof ObjectSelection)
            action.setEnabled(true);
        else
            action.setEnabled(false);
    }
}
