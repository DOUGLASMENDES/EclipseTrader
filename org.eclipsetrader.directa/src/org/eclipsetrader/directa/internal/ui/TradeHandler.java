/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.directa.internal.ui;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.instruments.ISecurity;

public class TradeHandler extends AbstractHandler {

	public TradeHandler() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);

		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> iter = selection.iterator(); iter.hasNext(); ) {
				Object target = iter.next();
				if (target instanceof IAdaptable)
					target = ((IAdaptable) target).getAdapter(ISecurity.class);
				if (target instanceof ISecurity) {
					OrderDialog dlg = new OrderDialog(site.getShell());
					dlg.setSecurity((ISecurity) target);
					dlg.open();
				}
			}
		}

		return null;
    }
}
