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

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.views.SecuritiesView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class DeleteAction extends Action
{
    private SecuritiesView view;

    public DeleteAction(SecuritiesView view)
    {
        this.view = view;
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
        if (MessageDialog.openConfirm(view.getViewSite().getShell(), "Security", "Do you really want to delete the selected securities ?"))
        {
            Security[] securities = view.getSelection();
            for (int i = 0; i < securities.length; i++)
                CorePlugin.getRepository().delete(securities[i]);
        }
    }
}
