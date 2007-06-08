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

package net.sourceforge.eclipsetrader.core.ui.internal;

import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.views.SecuritiesView;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CopyAction extends Action
{
    private SecuritiesView view;

    public CopyAction(SecuritiesView view)
    {
        this.view = view;
        setText(Messages.CopyAction_Text);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        Security[] securities = view.getSelection();
        Transfer[] transfer = new Transfer[securities.length];
        for (int i = 0; i < securities.length; i++)
            transfer[i] = SecurityTransfer.getInstance();
        
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(securities, transfer);
        clipboard.dispose();
    }
}
