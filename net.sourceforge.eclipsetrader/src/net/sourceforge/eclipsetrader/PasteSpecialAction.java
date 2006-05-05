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

package net.sourceforge.eclipsetrader;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

public class PasteSpecialAction extends LabelRetargetAction
{

    public PasteSpecialAction(IWorkbenchWindow window)
    {
        super("pasteSpecial", "Paste Special");
        window.getPartService().addPartListener(this);
        setActionDefinitionId("org.eclipse.ui.edit.pasteSpecial"); //$NON-NLS-1$
        setImageDescriptor(EclipseTraderPlugin.getImageDescriptor("icons/etool16/paste_special_edit.gif")); //$NON-NLS-1$
        setDisabledImageDescriptor(EclipseTraderPlugin.getImageDescriptor("icons/dtool16/paste_special_edit.gif")); //$NON-NLS-1$
    }
}
