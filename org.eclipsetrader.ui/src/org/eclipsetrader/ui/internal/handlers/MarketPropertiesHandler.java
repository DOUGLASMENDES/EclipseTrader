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

package org.eclipsetrader.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.ui.internal.markets.ConnectorsPage;
import org.eclipsetrader.ui.internal.markets.GeneralPage;
import org.eclipsetrader.ui.internal.markets.HolidaysPage;
import org.eclipsetrader.ui.internal.markets.MembersPage;

@SuppressWarnings("restriction")
public class MarketPropertiesHandler extends AbstractHandler {

	public MarketPropertiesHandler() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);

		if (selection != null && !selection.isEmpty()) {
			Object target = selection.getFirstElement();
			if (target instanceof IAdaptable)
				target = ((IAdaptable) target).getAdapter(Market.class);
			if (target instanceof Market)
				openPropertiesDialog(site.getShell(), getWrappedElement(target));
		}

		return null;
    }

	protected void openPropertiesDialog(Shell shell, final IAdaptable adaptableElement) {
		if (adaptableElement.getAdapter(Market.class) != null) {
    		PreferenceManager pageManager = new PreferenceManager();
    		pageManager.addToRoot(new PreferenceNode("general", new GeneralPage()));
    		pageManager.addToRoot(new PreferenceNode("connectors", new ConnectorsPage()));
    		pageManager.addToRoot(new PreferenceNode("holidays", new HolidaysPage()));
    		pageManager.addToRoot(new PreferenceNode("members", new MembersPage()));

    		for (Object nodeObj : pageManager.getElements(PreferenceManager.PRE_ORDER)) {
    			IPreferenceNode node = (IPreferenceNode) nodeObj;
    			if (node.getPage() instanceof PropertyPage)
    				((PropertyPage) node.getPage()).setElement(adaptableElement);
    		}

    		FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(shell, pageManager) {
                @Override
                protected void configureShell(Shell newShell) {
                    super.configureShell(newShell);
        			Market market = (Market) adaptableElement.getAdapter(Market.class);
                    newShell.setText("Properties for " + market.getName());
                }
    		};
    		dlg.open();
		}
	}

    /**
     * Wraps the element object to an IAdaptable instance, if necessary.
     *
     * @param element the object to wrap
     * @return an IAdaptable instance that wraps the object
     */
    protected IAdaptable getWrappedElement(final Object element) {
		if (element instanceof IAdaptable)
			return (IAdaptable) element;

		return new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(element.getClass()))
            		return element;
                return null;
            }
		};
    }
}
