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

package org.eclipsetrader.ui.internal.ats;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.ui.PriorityPreferenceNode;

@SuppressWarnings("restriction")
public class StrategyPropertiesHandler extends AbstractHandler {

    private GeneralProperties generalProperties;

    public StrategyPropertiesHandler() {
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
            if (target instanceof IAdaptable) {
                target = ((IAdaptable) target).getAdapter(ScriptStrategy.class);
            }
            if (target instanceof ScriptStrategy) {
                openPropertiesDialog(site.getShell(), getWrappedElement(target));
            }
        }

        return null;
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    protected void openPropertiesDialog(Shell shell, final IAdaptable adaptableElement) {
        PropertyPageManager pageManager = new PropertyPageManager();
        pageManager.addToRoot(new PriorityPreferenceNode(Activator.PLUGIN_ID + ".propertypages.general", generalProperties = new GeneralProperties(), -1));
        pageManager.addToRoot(new PriorityPreferenceNode(Activator.PLUGIN_ID + ".propertypages.instruments", new InstrumentsProperties(), 0));
        pageManager.addToRoot(new PriorityPreferenceNode(Activator.PLUGIN_ID + ".propertypages.bars", new BarsProperties(), 0));
        PropertyPageContributorManager.getManager().contribute(pageManager, adaptableElement);

        for (Object nodeObj : pageManager.getElements(PreferenceManager.PRE_ORDER)) {
            IPreferenceNode node = (IPreferenceNode) nodeObj;
            if (node.getPage() instanceof PropertyPage) {
                ((PropertyPage) node.getPage()).setElement(adaptableElement);
            }
        }

        FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(shell, pageManager) {

            @Override
            protected void configureShell(Shell newShell) {
                super.configureShell(newShell);
                ScriptStrategy element = (ScriptStrategy) adaptableElement.getAdapter(ScriptStrategy.class);
                newShell.setText("Properties for " + element.getName());
            }
        };
        dlg.setHelpAvailable(false);
        if (dlg.open() == Window.OK) {
            SaveAdaptableHelper.save(adaptableElement, generalProperties.getRepository());
        }
    }

    /**
     * Wraps the element object to an IAdaptable instance, if necessary.
     *
     * @param element the object to wrap
     * @return an IAdaptable instance that wraps the object
     */
    protected IAdaptable getWrappedElement(final Object element) {
        if (element instanceof IAdaptable) {
            return (IAdaptable) element;
        }

        return new IAdaptable() {

            @Override
            @SuppressWarnings({
                "unchecked", "rawtypes"
            })
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(element.getClass())) {
                    return element;
                }
                return null;
            }
        };
    }
}
