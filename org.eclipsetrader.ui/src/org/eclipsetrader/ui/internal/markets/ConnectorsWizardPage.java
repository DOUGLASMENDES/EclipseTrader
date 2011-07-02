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

package org.eclipsetrader.ui.internal.markets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ConnectorsWizardPage extends WizardPage {

    ComboViewer liveFeed;
    private List<Object> connectors = new ArrayList<Object>();

    public ConnectorsWizardPage() {
        super("connectors", "Connectors", null);
        setDescription("Select the connectors used by this market");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(new GridLayout(2, false));
        setControl(content);

        initializeDialogUnits(parent);

        Label label = new Label(content, SWT.NONE);
        label.setText("Live Feed");
        label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
        liveFeed = new ComboViewer(content, SWT.READ_ONLY);
        liveFeed.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        liveFeed.getCombo().setVisibleItemCount(15);
        liveFeed.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (!(element instanceof IFeedConnector)) {
                    IFeedConnector defaultConnector = CoreActivator.getDefault().getDefaultConnector();
                    return NLS.bind("- Default ({0}) -", new Object[] {
                        defaultConnector != null ? defaultConnector.getName() : "None",
                    });
                }
                return ((IFeedConnector) element).getName();
            }
        });
        liveFeed.setSorter(new ViewerSorter());
        liveFeed.setContentProvider(new ArrayContentProvider());

        connectors.add(new Object());
        connectors.addAll(Arrays.asList(getFeedService().getConnectors()));
        liveFeed.setInput(connectors.toArray());
        liveFeed.setSelection(new StructuredSelection(connectors.get(0)));

        liveFeed.getControl().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }

    public IFeedConnector getLiveFeedConnector() {
        Object s = ((IStructuredSelection) liveFeed.getSelection()).getFirstElement();
        return s instanceof IFeedConnector ? (IFeedConnector) s : null;
    }

    protected IFeedService getFeedService() {
        try {
            BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
            IFeedService service = (IFeedService) context.getService(serviceReference);
            context.ungetService(serviceReference);
            return service;
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading feed service", e);
            UIActivator.getDefault().getLog().log(status);
        }
        return null;
    }
}
