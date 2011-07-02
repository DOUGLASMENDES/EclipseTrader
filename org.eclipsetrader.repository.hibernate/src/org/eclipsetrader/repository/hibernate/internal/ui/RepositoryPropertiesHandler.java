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

package org.eclipsetrader.repository.hibernate.internal.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;

@SuppressWarnings("restriction")
public class RepositoryPropertiesHandler extends AbstractHandler {

    private boolean handled;

    public RepositoryPropertiesHandler() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
     */
    @Override
    public void setEnabled(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object var = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
            if (var instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) var;
                if (selection != null && !selection.isEmpty()) {
                    Object target = selection.getFirstElement();
                    if (target instanceof IAdaptable) {
                        target = ((IAdaptable) target).getAdapter(RepositoryDefinition.class);
                    }
                    setHandled(target instanceof RepositoryDefinition);
                    setBaseEnabled(target instanceof RepositoryDefinition);
                }
                return;
            }
        }
        setHandled(false);
    }

    protected void setHandled(boolean handled) {
        if (this.handled == handled) {
            return;
        }
        this.handled = handled;
        fireHandlerChanged(new HandlerEvent(this, false, true));
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isHandled()
     */
    @Override
    public boolean isHandled() {
        return handled;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        IWorkbenchSite site = HandlerUtil.getActiveSite(event);

        if (selection != null && !selection.isEmpty()) {
            Object target = selection.getFirstElement();
            if (target instanceof IAdaptable) {
                target = ((IAdaptable) target).getAdapter(RepositoryDefinition.class);
            }
            if (target instanceof RepositoryDefinition) {
                openPropertiesDialog(site.getShell(), getWrappedElement(target));
            }
        }

        return null;
    }

    protected void openPropertiesDialog(final Shell shell, final IAdaptable adaptableElement) {
        PreferenceManager pageManager = new PreferenceManager();
        pageManager.addToRoot(new PreferenceNode("general", new RepositoryProperties()));

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
                RepositoryDefinition repository = (RepositoryDefinition) adaptableElement.getAdapter(RepositoryDefinition.class);
                newShell.setText("Properties for " + repository.getLabel());
            }
        };
        dlg.setHelpAvailable(false);
        if (dlg.open() == Window.OK && !shell.isDisposed()) {
            Activator.saveRepositoryDefinitions();

            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (MessageDialog.openQuestion(shell, "EclipseTrader", "The workbench must be restarted for the changes to take effect.\r\nRestart the workbench now ?")) {
                        PlatformUI.getWorkbench().restart();
                    }
                }
            });
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
