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

package org.eclipsetrader.opentick.internal.ui;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.opentick.internal.Connector;
import org.eclipsetrader.opentick.internal.OTActivator;
import org.eclipsetrader.opentick.internal.core.repository.Exchange;
import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.ListExchangesCommand;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTExchange;

public class ExchangePage extends WizardPage {
	private TableViewer viewer;
	private Button refresh;

	private List<Exchange> exchangeList;

	private IRunnableWithProgress downloadExchangeRunnable = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        	String title = NLS.bind("Updating {0}", new Object[] {
        			"exchange list"
        		});

        	monitor.beginTask(title, IProgressMonitor.UNKNOWN);
        	try {
    			Connector.getInstance().connect();
    			IConnection connection = Connector.getInstance().getConnection();
    			if (connection == null)
    				return;

    			exchangeList = new ArrayList<Exchange>();
    	    	ListExchangesCommand command = new ListExchangesCommand(new IDataDelegate<OTExchange>() {
    	            public void onData(OTExchange event) {
    	            	exchangeList.add(new Exchange(event.getCode(), event.isAvailable(), event.getTitle()));
    	            }
    	    	});
    	    	IRequest request = connection.prepareRequest(command);
    	    	request.submit();
    	    	request.waitForCompletion();

        		File file = OTActivator.getDefault().getStateLocation().append("exchanges.xml").toFile();
        		try {
        			if (file.exists())
        				file.delete();

        			JAXBContext jaxbContext = JAXBContext.newInstance(Exchange[].class);
        			Marshaller marshaller = jaxbContext.createMarshaller();
        			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        			marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
        			marshaller.setEventHandler(new ValidationEventHandler() {
                        public boolean handleEvent(ValidationEvent event) {
            				Status status = new Status(Status.WARNING, OTActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
            				OTActivator.log(status);
                            return true;
                        }
        			});
        			JAXBElement<Exchange[]> element = new JAXBElement<Exchange[]>(new QName("list"), Exchange[].class, exchangeList.toArray(new Exchange[exchangeList.size()]));
        			marshaller.marshal(element, new FileWriter(file));
        		} catch (Exception e) {
        			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error saving exchanges list to " + file, e); //$NON-NLS-1$
        			OTActivator.log(status);
        		}

        		try {
    				if (!viewer.getControl().isDisposed()) {
    					viewer.getControl().getDisplay().asyncExec(new Runnable() {
    						public void run() {
                				if (!viewer.getControl().isDisposed()) {
                       				viewer.setInput(exchangeList);
                		        	viewer.getControl().setEnabled(true);
                				}
    						}
    					});
    				}
    			} catch(Exception e) {
    				// Do nothing
    			}
        	} finally {
        		monitor.done();
        	}
        }
	};

	public ExchangePage() {
		super("exchange", "Import Instruments", null);
		setDescription("Select the exchange.");
		setPageComplete(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		setControl(content);
		initializeDialogUnits(content);

		Label label = new Label(content, SWT.NONE);
		label.setText("Exchange:");

		viewer = new TableViewer(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
	            return ((Exchange) element).isAvailable();
            }
		});
		viewer.setSorter(new ViewerSorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
        		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        		onExchangeSelection(!selection.isEmpty() ? (Exchange) selection.getFirstElement() : null);
            }
		});

		refresh = new Button(content, SWT.NONE);
		refresh.setText("Refresh List");
		setButtonLayoutData(refresh);
		((GridData) refresh.getLayoutData()).horizontalAlignment = SWT.END;
		refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            	viewer.getControl().setEnabled(false);
            	try {
            		ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
            		dlg.run(true, false, downloadExchangeRunnable);
            	} catch(Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error loading exchanges", e); //$NON-NLS-1$
    				OTActivator.log(status);
            	}
            }
		});
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
	    super.setVisible(visible);
	    if (visible && exchangeList == null) {
	    	Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                	init();
                }
	    	});
	    }
    }

	protected void init() {
        if (exchangeList == null) {
    		File file = OTActivator.getDefault().getStateLocation().append("exchanges.xml").toFile();
            if (file.exists() == true) {
    			try {
    				JAXBContext jaxbContext = JAXBContext.newInstance(Exchange[].class);
    				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    				unmarshaller.setEventHandler(new ValidationEventHandler() {
                        public boolean handleEvent(ValidationEvent event) {
            				Status status = new Status(Status.WARNING, OTActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
            				OTActivator.log(status);
    	                    return true;
                        }
    				});
    		        JAXBElement<Exchange[]> element = unmarshaller.unmarshal(new StreamSource(file), Exchange[].class);

    		        exchangeList = new ArrayList<Exchange>(Arrays.asList(element.getValue()));
    			} catch (Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error loading exchanges from " + file, e); //$NON-NLS-1$
    				OTActivator.log(status);
    			}
            }
        }

        if (exchangeList == null) {
        	viewer.getControl().setEnabled(false);
        	try {
        		ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
        		dlg.run(true, false, downloadExchangeRunnable);
        	} catch(Exception e) {
				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error loading exchanges", e); //$NON-NLS-1$
				OTActivator.log(status);
        	}
        }

        if (exchangeList != null)
        	viewer.setInput(exchangeList);
	}

	protected void onExchangeSelection(final Exchange exchange) {
		if (exchange != null) {
			IWizardPage page = getWizard().getPage("instrument");
			((InstrumentsPage) page).setExchange(exchange);
		}
		setPageComplete(exchange != null);
	}
}
