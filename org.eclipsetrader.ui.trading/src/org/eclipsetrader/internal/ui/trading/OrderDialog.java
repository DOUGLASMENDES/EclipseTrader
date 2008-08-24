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

package org.eclipsetrader.internal.ui.trading;

import java.text.NumberFormat;
import java.util.Date;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.core.trading.Order;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class OrderDialog extends TitleAreaDialog {
	private Text symbol;
	private Label symbolDescription;

	private ComboViewer sideCombo;
	private Text quantity;
	private ComboViewer typeCombo;
	private Text price;
	private ComboViewer brokerCombo;
	private ComboViewer routeCombo;

	private ComboViewer validityCombo;
	private CDateTime expireDate;
	private Text orderReference;

	private ISecurity security;
	private IBroker broker;

	private NumberFormat numberFormat;
	private NumberFormat priceFormat;

	private ISelectionChangedListener orderTypeSelectionListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
        	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        	price.setEnabled(selection.getFirstElement() == IOrderType.Limit);
        	getButton(OK).setEnabled(isValid());
        }
	};

	public OrderDialog(Shell parentShell) {
		super(parentShell);

		numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(0);
		numberFormat.setGroupingUsed(true);

		priceFormat = NumberFormat.getInstance();
		priceFormat.setMinimumFractionDigits(1);
		priceFormat.setMaximumFractionDigits(4);
		priceFormat.setGroupingUsed(true);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Trade Order");
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

    	createContractDescriptionGroup(composite);
    	createOrderDescriptionGroup(composite);
    	createMiscellaneousGroup(composite);

    	ITradingService tradingService = getTradingService();
    	brokerCombo.setInput(tradingService.getBrokers());

    	if (security != null) {
    		symbol.setText(getSecuritySymbol(security));
    		symbolDescription.setText(security.getName());
    	}
    	if (broker != null) {
        	IStructuredSelection selection = new StructuredSelection(broker);
        	brokerCombo.setSelection(selection);
        	handleBrokerSelection(selection);
    	}

    	symbol.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	if (!brokerCombo.getSelection().isEmpty()) {
        			IBroker connector = (IBroker) ((IStructuredSelection) brokerCombo.getSelection()).getFirstElement();
        			security = connector.getSecurityFromSymbol(symbol.getText());
            		symbolDescription.setText(security.getName());
            	}
            	getButton(OK).setEnabled(isValid());
            }
    	});

    	typeCombo.addSelectionChangedListener(orderTypeSelectionListener);
    	brokerCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            	handleBrokerSelection(selection);
            	getButton(OK).setEnabled(isValid());
            }
    	});
    	validityCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	IStructuredSelection selection = (IStructuredSelection) event.getSelection();

            	IOrderValidity validity = selection.isEmpty() ? null : (IOrderValidity) (selection).getFirstElement();
            	expireDate.setEnabled(validity == IOrderValidity.GoodTillDate);

            	getButton(OK).setEnabled(isValid());
            }
    	});

    	quantity.setFocus();

    	return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
	    Control control = super.createContents(parent);

		setTitle("Trade Order");
		setMessage("Fill the form to send a trade order.");
	    getButton(OK).setEnabled(isValid());

	    return control;
    }

	protected void createContractDescriptionGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

    	Label label = new Label(composite, SWT.NONE);
    	label.setText("Symbol");
    	symbol = new Text(composite, SWT.BORDER);
    	symbol.setLayoutData(new GridData(convertWidthInCharsToPixels(18), SWT.DEFAULT));
    	symbolDescription = new Label(composite, SWT.NONE);
    	symbolDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	protected void createOrderDescriptionGroup(Composite parent) {
		Group content = new Group(parent, SWT.NONE);
		content.setText("Order Description");
    	content.setLayout(new GridLayout(2, false));
    	content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    	Label label = new Label(content, SWT.NONE);
    	label.setText("Action");
    	sideCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	sideCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	sideCombo.setContentProvider(new ArrayContentProvider());
    	sideCombo.setLabelProvider(new LabelProvider());

    	label = new Label(content, SWT.NONE);
    	label.setText("Quantity");
    	quantity = new Text(content, SWT.BORDER);
    	quantity.setLayoutData(new GridData(convertWidthInCharsToPixels(18), SWT.DEFAULT));
    	((GridData) quantity.getLayoutData()).horizontalAlignment = SWT.FILL;
    	quantity.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	getButton(OK).setEnabled(isValid());
            }
    	});
    	quantity.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
            	try {
        	    	long n = numberFormat.parse(((Text) event.widget).getText()).longValue();
        	    	((Text) event.widget).setText(numberFormat.format(n));
            	} catch(Exception e) {
            	}
            }
    	});

    	label = new Label(content, SWT.NONE);
    	label.setText("Order Type");
    	typeCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	typeCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	typeCombo.setContentProvider(new ArrayContentProvider());
    	typeCombo.setLabelProvider(new LabelProvider());

    	label = new Label(content, SWT.NONE);
    	label.setText("Limit Price");
    	price = new Text(content, SWT.BORDER);
    	price.setLayoutData(new GridData(convertWidthInCharsToPixels(18), SWT.DEFAULT));
    	((GridData) price.getLayoutData()).horizontalAlignment = SWT.FILL;
    	price.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	getButton(OK).setEnabled(isValid());
            }
    	});
    	price.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
            	try {
        	    	double n = priceFormat.parse(((Text) event.widget).getText()).doubleValue();
        	    	((Text) event.widget).setText(priceFormat.format(n));
            	} catch(Exception e) {
            	}
            }
    	});

    	label = new Label(content, SWT.NONE);
    	label.setText("Broker");
    	brokerCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	brokerCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	brokerCombo.setContentProvider(new ArrayContentProvider());
    	brokerCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
	            return ((IBroker) element).getName();
            }
    	});
    	brokerCombo.setSorter(new ViewerSorter());

    	label = new Label(content, SWT.NONE);
    	label.setText("Route");
    	routeCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	routeCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	routeCombo.setContentProvider(new ArrayContentProvider());
    	routeCombo.setLabelProvider(new LabelProvider());
    	routeCombo.setSorter(new ViewerSorter());
	}

	protected void createMiscellaneousGroup(Composite parent) {
		Group content = new Group(parent, SWT.NONE);
		content.setText("Miscellaneous");
    	content.setLayout(new GridLayout(2, false));
    	content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    	Label label = new Label(content, SWT.NONE);
    	label.setText("Time in Force");
    	validityCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	validityCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	validityCombo.setContentProvider(new ArrayContentProvider());
    	validityCombo.setLabelProvider(new LabelProvider());

    	label = new Label(content, SWT.NONE);
    	label.setText("Expire");
    	expireDate = new CDateTime(content, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
    	expireDate.setSelection(new Date());
    	expireDate.setEnabled(false);

    	label = new Label(content, SWT.NONE);
    	label.setText("Order Ref.");
    	orderReference = new Text(content, SWT.BORDER);
    	orderReference.setLayoutData(new GridData(convertWidthInCharsToPixels(40), SWT.DEFAULT));
	}

	protected boolean isValid() {
		if (symbol.getText().equals(""))
			return false;

		try {
	    	long quantity = numberFormat.parse(this.quantity.getText()).longValue();
	    	if (quantity <= 0)
	    		return false;
		} catch(Exception e) {
			return false;
		}

		IOrderType orderType = (IOrderType) ((IStructuredSelection) typeCombo.getSelection()).getFirstElement();
		if (orderType == IOrderType.Limit) {
			try {
		    	double price = priceFormat.parse(this.price.getText()).doubleValue();
		    	if (price <= 0)
		    		return false;
			} catch(Exception e) {
				return false;
			}
		}

		return true;
    }

	public void setSecurity(ISecurity security) {
    	this.security = security;
    }

	public void setBroker(IBroker broker) {
    	this.broker = broker;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
		try {
			IOrderType orderType = (IOrderType) ((IStructuredSelection) typeCombo.getSelection()).getFirstElement();
			Double limitPrice = orderType == IOrderType.Market ? null : priceFormat.parse(price.getText()).doubleValue();
	    	Order order = new Order(
	    			null,
	    			orderType,
	    			(IOrderSide) ((IStructuredSelection) sideCombo.getSelection()).getFirstElement(),
	    			security,
	    			numberFormat.parse(quantity.getText()).longValue(),
	    			limitPrice
	    		);

	    	if (!routeCombo.getSelection().isEmpty())
	    		order.setRoute((IOrderRoute) ((IStructuredSelection) routeCombo.getSelection()).getFirstElement());

	    	if (!validityCombo.getSelection().isEmpty()) {
	    		IOrderValidity validity = (IOrderValidity) ((IStructuredSelection) validityCombo.getSelection()).getFirstElement();
	    		order.setValidity(validity);
	    		if (expireDate.getEnabled())
	    			order.setExpire(expireDate.getSelection());
	    	}

	    	if (!orderReference.getText().equals(""))
	    		order.setReference(orderReference.getText());

			IBroker connector = (IBroker) ((IStructuredSelection) brokerCombo.getSelection()).getFirstElement();
	    	IOrderMonitor tracker = connector.prepareOrder(order);
	    	tracker.submit();

	    	super.okPressed();
		} catch(Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error submitting order", e);
			Activator.log(status);
			ErrorDialog.openError(getShell(), getShell().getText(), null, status);
		}
    }

	protected IRepositoryService getRepositoryService() {
		IRepositoryService service = null;
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
		if (serviceReference != null) {
			service = (IRepositoryService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}

	protected ITradingService getTradingService() {
		ITradingService service = null;
		BundleContext context = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
		if (serviceReference != null) {
			service = (ITradingService) context.getService(serviceReference);
			context.ungetService(serviceReference);
		}
		return service;
	}

	protected String getSecuritySymbol(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null)
			return null;
		return identifier.getSymbol();
	}

	protected void handleBrokerSelection(IStructuredSelection selection) {
		IBroker connector = (IBroker) selection.getFirstElement();

		if (!symbol.getText().equals("")) {
			security = connector.getSecurityFromSymbol(symbol.getText());
    		symbolDescription.setText(security.getName().replaceAll("&", "&&"));
		}

		IOrderSide[] sides =  connector.getAllowedSides();
		sideCombo.setInput(sides);
		if (sideCombo.getSelection().isEmpty())
			sideCombo.setSelection(new StructuredSelection(sides[0]));

    	IOrderType[] types =  connector.getAllowedTypes();
    	typeCombo.setInput(types);
		if (typeCombo.getSelection().isEmpty())
			typeCombo.setSelection(new StructuredSelection(types[0]));

    	IOrderValidity[] validities = connector.getAllowedValidity();
    	validityCombo.setInput(validities);
		if (validityCombo.getSelection().isEmpty())
			validityCombo.setSelection(new StructuredSelection(validities[0]));

		IOrderRoute[] routes = connector.getAllowedRoutes();
		if (routes == null)
			routes = new IOrderRoute[0];
		routeCombo.setInput(routes);
		if (routes.length != 0 && routeCombo.getSelection().isEmpty())
			routeCombo.setSelection(new StructuredSelection(routes[0]));
	}
}
