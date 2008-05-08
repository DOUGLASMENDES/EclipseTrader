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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.securities.properties.GeneralProperties;
import org.eclipsetrader.ui.internal.securities.properties.IdentifierProperties;
import org.eclipsetrader.ui.internal.securities.properties.MarketsProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class SecurityPropertiesHandler extends AbstractHandler {

	public SecurityPropertiesHandler() {
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
				target = ((IAdaptable) target).getAdapter(Security.class);
			if (target instanceof Security)
				openPropertiesDialog(site.getShell(), getWrappedElement(target));
		}

		return null;
    }

	protected void openPropertiesDialog(Shell shell, final IAdaptable adaptableElement) {
		PreferenceManager pageManager = new PreferenceManager();
		if (adaptableElement.getAdapter(Security.class) != null) {
    		pageManager.addToRoot(new PreferenceNode("general", new GeneralProperties()));
    		pageManager.addToRoot(new PreferenceNode("identifier", new IdentifierProperties()));
    		pageManager.addToRoot(new PreferenceNode("markets", new MarketsProperties()));
		}

		for (Object nodeObj : pageManager.getElements(PreferenceManager.PRE_ORDER)) {
			IPreferenceNode node = (IPreferenceNode) nodeObj;
			if (node.getPage() instanceof PropertyPage)
				((PropertyPage) node.getPage()).setElement(adaptableElement);
		}

		FilteredPreferenceDialog dlg = new FilteredPreferenceDialog(shell, pageManager) {
            @Override
            protected void configureShell(Shell newShell) {
                super.configureShell(newShell);
    			Security security = (Security) adaptableElement.getAdapter(Security.class);
                newShell.setText("Properties for " + security.getName());
            }
		};
		dlg.setHelpAvailable(false);
		if (dlg.open() == PreferenceDialog.OK) {
			BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				final IRepositoryService service = (IRepositoryService) context.getService(serviceReference);
    			service.runInService(new IRepositoryRunnable() {
    	            public IStatus run(IProgressMonitor monitor) throws Exception {
    	            	service.saveAdaptable(new IAdaptable[] { adaptableElement });
    		            return Status.OK_STATUS;
    	            }
    			}, null);
			}
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
