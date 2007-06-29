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

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Implements the security tree comparator that sorts the elements alphabetically,
 * with the groups as the topmost elements.
 */
public class InstrumentsViewerComparator extends ViewerComparator {

	public InstrumentsViewerComparator() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
    	String s1 = "";
    	String s2 = "";
    	
    	if (e1 instanceof SecurityGroup)
    		s1 = "0" + getText(viewer, e1);
    	if (e1 instanceof Security)
    		s1 = "1" + getText(viewer, e1);
    	
    	if (e2 instanceof SecurityGroup)
    		s2 = "0" + getText(viewer, e2);
    	if (e2 instanceof Security)
    		s2 = "1" + getText(viewer, e2);
	    
    	return s1.compareTo(s2);
    }
    
    protected String getText(Viewer viewer, Object element) {
    	String text = "";
    	
    	if (viewer instanceof ContentViewer) {
    		IBaseLabelProvider labelProvider = ((ContentViewer) viewer).getLabelProvider();
    		if (labelProvider instanceof ILabelProvider)
    			text = ((ILabelProvider) labelProvider).getText(element);
    	}
    	
    	return text;
    }
}
