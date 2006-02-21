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

package net.sourceforge.eclipsetrader.core.internal;

import java.util.Observable;

public class CObservable extends Observable
{

    public CObservable()
    {
    }

    /* (non-Javadoc)
     * @see java.util.Observable#clearChanged()
     */
    public synchronized void clearChanged()
    {
        super.clearChanged();
    }

    /* (non-Javadoc)
     * @see java.util.Observable#setChanged()
     */
    public synchronized void setChanged()
    {
        super.setChanged();
    }
}
