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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class OrdersColumnsDialog extends Dialog {

    ConfigurationElementsViewer viewer;
    String[] visibleId;

    public OrdersColumnsDialog(Shell parentShell) {
        super(parentShell);
    }

    public void setVisibleId(String[] visibleElements) {
        this.visibleId = visibleElements;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText(Messages.OrdersColumnsDialog_Text);
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        viewer = new ConfigurationElementsViewer(container);
        viewer.setAvailableElements(getAvailableElements());

        List<IConfigurationElement> visible = new ArrayList<IConfigurationElement>();
        for (int i = 0; i < visibleId.length; i++) {
            IConfigurationElement element = getConfigurationElement(visibleId[i]);
            visible.add(element);
        }
        viewer.setSelectedElements(visible.toArray(new IConfigurationElement[visible.size()]));

        return container;
    }

    IConfigurationElement[] getAvailableElements() {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("org.eclipsetrader.ui.viewLabelProviders"); //$NON-NLS-1$

        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if ("viewContribution".equals(configElements[i].getName())) { //$NON-NLS-1$
                return configElements[i].getChildren();
            }
        }

        return new IConfigurationElement[0];
    }

    IConfigurationElement getConfigurationElement(String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("org.eclipsetrader.ui.viewLabelProviders"); //$NON-NLS-1$

        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configElements.length; i++) {
            if ("viewContribution".equals(configElements[i].getName())) { //$NON-NLS-1$
                configElements = configElements[i].getChildren();
                for (int j = 0; j < configElements.length; j++) {
                    String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
                    if (targetID.equals(strID)) {
                        return configElements[j];
                    }
                }
                break;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IConfigurationElement[] elements = viewer.getSelectedElements();

        visibleId = new String[elements.length];
        for (int i = 0; i < elements.length; i++) {
            visibleId[i] = elements[i].getAttribute("id"); //$NON-NLS-1$
        }

        super.okPressed();
    }

    public String[] getVisibleId() {
        return visibleId;
    }
}
