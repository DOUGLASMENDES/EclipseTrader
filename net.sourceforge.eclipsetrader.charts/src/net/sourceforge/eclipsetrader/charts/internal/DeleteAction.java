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

package net.sourceforge.eclipsetrader.charts.internal;

import net.sourceforge.eclipsetrader.charts.events.IndicatorSelection;
import net.sourceforge.eclipsetrader.charts.events.ObjectSelection;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DeleteAction extends Action implements ISelectionChangedListener
{
    private ISelection selection;

    public DeleteAction(ChartView view)
    {
        view.getSite().getSelectionProvider().addSelectionChangedListener(this);
        setText("&Delete");
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (MessageDialog.openConfirm(window.getShell(), "Delete Object", "Do you really want to delete the selected object ?"))
        {
            ChartTab tab = null;
            if (selection instanceof IndicatorSelection)
            {
                ChartIndicator indicator = ((IndicatorSelection)selection).getIndicator();
                tab = indicator.getParent();
                tab.getIndicators().remove(indicator);
            }
            else if (selection instanceof ObjectSelection)
            {
                ChartObject object = ((ObjectSelection)selection).getObject();
                tab = object.getParent();
                tab.getObjects().remove(object);
            }
            if (tab != null)
                CorePlugin.getRepository().save(tab.getParent().getParent());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
        selection = null;
        if (event.getSelection() instanceof IndicatorSelection || event.getSelection() instanceof ObjectSelection)
            selection = event.getSelection();
        setEnabled(selection != null);
    }
}
