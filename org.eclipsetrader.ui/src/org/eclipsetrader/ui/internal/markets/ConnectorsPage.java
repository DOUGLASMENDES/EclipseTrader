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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.feed.IFeedService;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ConnectorsPage extends PropertyPage {
    ComboViewer liveFeed;
    ComboViewer backfillFeed;
    ComboViewer intradayBackfillFeed;
    ComboViewer brokerCombo;

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
            		IFeedConnector defaultConnector = CoreActivator.getDefault() != null ? CoreActivator.getDefault().getDefaultConnector() : null;
            		return NLS.bind("Default ({0})", new Object[] {
            				defaultConnector != null ? defaultConnector.getName() :  "None",
            			});
            	}
	            return ((IFeedConnector) element).getName();
            }
		});
		liveFeed.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
            	if (element instanceof IFeedConnector)
            		return 1;
	            return 0;
            }
		});
		liveFeed.setContentProvider(new ArrayContentProvider());

		label = new Label(content, SWT.NONE);
		label.setText("History Backfill");
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		backfillFeed = new ComboViewer(content, SWT.READ_ONLY);
		backfillFeed.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		backfillFeed.getCombo().setVisibleItemCount(15);
		backfillFeed.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (!(element instanceof IBackfillConnector))
            		return "None";
	            return ((IBackfillConnector) element).getName();
            }
		});
		backfillFeed.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
            	if (element instanceof IBackfillConnector)
            		return 1;
	            return 0;
            }
		});
		backfillFeed.setContentProvider(new ArrayContentProvider());

		label = new Label(content, SWT.NONE);
		label.setText("Intraday Backfill");
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		intradayBackfillFeed = new ComboViewer(content, SWT.READ_ONLY);
		intradayBackfillFeed.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
		intradayBackfillFeed.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
            	if (element instanceof IBackfillConnector)
            		return 1;
	            return 0;
            }
		});
		intradayBackfillFeed.setContentProvider(new ArrayContentProvider());

		label = new Label(content, SWT.NONE);
		label.setText("Broker");
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		brokerCombo = new ComboViewer(content, SWT.READ_ONLY);
		brokerCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		brokerCombo.getCombo().setVisibleItemCount(15);
		brokerCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (!(element instanceof IBroker))
            		return "None";
	            return ((IBroker) element).getName();
            }
		});
		brokerCombo.setSorter(new ViewerSorter() {
            @Override
            public int category(Object element) {
            	if (element instanceof IBroker)
            		return 1;
	            return 0;
            }
		});
		brokerCombo.setContentProvider(new ArrayContentProvider());

	    List<Object> feedConnectors = new ArrayList<Object>();
		feedConnectors.add(new Object());
		if (getFeedService() != null)
			feedConnectors.addAll(Arrays.asList(getFeedService().getConnectors()));
		liveFeed.setInput(feedConnectors.toArray());
		liveFeed.setSelection(new StructuredSelection(feedConnectors.get(0)));

		final Object defaultBackfillElement = new Object();
	    List<Object> backfillConnectors = new ArrayList<Object>();
	    backfillConnectors.add(defaultBackfillElement);
	    if (getFeedService() != null)
	    	backfillConnectors.addAll(Arrays.asList(getFeedService().getBackfillConnectors()));

	    backfillFeed.setInput(backfillConnectors.toArray());
	    backfillFeed.setSelection(new StructuredSelection(defaultBackfillElement));
		backfillFeed.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
				intradayBackfillFeed.update(defaultBackfillElement, null);
            }
	    });

	    intradayBackfillFeed.setInput(backfillConnectors.toArray());
	    intradayBackfillFeed.setSelection(new StructuredSelection(defaultBackfillElement));

	    List<Object> brokers = new ArrayList<Object>();
	    brokers.add(new Object());
	    if (getTradingService() != null)
	    	brokers.addAll(Arrays.asList(getTradingService().getBrokers()));
	    brokerCombo.setInput(brokers.toArray());
	    brokerCombo.setSelection(new StructuredSelection(brokers.get(0)));

		if (getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				IFeedConnector liveConnector = market.getLiveFeedConnector();
				if (liveConnector != null) {
					if (!feedConnectors.contains(liveConnector)) {
						feedConnectors.add(liveConnector);
						liveFeed.setInput(feedConnectors.toArray());
					}
					liveFeed.setSelection(new StructuredSelection(liveConnector));
				}
				IBackfillConnector backfillConnector = market.getBackfillConnector();
				if (backfillConnector != null) {
					if (!backfillConnectors.contains(backfillConnector)) {
						backfillConnectors.add(backfillConnector);
					    backfillFeed.setInput(backfillConnectors.toArray());
					    intradayBackfillFeed.setInput(backfillConnectors.toArray());
					}
					backfillFeed.setSelection(new StructuredSelection(backfillConnector));
				}
				backfillConnector = market.getIntradayBackfillConnector();
				if (backfillConnector != null) {
					if (!backfillConnectors.contains(backfillConnector)) {
						backfillConnectors.add(backfillConnector);
					    backfillFeed.setInput(backfillConnectors.toArray());
					    intradayBackfillFeed.setInput(backfillConnectors.toArray());
					}
					intradayBackfillFeed.setSelection(new StructuredSelection(backfillConnector));
				}
				else
				    intradayBackfillFeed.setSelection(new StructuredSelection(defaultBackfillElement));
				IBroker broker = market.getBroker();
				if (broker != null) {
					if (!brokers.contains(broker)) {
						brokers.add(broker);
					    brokerCombo.setInput(brokers.toArray());
					}
					brokerCombo.setSelection(new StructuredSelection(broker));
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
				s = ((IStructuredSelection) backfillFeed.getSelection()).getFirstElement();
				market.setBackfillConnector(s instanceof IBackfillConnector ? (IBackfillConnector) s : null);
				s = ((IStructuredSelection) intradayBackfillFeed.getSelection()).getFirstElement();
				market.setIntradayBackfillConnector(s instanceof IBackfillConnector ? (IBackfillConnector) s : null);
				s = ((IStructuredSelection) brokerCombo.getSelection()).getFirstElement();
				market.setBroker(s instanceof IBroker ? (IBroker) s : null);
			}
		}
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

    protected ITradingService getTradingService() {
    	if (UIActivator.getDefault() == null)
    		return null;
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
    		ITradingService service = (ITradingService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading trading service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }
}
