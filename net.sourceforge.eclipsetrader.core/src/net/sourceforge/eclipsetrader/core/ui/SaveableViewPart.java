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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.part.ViewPart;

public abstract class SaveableViewPart extends ViewPart implements ISaveablePart
{
    boolean dirty = false;

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor)
    {
        if (dirty)
        {
            dirty = false;
            firePropertyChange(PROP_DIRTY);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs()
    {
        if (dirty)
        {
            dirty = false;
            firePropertyChange(PROP_DIRTY);
        }
    }
    
    public void setDirty()
    {
        if (!dirty)
        {
            dirty = true;
            firePropertyChange(PROP_DIRTY);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded()
    {
        return dirty;
    }
}
