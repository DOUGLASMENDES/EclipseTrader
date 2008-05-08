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

package org.eclipsetrader.ui.internal.markets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ConnectorsPage extends PropertyPage {
    ComboViewer liveFeed;
    private List<Object> connectors = new ArrayList<Object>();

	public ConnectorsPage() {
		setTitle("Connectors");
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		initializeDialogUnits(content);

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
            				defaultConnector != null ? defaultConnector.getName() :  "None",
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

		if (getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				IFeedConnector connector = market.getLiveFeedConnector();
				if (connector != null) {
					if (!connectors.contains(connector))
						connectors.add(connector);
					liveFeed.setSelection(new StructuredSelection(connector));
				}
			}
		}

		return content;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
	    return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
		if (isControlCreated() && getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				Object s = ((IStructuredSelection) liveFeed.getSelection()).getFirstElement();
				market.setLiveFeedConnector(s instanceof IFeedConnector ? (IFeedConnector) s : null);
			}
		}
	    return super.performOk();
    }

    protected IFeedService getFeedService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(IFeedService.class.getName());
    		IFeedService service = (IFeedService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading feed service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }
}
