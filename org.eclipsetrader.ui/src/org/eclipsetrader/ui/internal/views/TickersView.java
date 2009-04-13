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

package org.eclipsetrader.ui.internal.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.repositories.IStoreObject;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;
import org.eclipsetrader.ui.internal.providers.ChangeFactory;
import org.eclipsetrader.ui.internal.providers.LastTradePriceFactory;
import org.eclipsetrader.ui.internal.providers.LastTradeTimeFactory;
import org.eclipsetrader.ui.internal.providers.SecurityNameFactory;
import org.eclipsetrader.ui.internal.providers.TrendFactory;
import org.eclipsetrader.ui.internal.repositories.RepositoryObjectTransfer;
import org.eclipsetrader.ui.internal.securities.SecurityObjectTransfer;

public class TickersView extends ViewPart {
	public static final String VIEW_ID = "org.eclipsetrader.ui.views.tickers";
	public static final String K_SECURITIES = "securities";

	private BoxViewer viewer;
	private IDialogSettings dialogSettings;

	private Action deleteAction;
	private Action settingsAction;

	private List<TickerViewItem> input;
	private MarketPricingEnvironment pricingEnvironment;
	private IDataProvider[] providers;

	private IPricingListener pricingListener = new IPricingListener() {
        public void pricingUpdate(PricingEvent event) {
        	onPricingUpdate(event);
        }
	};

	public TickersView() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
	    super.init(site, memento);
		ImageRegistry imageRegistry = UIActivator.getDefault().getImageRegistry();

		dialogSettings = UIActivator.getDefault().getDialogSettings().getSection(site.getId());
		if (dialogSettings == null)
			dialogSettings = UIActivator.getDefault().getDialogSettings().addNewSection(site.getId());

		deleteAction = new Action("Delete") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				input.removeAll(selection.toList());

				Object[] o = selection.toArray();
				ISecurity[] s = new ISecurity[o.length];
				for (int i = 0; i < s.length; i++)
					s[i] = ((TickerViewItem) o[i]).getSecurity();
				pricingEnvironment.removeSecurities(s);

				saveInput();
				viewer.refresh();
			}
		};
		deleteAction.setImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_ICON));
		deleteAction.setDisabledImageDescriptor(imageRegistry.getDescriptor(UIConstants.DELETE_EDIT_DISABLED_ICON));
		deleteAction.setId(ActionFactory.DELETE.getId());
		deleteAction.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
		deleteAction.setEnabled(false);

		settingsAction = new TickersSettingsAction(site.getShell(), this);

		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(settingsAction.getId(), settingsAction);
		actionBars.setGlobalActionHandler(deleteAction.getId(), deleteAction);
		actionBars.updateActionBars();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = createViewer(parent);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
				deleteAction.setEnabled(!event.getSelection().isEmpty());
            }
		});
		viewer.addDropSupport(
				DND.DROP_COPY | DND.DROP_MOVE,
				new Transfer[] {
						SecurityObjectTransfer.getInstance(),
						RepositoryObjectTransfer.getInstance(),
					},
				new ViewerDropAdapter(viewer) {
                    @Override
                    public boolean validateDrop(Object target, int operation, TransferData transferType) {
	                    return SecurityObjectTransfer.getInstance().isSupportedType(transferType) ||
	                           RepositoryObjectTransfer.getInstance().isSupportedType(transferType);
                    }

                    @Override
                    public boolean performDrop(Object data) {
						final IAdaptable[] contents = (IAdaptable[]) data;
						for (int i = 0; i < contents.length; i++) {
							ISecurity security = (ISecurity) contents[i].getAdapter(ISecurity.class);
							if (security != null) {
								TickerViewItem viewItem = new TickerViewItem(security);
								input.add(viewItem);

								pricingEnvironment.addSecurity(security);
								viewItem.setTrade(pricingEnvironment.getTrade(security));
								viewItem.setQuote(pricingEnvironment.getQuote(security));
								viewItem.setLastClose(pricingEnvironment.getLastClose(security));

								IAdaptable[] newValues = new IAdaptable[providers.length];
								for (int ii = 0; ii < newValues.length; ii++)
									newValues[ii] = providers[ii] != null ? providers[ii].getValue(viewItem) : null;
								viewItem.setValues(newValues);
							}
						}
						viewer.refresh();
						saveInput();
						return true;
                    }
				});

		providers = new IDataProvider[] {
			new SecurityNameFactory().createProvider(),
			new LastTradeTimeFactory().createProvider(),
			new LastTradePriceFactory().createProvider(),
			new ChangeFactory().createProvider(),
			new TrendFactory().createProvider(),
		};

		IRepositoryService repository = getRepositoryService();

		input = new ArrayList<TickerViewItem>();
		List<ISecurity> l = new ArrayList<ISecurity>();

		String[] uri = dialogSettings.getArray(K_SECURITIES);
		if (uri != null) {
			for (int i = 0; i < uri.length; i++) {
				try {
					ISecurity security = repository.getSecurityFromURI(new URI(uri[i]));
					l.add(security);
					input.add(new TickerViewItem(security));
				} catch(Exception e) {
		        	Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, "Error loading security " + uri[i], e);
			        UIActivator.getDefault().getLog().log(status);
				}
			}
		}

		pricingEnvironment = new MarketPricingEnvironment(getMarketService(), l.toArray(new ISecurity[l.size()]));
		pricingEnvironment.addPricingListener(pricingListener);

		for (TickerViewItem viewItem : input) {
			viewItem.setTrade(pricingEnvironment.getTrade(viewItem.getSecurity()));
			viewItem.setQuote(pricingEnvironment.getQuote(viewItem.getSecurity()));
			viewItem.setLastClose(pricingEnvironment.getLastClose(viewItem.getSecurity()));

			IAdaptable[] newValues = new IAdaptable[providers.length];
			for (int ii = 0; ii < newValues.length; ii++)
				newValues[ii] = providers[ii] != null ? providers[ii].getValue(viewItem) : null;
			viewItem.setValues(newValues);
		}

		MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuManager) {
				menuManager.add(deleteAction);
				menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

		getViewSite().setSelectionProvider(viewer);

		viewer.setInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
    	if (pricingEnvironment != null) {
    		pricingEnvironment.removePricingListener(pricingListener);
    		pricingEnvironment.dispose();
    	}
		saveInput();
	    super.dispose();
    }

	protected void saveInput() {
    	String[] ar = new String[input.size()];
    	for (int i = 0; i < ar.length; i++) {
    		IStoreObject storeObject = (IStoreObject) input.get(i).getSecurity().getAdapter(IStoreObject.class);
    		ar[i] = storeObject.getStore().toURI().toString();
    	}
    	dialogSettings.put(K_SECURITIES, ar);
    }

	protected BoxViewer createViewer(Composite parent) {
		BoxViewer viewer = new BoxViewer(parent);
		viewer.setUseHashlookup(true);

		viewer.setLabelProvider(new ViewItemLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());

		return viewer;
	}

    @SuppressWarnings("unchecked")
    protected int compareValues(IAdaptable[] v1, IAdaptable[] v2, int sortColumn) {
    	if (sortColumn < 0 || sortColumn >= v1.length || sortColumn >= v2.length)
    		return 0;
    	if (v1[sortColumn] == null || v2[sortColumn] == null)
    		return 0;

    	Object o1 = v1[sortColumn].getAdapter(Comparable.class);
    	Object o2 = v2[sortColumn].getAdapter(Comparable.class);
    	if (o1 != null && o2 != null)
    		return ((Comparable) o1).compareTo(o2);

    	o1 = v1[sortColumn].getAdapter(Number.class);
    	o2 = v2[sortColumn].getAdapter(Number.class);
    	if (o1 != null && o2 != null) {
    		if (((Number) o1).doubleValue() < ((Number) o2).doubleValue())
    			return -1;
    		if (((Number) o1).doubleValue() > ((Number) o2).doubleValue())
    			return 1;
    		return 0;
    	}

    	return 0;
    }

    protected void onPricingUpdate(PricingEvent event) {
    	final List<TickerViewItem> l = new ArrayList<TickerViewItem>();

    	synchronized(input) {
    		for (TickerViewItem viewItem : input) {
    			if (viewItem.getSecurity() == event.getSecurity()) {
    				for (PricingDelta delta : event.getDelta()) {
    					if (delta.getNewValue() instanceof ITrade)
    						viewItem.setTrade((ITrade) delta.getNewValue());
    					if (delta.getNewValue() instanceof IQuote)
    						viewItem.setQuote((IQuote) delta.getNewValue());
    					if (delta.getNewValue() instanceof ILastClose)
    						viewItem.setLastClose((ILastClose) delta.getNewValue());
    				}

    				IAdaptable[] oldValues = viewItem.getValues();

    				IAdaptable[] newValues = new IAdaptable[providers.length];
    				for (int i = 0; i < newValues.length; i++)
    					newValues[i] = providers[i] != null ? providers[i].getValue(viewItem) : null;

   					if (!valuesEquals(oldValues, newValues)) {
   						viewItem.setValues(newValues);
   						l.add(viewItem);
   					}
    			}
    		}
    	}

    	if (!viewer.getControl().isDisposed()) {
    		try {
    	    	viewer.getControl().getDisplay().asyncExec(new Runnable() {
    	    		public void run() {
    	    	    	if (!viewer.getControl().isDisposed())
    	    	    		viewer.update(l.toArray(), null);
    	    		}
    	    	});
    		} catch(SWTException e) {
    			// Do nothing
    		}
    	}
    }

    protected boolean valuesEquals(IAdaptable[] oldValues, IAdaptable[] newValues) {
    	if (oldValues == newValues)
    		return true;

    	if ((oldValues == null && newValues != null) || (oldValues != null && newValues == null))
    		return false;
    	if (oldValues.length != newValues.length)
    		return false;

    	for (int i = 0; i < newValues.length; i++) {
        	if (oldValues[i] == newValues[i])
        		continue;
        	if ((oldValues[i] == null && newValues[i] != null) || (oldValues[i] != null && newValues[i] == null))
        		return false;
        	if (!newValues[i].equals(oldValues[i]))
        		return false;
    	}

    	return true;
    }

	protected IRepositoryService getRepositoryService() {
		return UIActivator.getDefault().getRepositoryService();
	}

    protected IMarketService getMarketService() {
    	return UIActivator.getDefault().getMarketService();
    }

    public TickerViewItem[] getViewItems() {
    	return input.toArray(new TickerViewItem[input.size()]);
    }

    public void setViewItems(TickerViewItem[] input) {
		for (TickerViewItem viewItem : input)
			pricingEnvironment.removeSecurity(viewItem.getSecurity());

		this.input = new ArrayList<TickerViewItem>(Arrays.asList(input));
		saveInput();

		for (TickerViewItem viewItem : input) {
			pricingEnvironment.addSecurity(viewItem.getSecurity());

			viewItem.setTrade(pricingEnvironment.getTrade(viewItem.getSecurity()));
			viewItem.setQuote(pricingEnvironment.getQuote(viewItem.getSecurity()));
			viewItem.setLastClose(pricingEnvironment.getLastClose(viewItem.getSecurity()));

			IAdaptable[] newValues = new IAdaptable[providers.length];
			for (int ii = 0; ii < newValues.length; ii++)
				newValues[ii] = providers[ii] != null ? providers[ii].getValue(viewItem) : null;
			viewItem.setValues(newValues);
		}

		viewer.setInput(this.input);
    }
}
