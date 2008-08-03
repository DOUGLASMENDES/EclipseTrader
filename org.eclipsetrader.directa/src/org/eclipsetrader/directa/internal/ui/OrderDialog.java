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

package org.eclipsetrader.directa.internal.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.BrokerConnector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class OrderDialog extends TitleAreaDialog {
	private ComboViewer sideCombo;
	private ComboViewer securityCombo;
	private Text quantity;
	private Text price;
	private Button validity;
	private ComboViewer routeCombo;

	private ISecurity security;
	private NumberFormat numberFormat;
	private NumberFormat priceFormat;

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
    	Composite composite = (Composite) super.createDialogArea(parent);
    	createContent(composite);
    	return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
	    Control control = super.createContents(parent);

		setTitle("Directa Trade Order");
		setMessage("Fill the form to send a trade order to Directa.");
	    getButton(OK).setEnabled(isValid());

	    return control;
    }

	protected void createContent(Composite parent) {
    	Composite content = new Composite(parent, SWT.NONE);
    	content.setLayout(new GridLayout(2, false));
    	content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    	Label label = new Label(content, SWT.NONE);
    	label.setText("Side");
    	label.setLayoutData(new GridData(convertWidthInCharsToPixels(20), SWT.DEFAULT));
    	sideCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	sideCombo.setContentProvider(new ArrayContentProvider());
    	sideCombo.setLabelProvider(new LabelProvider());
    	sideCombo.setInput(new Object[] {
    			IOrderSide.Buy,
    			IOrderSide.Sell,
    		});
    	sideCombo.setSelection(new StructuredSelection(IOrderSide.Buy));

    	label = new Label(content, SWT.NONE);
    	label.setText("Instrument");
    	securityCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	securityCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    	securityCombo.setContentProvider(new ArrayContentProvider());
    	securityCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
            	ISecurity security = (ISecurity) element;
	            return NLS.bind("{0} ({1})", new Object[] {
	            		security.getName(),
	            		getSecuritySymbol(security),
	            	});
            }
    	});
    	securityCombo.setSorter(new ViewerSorter());

    	ISecurity[] securities = getRepositoryService().getSecurities();
    	securityCombo.setInput(getFilteredSecurities(securities));
    	securityCombo.setSelection(security != null ? new StructuredSelection(security) : StructuredSelection.EMPTY);

    	securityCombo.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	getButton(OK).setEnabled(isValid());
            }
    	});

    	label = new Label(content, SWT.NONE);
    	label.setText("Quantity");
    	quantity = new Text(content, SWT.BORDER);
    	quantity.setLayoutData(new GridData(convertWidthInCharsToPixels(15), SWT.DEFAULT));
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
    	label.setText("Price");
    	price = new Text(content, SWT.BORDER);
    	price.setLayoutData(new GridData(convertWidthInCharsToPixels(15), SWT.DEFAULT));
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
    	label.setText("Validity");
    	validity = new Button(content, SWT.CHECK);
    	validity.setText("30 days");

    	label = new Label(content, SWT.NONE);
    	label.setText("Route");
    	routeCombo = new ComboViewer(content, SWT.BORDER | SWT.READ_ONLY | SWT.DROP_DOWN);
    	routeCombo.setContentProvider(new ArrayContentProvider());
    	routeCombo.setLabelProvider(new LabelProvider());
    	routeCombo.setSorter(new ViewerSorter());
    	routeCombo.setInput(new Object[] {
    			BrokerConnector.Immediate,
    			BrokerConnector.MTA,
    			BrokerConnector.CloseMTA,
    			BrokerConnector.Open,
    			BrokerConnector.AfterHours,
    		});
    	routeCombo.setSelection(new StructuredSelection(BrokerConnector.Immediate));

    	quantity.setFocus();
    }

	protected boolean isValid() {
		if (securityCombo.getSelection().isEmpty())
			return false;

		try {
	    	long quantity = numberFormat.parse(this.quantity.getText()).longValue();
	    	if (quantity <= 0)
	    		return false;
		} catch(Exception e) {
			return false;
		}

		try {
	    	double price = priceFormat.parse(this.price.getText()).doubleValue();
	    	if (price <= 0)
	    		return false;
		} catch(Exception e) {
			return false;
		}

		return true;
    }

	public void setSecurity(ISecurity security) {
    	this.security = security;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
		try {
	    	Order order = new Order(
	    			null,
	    			IOrderType.Limit,
	    			(IOrderSide) ((IStructuredSelection) sideCombo.getSelection()).getFirstElement(),
	    			(ISecurity) ((IStructuredSelection) securityCombo.getSelection()).getFirstElement(),
	    			numberFormat.parse(quantity.getText()).longValue(),
	    			priceFormat.parse(price.getText()).doubleValue(),
	    			(IOrderRoute) ((IStructuredSelection) routeCombo.getSelection()).getFirstElement()
	    		);

			BrokerConnector connector = BrokerConnector.getInstance();
	    	IOrderMonitor tracker = connector.prepareOrder(order);
	    	tracker.submit();

	    	super.okPressed();
		} catch(Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error submitting order", e);
			Activator.log(status);
			ErrorDialog.openError(getShell(), getShell().getText(), null, status);
		}
    }

	protected ISecurity[] getFilteredSecurities(ISecurity[] list) {
		List<ISecurity> l = new ArrayList<ISecurity>();

		for (ISecurity security : list) {
			IFeedIdentifier identifier = (IFeedIdentifier) security.getAdapter(IFeedIdentifier.class);
			if (identifier != null) {
				String code = identifier.getSymbol();
				String isin = null;

				IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
				if (properties != null) {
					if (properties.getProperty(Activator.PROP_ISIN) != null)
						isin = properties.getProperty(Activator.PROP_ISIN);
					if (properties.getProperty(Activator.PROP_CODE) != null)
						code = properties.getProperty(Activator.PROP_CODE);
				}

				if (code != null && isin != null)
					l.add(security);
			}
		}

		Collections.sort(l, new Comparator<ISecurity>() {
            public int compare(ISecurity o1, ISecurity o2) {
	            return o1.getName().compareToIgnoreCase(o2.getName());
            }
		});

		return l.toArray(new ISecurity[l.size()]);
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

	protected String getSecuritySymbol(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null)
			return null;

		String symbol = identifier.getSymbol();
		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			if (properties.getProperty(Activator.PROP_CODE) != null)
				symbol = properties.getProperty(Activator.PROP_CODE);
		}

		return symbol;
	}
}
