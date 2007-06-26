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

import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

public class ExplorerDragAdapter extends DragSourceAdapter {
	private StructuredViewer viewer;

	public ExplorerDragAdapter(StructuredViewer viewer) {
		this.viewer = viewer;
		viewer.addDragSupport(DND.DROP_COPY|DND.DROP_MOVE, new Transfer[] { ExplorerTransfer.getInstance(), SecurityTransfer.getInstance() }, this);
	}

	/* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
    	if (viewer.getSelection().isEmpty())
    		event.doit = false;
    	else {
        	Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
    		event.doit = ExplorerTransfer.getInstance().checkMyType(selection);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
    	Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
    	
    	if (ExplorerTransfer.getInstance().isSupportedType(event.dataType)) {
        	if (ExplorerTransfer.getInstance().checkMyType(selection))
            	event.data = selection;
    	}
    	else if (SecurityTransfer.getInstance().isSupportedType(event.dataType)) {
        	if (SecurityTransfer.getInstance().checkMyType(selection))
            	event.data = selection;
    	}
    }
}
