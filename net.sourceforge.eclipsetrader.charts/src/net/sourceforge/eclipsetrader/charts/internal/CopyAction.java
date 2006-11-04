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
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.transfers.ChartIndicatorTransfer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CopyAction extends Action implements ISelectionChangedListener
{
    private ChartView view;
    private IndicatorSelection selection;

    public CopyAction(ChartView view)
    {
        this.view = view;
        setText(Messages.CopyAction_Copy);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        view.getSite().getSelectionProvider().addSelectionChangedListener(this);
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        ChartIndicator indicator = selection.getIndicator();
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new ChartIndicator[] { indicator }, new Transfer[] { ChartIndicatorTransfer.getInstance() });          
        clipboard.dispose();
        
        view.getViewSite().getActionBars().getGlobalActionHandler("paste").setEnabled(true); //$NON-NLS-1$
        view.getViewSite().getActionBars().getGlobalActionHandler("pasteSpecial").setEnabled(true); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
        selection = null;
        if (event.getSelection() instanceof IndicatorSelection)
            selection = (IndicatorSelection) event.getSelection();
        setEnabled(selection != null);
    }
}
