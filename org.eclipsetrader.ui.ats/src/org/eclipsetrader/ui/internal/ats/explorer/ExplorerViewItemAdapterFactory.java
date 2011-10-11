/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.ats.explorer;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.Script;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

public class ExplorerViewItemAdapterFactory implements IAdapterFactory {

    public ExplorerViewItemAdapterFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof ExplorerViewItem) {
            return ((ExplorerViewItem) adaptableObject).getAdapter(adapterType);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return new Class[] {
            ScriptStrategy.class,
            IScriptStrategy.class,
            IStrategy.class,
            Script.class,
            IScript.class,
            Security.class,
            ISecurity.class,
        };
    }
}
