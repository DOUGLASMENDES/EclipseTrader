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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.editors.ScriptEditor;

/**
 * Default handler to open script objects.
 *
 * @since 1.0
 */
public class ScriptOpenHandler extends AbstractHandler {

    public ScriptOpenHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        IWorkbenchSite site = HandlerUtil.getActiveSite(event);

        if (selection != null && !selection.isEmpty()) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object target = iter.next();
                if (target instanceof IAdaptable) {
                    target = ((IAdaptable) target).getAdapter(IScript.class);
                }
                if (target instanceof IScript) {
                    IScript watchList = (IScript) target;
                    try {
                        IStoreObject storeObject = (IStoreObject) watchList.getAdapter(IStoreObject.class);
                        IDialogSettings dialogSettings = UIActivator.getDefault().getDialogSettingsForView(storeObject.getStore().toURI());
                        site.getPage().showView(ScriptEditor.VIEW_ID, dialogSettings.getName(), IWorkbenchPage.VIEW_ACTIVATE);
                    } catch (PartInitException e) {
                        Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error opening script editor", e); //$NON-NLS-1$
                        UIActivator.getDefault().getLog().log(status);
                    }
                }
            }
        }
        return null;
    }
}
