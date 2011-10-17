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

package org.eclipsetrader.ui.internal.trading;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipsetrader.ui.internal.UIActivator;

public class AlertWizardNode implements IWizardNode {

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private IConfigurationElement element;
    private IWizard wizard;

    public AlertWizardNode(IConfigurationElement element, IWorkbench workbench, IStructuredSelection selection) {
        this.element = element;
        this.workbench = workbench;
        this.selection = selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#dispose()
     */
    @Override
    public void dispose() {
        if (wizard != null) {
            wizard.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
     */
    @Override
    public Point getExtent() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#getWizard()
     */
    @Override
    public IWizard getWizard() {
        try {
            if (wizard == null) {
                wizard = (IWizard) element.createExecutableExtension("class"); //$NON-NLS-1$
                if (wizard instanceof INewWizard) {
                    ((INewWizard) wizard).init(workbench, selection);
                }
            }
        } catch (CoreException e) {
            Status status = new Status(IStatus.WARNING, UIActivator.PLUGIN_ID, 0, Messages.AlertWizardNode_CreateErrorMessage, e);
            UIActivator.log(status);
        }
        return wizard;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
     */
    @Override
    public boolean isContentCreated() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return element.getAttribute("name"); //$NON-NLS-1$
    }
}
