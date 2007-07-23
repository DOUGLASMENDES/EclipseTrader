/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;

import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Implements the security tree comparator that sorts the elements alphabetically,
 * with the groups as the topmost elements.
 */
public class InstrumentsViewerComparator extends ViewerComparator {

	public InstrumentsViewerComparator() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
    	if (element instanceof SecurityGroup)
    		return 0;
    	if (element instanceof Security)
    		return 1;
	    return super.category(element);
    }
}
