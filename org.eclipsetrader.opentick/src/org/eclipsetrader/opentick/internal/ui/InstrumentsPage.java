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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipsetrader.opentick.internal.core.repository.Instrument;
import org.otfeed.IConnection;
import org.otfeed.IRequest;
import org.otfeed.command.ListSymbolsCommand;
import org.otfeed.event.IDataDelegate;
import org.otfeed.event.OTSymbol;

public class InstrumentsPage extends WizardPage {
	private CheckboxTableViewer instruments;
	private Button refresh;

	private Exchange exchange;
	private Map<Exchange, List<Instrument>> instrumentsMap = new HashMap<Exchange, List<Instrument>>();

	private IRunnableWithProgress downloadRunnable = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        	String title = NLS.bind("Updating {0}", new Object[] {
        			exchange.getDescription()
        		});

        	monitor.beginTask(title, IProgressMonitor.UNKNOWN);
        	try {
    			Connector.getInstance().connect();
    			IConnection connection = Connector.getInstance().getConnection();
    			if (connection == null)
    				return;

    	    	final List<Instrument> list = new ArrayList<Instrument>();
    	    	ListSymbolsCommand command = new ListSymbolsCommand(exchange.getCode(), new IDataDelegate<OTSymbol>() {
    	            public void onData(OTSymbol event) {
    	            	list.add(new Instrument(event.getCode(), event.getCompany(), event.getCurrency(), event.getType().code));
    	            }
    	    	});
    	    	IRequest request = connection.prepareRequest(command);
    	    	request.submit();
    	    	request.waitForCompletion();

        		File file = OTActivator.getDefault().getStateLocation().append(exchange.getCode() + ".xml").toFile();
        		try {
        			if (file.exists())
        				file.delete();

        			JAXBContext jaxbContext = JAXBContext.newInstance(Instrument[].class);
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
        			JAXBElement<Instrument[]> element = new JAXBElement<Instrument[]>(new QName("list"), Instrument[].class, list.toArray(new Instrument[list.size()]));
        			marshaller.marshal(element, new FileWriter(file));
        		} catch (Exception e) {
        			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error saving exchanges list to " + file, e); //$NON-NLS-1$
        			OTActivator.log(status);
        		}

    	    	instrumentsMap.put(exchange, list);

    			try {
    				if (!instruments.getControl().isDisposed()) {
    					instruments.getControl().getDisplay().asyncExec(new Runnable() {
    						public void run() {
                				if (!instruments.getControl().isDisposed()) {
                					instruments.getControl().setRedraw(false);
               						instruments.setInput(list);
                					instruments.getControl().setRedraw(true);
                        			instruments.getControl().setEnabled(true);
                        			setPageComplete(false);
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

	public InstrumentsPage() {
		super("instrument", "Import Instruments", null);
		setDescription("Select the intruments.");
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
		label.setText("Instruments:");

		instruments = CheckboxTableViewer.newCheckList(content, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		instruments.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData) instruments.getControl().getLayoutData()).heightHint = instruments.getTable().getItemHeight() * 15 + instruments.getTable().getBorderWidth() * 2;
		instruments.setLabelProvider(new LabelProvider());
		instruments.setContentProvider(new ArrayContentProvider());
		instruments.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
	            return !"".equals(((Instrument) element).getCompany());
            }
		});
		instruments.setSorter(new ViewerSorter());
		instruments.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
            	setPageComplete(instruments.getCheckedElements().length != 0);
            }
		});

		refresh = new Button(content, SWT.NONE);
		refresh.setText("Refresh List");
		setButtonLayoutData(refresh);
		((GridData) refresh.getLayoutData()).horizontalAlignment = SWT.END;
		refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            	instruments.getControl().setEnabled(false);
            	try {
            		ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
            		dlg.run(true, false, downloadRunnable);
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
	    if (visible && instrumentsMap.get(exchange) == null) {
	    	Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                	init();
                }
	    	});
	    }
    }

	protected void init() {
		try {
    		File file = OTActivator.getDefault().getStateLocation().append(exchange.getCode() + ".xml").toFile();
            if (file.exists() == true) {
    			try {
    				JAXBContext jaxbContext = JAXBContext.newInstance(Instrument[].class);
    				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    				unmarshaller.setEventHandler(new ValidationEventHandler() {
                        public boolean handleEvent(ValidationEvent event) {
            				Status status = new Status(Status.WARNING, OTActivator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
            				OTActivator.log(status);
    	                    return true;
                        }
    				});
    		        JAXBElement<Instrument[]> element = unmarshaller.unmarshal(new StreamSource(file), Instrument[].class);

    		        List<Instrument> list = new ArrayList<Instrument>(Arrays.asList(element.getValue()));
	    	    	instrumentsMap.put(exchange, list);
    			} catch (Exception e) {
    				Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error loading exchanges from " + file, e); //$NON-NLS-1$
    				OTActivator.log(status);
    			}
            }

    	    if (instrumentsMap.get(exchange) != null) {
    			try {
    				if (!instruments.getControl().isDisposed()) {
    					instruments.getControl().getDisplay().asyncExec(new Runnable() {
    						public void run() {
                				if (!instruments.getControl().isDisposed()) {
                					instruments.getControl().setRedraw(false);
               						instruments.setInput(instrumentsMap.get(exchange));
                					instruments.getControl().setRedraw(true);
                        			instruments.getControl().setEnabled(true);
                        			setPageComplete(false);
                				}
    						}
    					});
    				}
    			} catch(Exception e) {
    				// Do nothing
    			}
    	    	return;
    	    }

            ProgressMonitorDialog dlg = new ProgressMonitorDialog(getShell());
    		dlg.run(true, false, downloadRunnable);
    	} catch(Exception e) {
			Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, "Error loading instruments", e); //$NON-NLS-1$
			OTActivator.log(status);
    	}
	}

	public Exchange getExchange() {
    	return exchange;
    }

	public void setExchange(Exchange exchange) {
    	this.exchange = exchange;
    }

	public Instrument[] getInstruments() {
		Object[] o = instruments.getCheckedElements();
		Instrument[] i = new Instrument[o.length];
		System.arraycopy(o, 0, i, 0, i.length);
		return i;
	}
}
