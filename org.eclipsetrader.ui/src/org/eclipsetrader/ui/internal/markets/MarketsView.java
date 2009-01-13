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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketStatusListener;
import org.eclipsetrader.core.markets.MarketStatusEvent;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.SelectionProvider;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MarketsView extends ViewPart {
	private TableViewer viewer;

	private Action newMarketAction;
	private Action deleteAction;

	private Runnable timedRunnable = new Runnable() {
		public void run() {
			if (!viewer.getControl().isDisposed()) {
				viewer.update((Object[]) viewer.getInput(), null);
				int delay = (int)(60000 - System.currentTimeMillis() % 60000);
				Display.getCurrent().timerExec(delay, timedRunnable);
			}
		}
	};

	private IMarketStatusListener marketStatusListener = new IMarketStatusListener() {
        public void marketStatusChanged(MarketStatusEvent event) {
        	if (!viewer.getControl().isDisposed()) {
        		final IMarket market = event.getMarket();
        		try {
        			viewer.getControl().getDisplay().asyncExec(new Runnable() {
        				public void run() {
        		        	if (!viewer.getControl().isDisposed())
    		            		viewer.update(market, null);
        				}
        			});
        		} catch(SWTException e) {
        		}
        	}
        }
	};

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
        	if (!viewer.getControl().isDisposed()) {
        		try {
        			viewer.getControl().getDisplay().asyncExec(new Runnable() {
        				public void run() {
        		        	if (!viewer.getControl().isDisposed()) {
        		            	if (IMarket.PROP_NAME.equals(evt.getPropertyName()))
        		            		viewer.refresh();
        		            	else
        		            		viewer.update(evt.getSource(), null);
        		        	}
        				}
        			});
        		} catch(SWTException e) {
        		}
        	}
        }
	};

	public MarketsView() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    @Override
    public void init(IViewSite site) throws PartInitException {
	    super.init(site);

	    site.setSelectionProvider(new SelectionProvider());

	    newMarketAction = new NewMarketAction(site.getShell()) {
            @Override
            public void run() {
	            super.run();
	    		refreshInput();
            }
	    };

	    deleteAction = new Action("Delete") {
            @Override
            public void run() {
            	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    			MarketService marketService = getMarketService();
    			if (marketService != null) {
    				for (Object obj : selection.toArray()) {
    					if (obj instanceof Market) {
    						Market market = (Market) obj;
    						marketService.deleteMarket(market);
    						PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
    						if (propertyChangeSupport != null)
    							propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    					}
    				}
    			}
    			refreshInput();
            }
	    };
	    deleteAction.setImageDescriptor(UIActivator.getDefault().getImageRegistry().getDescriptor(UIConstants.DELETE_ICON));
	    deleteAction.setDisabledImageDescriptor(UIActivator.getDefault().getImageRegistry().getDescriptor(UIConstants.DELETE_DISABLED_ICON));
	    deleteAction.setEnabled(false);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		createContextMenu();

		refreshInput();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	getViewSite().getSelectionProvider().setSelection(event.getSelection());
            	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            	deleteAction.setEnabled(!selection.isEmpty());
            }
		});

		getMarketService().addMarketStatusListener(marketStatusListener);

		int delay = (int)(60000 - System.currentTimeMillis() % 60000);
		Display.getCurrent().timerExec(delay, timedRunnable);
	}

	protected void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);

		TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Market");
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("State");
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumn.setText("Message");
		tableColumn.setWidth(250);

		viewer.setLabelProvider(new MarketLabelProvider());
		viewer.setComparator(new ViewerComparator());
		viewer.setContentProvider(new ArrayContentProvider());
	}

	protected void refreshInput() {
		IMarket[] input = getInput();
		for (IMarket market : input) {
			PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) market.getAdapter(PropertyChangeSupport.class);
			if (propertyChangeSupport != null) {
				propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
				propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
			}
		}
		viewer.setInput(input);
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
    	Display.getCurrent().timerExec(-1, timedRunnable);
		getMarketService().removeMarketStatusListener(marketStatusListener);
	    super.dispose();
    }

    protected void createContextMenu() {
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				MenuManager newMenu = new MenuManager("New", "group.new");
				{
					newMenu.add(new Separator("top")); //$NON-NLS-1$
					newMenu.add(newMarketAction);
					newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					newMenu.add(new Separator("bottom")); //$NON-NLS-1$
				}
				menuManager.add(newMenu);
				menuManager.add(new GroupMarker("group.goto"));
				menuManager.add(new Separator("group.open"));
				menuManager.add(new GroupMarker("group.openWith"));
				menuManager.add(new Separator("group.show"));
				menuManager.add(new Separator("group.edit"));
				menuManager.add(new GroupMarker("group.reorganize"));
				menuManager.add(new GroupMarker("group.port"));
				menuManager.add(new Separator("group.generate"));
				menuManager.add(new Separator("group.search"));
				menuManager.add(new Separator("group.build"));
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				menuManager.add(new Separator("group.properties"));

				menuManager.appendToGroup("group.reorganize", deleteAction);
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
    }

    protected IMarket[] getInput() {
		MarketService marketService = getMarketService();
		return marketService != null ? marketService.getMarkets() : new IMarket[0];
    }

    protected MarketService getMarketService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(MarketService.class.getName());
    		MarketService service = (MarketService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading market service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
    }
}
