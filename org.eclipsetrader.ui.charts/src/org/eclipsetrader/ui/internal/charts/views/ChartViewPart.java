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

package org.eclipsetrader.ui.internal.charts.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplate;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.ChartRowViewItem;
import org.eclipsetrader.ui.charts.ChartView;
import org.eclipsetrader.ui.charts.ChartViewer;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartViewPart extends ViewPart {
	public static final String VIEW_ID = "org.eclipsetrader.ui.chart";

	public static final String K_VIEWS = "Views";
	public static final String K_URI = "uri";
	public static final String K_TEMPLATE = "template";

	private URI uri;
	private ISecurity security;
	private IChartTemplate template;

	private BaseChartViewer viewer;
	private ChartView view;
	private IHistory history;

	private IDialogSettings dialogSettings;

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	if (IPropertyConstants.BARS.equals(evt.getPropertyName()))
        		job.schedule();
        }
	};

	private Job job = new Job("ChartObject Loading") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
        	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            try {
            	if (history == null) {
                	IRepositoryService repositoryService = ChartsUIActivator.getDefault().getRepositoryService();
                	history = repositoryService.getHistoryFor(security);
                	PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
                	if (propertyChangeSupport != null)
                		propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
            	}

    	        view = new ChartView(template);
    	        view.setRootDataSeries(new OHLCDataSeries(history.getSecurity() != null ? history.getSecurity().getName() : "MAIN", history.getOHLC()));

    	        if (!viewer.isDisposed()) {
    				try {
    					viewer.getDisplay().asyncExec(new Runnable() {
    						public void run() {
    							if (!viewer.isDisposed()) {
    		    					BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
    		    						public void run() {
    		    							ChartRowViewItem[] rowViewItem = (ChartRowViewItem[]) view.getAdapter(ChartRowViewItem[].class);
    		    							if (rowViewItem != null) {
    		    								IChartObject[] input = new IChartObject[rowViewItem.length];
    		    								for (int i = 0; i < input.length; i++)
    		    									input[i] = (IChartObject) rowViewItem[i].getAdapter(IChartObject.class);
        		   				    			viewer.setInput(input);
    		    							}
    		    						}
    		    					});
    							}
    						}
    					});
    				} catch (SWTException e) {
    					if (e.code != SWT.ERROR_DEVICE_DISPOSED)
    						throw e;
    				}
    			}
            } catch (Exception e) {
            	Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, "Error loading view " + getViewSite().getSecondaryId(), e);
            	ChartsUIActivator.getDefault().getLog().log(status);
            } finally {
            	monitor.done();
            }
	        return Status.OK_STATUS;
        }
	};

	public ChartViewPart() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);

        try {
    		dialogSettings = ChartsUIActivator.getDefault().getDialogSettings().getSection(K_VIEWS).getSection(site.getSecondaryId());
        	uri = new URI(dialogSettings.get(K_URI));

        	IRepositoryService repositoryService = ChartsUIActivator.getDefault().getRepositoryService();
        	security = repositoryService.getSecurityFromURI(uri);

			JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
	        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	        unmarshaller.setEventHandler(new ValidationEventHandler() {
				public boolean handleEvent(ValidationEvent event) {
					System.out.println("Error validating XML: " + event.getMessage());
					if (event.getLinkedException() != null)
						event.getLinkedException().printStackTrace(System.out);
					return true;
				}
			});

        	IPath templatePath = new Path("data").append(dialogSettings.get(K_TEMPLATE)); //$NON-NLS-1$
        	InputStream stream = FileLocator.openStream(ChartsUIActivator.getDefault().getBundle(), templatePath, false);
	        template = (IChartTemplate) unmarshaller.unmarshal(stream);

        } catch (Exception e) {
        	Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, "Error loading view " + site.getSecondaryId(), e);
        	ChartsUIActivator.getDefault().getLog().log(status);
        }

        IActionBars actionBars = site.getActionBars();
        actionBars.getToolBarManager().add(new Separator("additions"));
        actionBars.updateActionBars();
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
    	viewer = new BaseChartViewer(parent, SWT.NONE);
    	//viewer.setHorizontalAxis(new DateValuesAxis());
		//((DateValuesAxis) viewer.getHorizontalAxis()).fillAvailableSpace = false;
		//viewer.setVerticalAxis(new DoubleValuesAxis());
		viewer.setHorizontalScaleVisible(true);
		viewer.setVerticalScaleVisible(true);
		//viewer.setRenderer(new ChartDocumentRenderer());
		//viewer.setContentProvider(new ChartDocumentContentProvider());

		if (security != null && template != null) {
			setPartName(NLS.bind("{0} - {1}", new Object[] {
					security.getName(),
					template.getName(),
				}));

			job.setName("Loading " + getPartName());
			job.setUser(false);
			job.schedule();
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    	viewer.getControl().setFocus();
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
    	if (history != null) {
        	PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
        	if (propertyChangeSupport != null)
        		propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    	}

    	super.dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(ChartViewer.class))
    		return viewer;
    	if (adapter.isAssignableFrom(ChartView.class))
    		return view;
    	if (adapter.isAssignableFrom(IChartTemplate.class))
    		return template;
    	if (adapter.isAssignableFrom(ISecurity.class))
    		return security;
    	if (adapter.isAssignableFrom(IDialogSettings.class))
    		return dialogSettings;
	    return super.getAdapter(adapter);
    }
}
