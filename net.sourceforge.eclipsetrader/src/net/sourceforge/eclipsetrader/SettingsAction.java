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
import org.eclipse.ui.actions.RetargetAction;

public class SettingsAction extends RetargetAction
{

    public SettingsAction(IWorkbenchWindow window)
    {
        super("settings", "Settings...");
        window.getPartService().addPartListener(this);
        setActionDefinitionId("org.eclipse.ui.edit.settings"); //$NON-NLS-1$
    }
}
