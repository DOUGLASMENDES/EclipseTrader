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

package net.sourceforge.eclipsetrader.core.ui.actions;

import org.eclipse.jface.action.Action;

/**
 * Properties action class to use in pop-up or view's local menus.
 * <p>The action is initially disabled and linked with the <code>org.eclipse.ui.file.properties</code>
 * action definition id. Implementors must override one of the Action's run methods and set
 * the enablement state.</p>
 * 
 * @see org.eclipse.jface.action.Action#run()
 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
 */
public abstract class PropertiesAction extends Action
{

    public PropertiesAction()
    {
        setText("P&roperties");
        setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$
        setEnabled(false);
    }
}
