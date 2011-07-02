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

package org.eclipsetrader.ui.internal.handlers;

import java.util.Iterator;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.views.Level2View;

public class Level2OpenHandler extends AbstractHandler {

    public Level2OpenHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        IWorkbenchSite site = HandlerUtil.getActiveSite(event);

        if (selection != null && !selection.isEmpty()) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object target = iter.next();
                if (target instanceof IAdaptable) {
                    target = ((IAdaptable) target).getAdapter(ISecurity.class);
                }
                if (target instanceof ISecurity) {
                    ISecurity security = (ISecurity) target;
                    try {
                        Level2View view = (Level2View) site.getPage().showView(Level2View.VIEW_ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
                        view.setSecurity(security);
                    } catch (PartInitException e) {
                        Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error opening Level II view", e); //$NON-NLS-1$
                        UIActivator.getDefault().getLog().log(status);
                    }
                }
            }
        }

        return null;
    }
}
