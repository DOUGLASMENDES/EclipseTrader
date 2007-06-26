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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class ExplorerDropAdapter extends ViewerDropAdapter {

	public ExplorerDropAdapter(StructuredViewer viewer) {
		super(viewer);
		setScrollExpandEnabled(true);
		setFeedbackEnabled(false);
		setSelectionFeedbackEnabled(true);
		viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { ExplorerTransfer.getInstance() }, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		SecurityGroup newParentGroup = null;
		if (getCurrentTarget() instanceof SecurityGroup)
			newParentGroup = (SecurityGroup) getCurrentTarget();
		if (getCurrentTarget() instanceof Security)
			newParentGroup = ((Security) getCurrentTarget()).getGroup();
		
		Object[] objects = (Object[]) data;
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof Security)
				((Security) objects[i]).setGroup(newParentGroup);
			if (objects[i] instanceof SecurityGroup)
				((SecurityGroup) objects[i]).setParentGroup(newParentGroup);
		}

		getViewer().refresh();

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return ExplorerTransfer.getInstance().isSupportedType(transferType);
	}
}
