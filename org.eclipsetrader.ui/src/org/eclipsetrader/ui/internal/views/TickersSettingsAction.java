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

package org.eclipsetrader.ui.internal.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;

public class TickersSettingsAction extends Action implements ISelectionChangedListener {

    private Shell shell;
    private TickersView view;

    public TickersSettingsAction(Shell shell, TickersView view) {
        super("Settings");
        this.shell = shell;
        this.view = view;
        setId("settings");
        setActionDefinitionId("org.eclipse.ui.edit.settings"); //$NON-NLS-1$
    }

    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    @SuppressWarnings("restriction")
    public void run() {
        final IAdaptable adaptableElement = getWrappedElement(view);

        PreferenceManager pageManager = new PreferenceManager();
        pageManager.addToRoot(new PreferenceNode("securities", new SecurityProperties()));

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
                newShell.setText("Settings for " + view.getPartName());
            }
        };
        dlg.setHelpAvailable(false);
        dlg.open();
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
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(element.getClass())) {
                    return element;
                }
                return null;
            }
        };
    }
}
