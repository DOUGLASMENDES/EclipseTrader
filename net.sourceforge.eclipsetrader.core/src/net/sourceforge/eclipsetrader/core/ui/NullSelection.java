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

package net.sourceforge.eclipsetrader.core.ui;

import org.eclipse.jface.viewers.ISelection;

public class NullSelection implements ISelection
{

    public NullSelection()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty()
    {
        return true;
    }
}
