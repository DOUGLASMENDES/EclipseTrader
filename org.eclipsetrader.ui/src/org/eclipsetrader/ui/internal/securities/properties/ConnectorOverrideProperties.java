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

package org.eclipsetrader.ui.internal.securities.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IConnectorOverride;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.feed.ConnectorOverride;
import org.eclipsetrader.core.internal.feed.ConnectorOverrideAdapter;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class ConnectorOverrideProperties extends PropertyPage implements IWorkbenchPropertyPage {
	Button liveFeedOverride;
    ComboViewer liveFeed;
	Button backfillFeedOverride;
    ComboViewer backfillFeed;
	Button intradayBackfillFeedOverride;
    ComboViewer intradayBackfillFeed;

	public ConnectorOverrideProperties() {
		setTitle("Overrides");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		initializeDialogUnits(content);

		liveFeedOverride = new Button(content, SWT.CHECK);
		liveFeedOverride.setText("Live Feed");
		liveFeedOverride.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	liveFeed.getControl().setEnabled(liveFeedOverride.getSelection());
            }
		});

		liveFeed = new ComboViewer(content, SWT.READ_ONLY);
		liveFeed.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData) liveFeed.getControl().getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(32);
		liveFeed.getCombo().setVisibleItemCount(15);
		liveFeed.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (!(element instanceof IFeedConnector)) {
            		IFeedConnector defaultConnector = CoreActivator.getDefault() != null ? CoreActivator.getDefault().getDefaultConnector() : null;
            		return NLS.bind("Default ({0})", new Object[] {
            				defaultConnector != null ? defaultConnector.getName() :  "None",
            			});
            	}
	            return ((IFeedConnector) element).getName();
            }
		});
		liveFeed.setSorter(new ViewerSorter());
		liveFeed.setContentProvider(new ArrayContentProvider());

		backfillFeedOverride = new Button(content, SWT.CHECK);
		backfillFeedOverride.setText("History Backfill");
		backfillFeedOverride.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	backfillFeed.getControl().setEnabled(backfillFeedOverride.getSelection());
            }
		});

		backfillFeed = new ComboViewer(content, SWT.READ_ONLY);
		backfillFeed.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData) backfillFeed.getControl().getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(32);
		backfillFeed.getCombo().setVisibleItemCount(15);
		backfillFeed.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (!(element instanceof IBackfillConnector))
            		return "None";
	            return ((IBackfillConnector) element).getName();
            }
		});
		backfillFeed.setSorter(new ViewerSorter());
		backfillFeed.setContentProvider(new ArrayContentProvider());

		intradayBackfillFeedOverride = new Button(content, SWT.CHECK);
		intradayBackfillFeedOverride.setText("Intraday Backfill");
		intradayBackfillFeedOverride.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	intradayBackfillFeed.getControl().setEnabled(intradayBackfillFeedOverride.getSelection());
            }
		});

		intradayBackfillFeed = new ComboViewer(content, SWT.READ_ONLY);
		intradayBackfillFeed.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData) intradayBackfillFeed.getControl().getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(32);
		intradayBackfillFeed.getCombo().setVisibleItemCount(15);
		intradayBackfillFeed.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (!(element instanceof IBackfillConnector)) {
            		Object o = ((IStructuredSelection) backfillFeed.getSelection()).getFirstElement();
            		return NLS.bind("Default ({0})", new Object[] {
            				o instanceof IBackfillConnector ? ((IBackfillConnector) o).getName() :  "None",
            			});
            	}
	            return ((IBackfillConnector) element).getName();
            }
		});
		intradayBackfillFeed.setSorter(new ViewerSorter());
		intradayBackfillFeed.setContentProvider(new ArrayContentProvider());

	    List<Object> feedConnectors = new ArrayList<Object>();
		if (getFeedService() != null)
			feedConnectors.addAll(Arrays.asList(getFeedService().getConnectors()));
		liveFeed.setInput(feedConnectors.toArray());

	    List<Object> backfillConnectors = new ArrayList<Object>();
	    if (getFeedService() != null)
	    	backfillConnectors.addAll(Arrays.asList(getFeedService().getBackfillConnectors()));

	    backfillFeed.setInput(backfillConnectors.toArray());
	    intradayBackfillFeed.setInput(backfillConnectors.toArray());

	    performDefaults();

		return content;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
		ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
		IConnectorOverride override = (IConnectorOverride) AdapterManager.getDefault().getAdapter(security, IConnectorOverride.class);
		if (override != null) {
			liveFeed.setSelection(override.getLiveFeedConnector() != null ? new StructuredSelection(override.getLiveFeedConnector()) : StructuredSelection.EMPTY);
			backfillFeed.setSelection(override.getBackfillConnector() != null ? new StructuredSelection(override.getBackfillConnector()) : StructuredSelection.EMPTY);
			intradayBackfillFeed.setSelection(override.getIntradayBackfillConnector() != null ? new StructuredSelection(override.getIntradayBackfillConnector()) : StructuredSelection.EMPTY);
		}

		liveFeedOverride.setSelection(!liveFeed.getSelection().isEmpty());
    	liveFeed.getControl().setEnabled(liveFeedOverride.getSelection());

    	backfillFeedOverride.setSelection(!backfillFeed.getSelection().isEmpty());
    	backfillFeed.getControl().setEnabled(backfillFeedOverride.getSelection());

    	intradayBackfillFeedOverride.setSelection(!intradayBackfillFeed.getSelection().isEmpty());
    	intradayBackfillFeed.getControl().setEnabled(intradayBackfillFeedOverride.getSelection());

		super.performDefaults();
    }

    protected void applyChanges() {
		ISecurity security = (ISecurity) getElement().getAdapter(ISecurity.class);
		ConnectorOverrideAdapter adapter = (ConnectorOverrideAdapter) AdapterManager.getDefault().getAdapter(security, ConnectorOverrideAdapter.class);

		if (!liveFeedOverride.getSelection() && !backfillFeedOverride.getSelection() && !intradayBackfillFeedOverride.getSelection())
			adapter.clearOverride(security);
		else {
			ConnectorOverride override = new ConnectorOverride(security);

			IStructuredSelection selection = (IStructuredSelection) liveFeed.getSelection();
			override.setLiveFeedConnector((IFeedConnector) selection.getFirstElement());

			selection = (IStructuredSelection) backfillFeed.getSelection();
			override.setBackfillConnector((IBackfillConnector) selection.getFirstElement());

			selection = (IStructuredSelection) intradayBackfillFeed.getSelection();
			override.setIntradayBackfillConnector((IBackfillConnector) selection.getFirstElement());

			adapter.addOverride(override);
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	if (getControl() != null)
    		applyChanges();
	    return super.performOk();
    }

	protected IFeedService getFeedService() {
    	if (UIActivator.getDefault() == null)
    		return null;
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
