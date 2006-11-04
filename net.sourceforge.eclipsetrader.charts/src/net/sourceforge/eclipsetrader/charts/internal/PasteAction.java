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

import net.sourceforge.eclipsetrader.charts.events.TabSelection;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.transfers.ChartIndicatorTransfer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class PasteAction extends Action implements IPartListener
{
    private ChartView view;

    public PasteAction(ChartView view)
    {
        this.view = view;
        view.getSite().getWorkbenchWindow().getPartService().addPartListener(this);
        setText(Messages.PasteAction_Paste);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        Clipboard clipboard = new Clipboard(Display.getDefault());
        ISelection selection = view.getSite().getSelectionProvider().getSelection();
        if (selection instanceof TabSelection)
        {
            ChartIndicator indicator = (ChartIndicator)clipboard.getContents(ChartIndicatorTransfer.getInstance());
            ((TabSelection) selection).getChartTab().getIndicators().add(indicator);
            CorePlugin.getRepository().save(view.getChart());
        }
        clipboard.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part)
    {
        if (part == view)
        {
            Clipboard clipboard = new Clipboard(Display.getDefault());
            TransferData[] types = clipboard.getAvailableTypes();
            for (int i = 0; i < types.length; i++)
            {
                if (ChartIndicatorTransfer.getInstance().isSupportedType(types[i]))
                {
                    setEnabled(true);
                    clipboard.dispose();
                    return;
                }
            }
            clipboard.dispose();
        }
        
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part)
    {
        if (part == view)
            view.getSite().getWorkbenchWindow().getPartService().removePartListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part)
    {
    }
}
