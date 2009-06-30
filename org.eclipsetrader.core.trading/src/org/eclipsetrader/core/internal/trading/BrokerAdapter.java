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

package org.eclipsetrader.core.internal.trading;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerAdapter extends XmlAdapter<String, IBroker> {

	public class FailsafeBroker implements IBroker {
		private String id;

		public FailsafeBroker(String id) {
			this.id = id;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getId()
		 */
		public String getId() {
			return id;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getName()
		 */
		public String getName() {
			return id;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core.instruments.ISecurity)
		 */
		public boolean canTrade(ISecurity security) {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#connect()
		 */
		public void connect() {
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#disconnect()
		 */
		public void disconnect() {
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
		 */
		public IOrderSide[] getAllowedSides() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
		 */
		public IOrderType[] getAllowedTypes() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
		 */
		public IOrderValidity[] getAllowedValidity() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
		 */
		public IOrderRoute[] getAllowedRoutes() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getOrders()
		 */
		public IOrderMonitor[] getOrders() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader.core.trading.IOrder)
		 */
		public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang.String)
		 */
		public ISecurity getSecurityFromSymbol(String symbol) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
		 */
		public String getSymbolFromSecurity(ISecurity security) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
		 */
		public void addOrderChangeListener(IOrderChangeListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
		 */
		public void removeOrderChangeListener(IOrderChangeListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
		 */
		public IAccount[] getAccounts() {
			return new IAccount[0];
		}
	}

	public BrokerAdapter() {
	}

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal(IBroker v) throws Exception {
		return v != null ? v.getId() : null;
	}

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public IBroker unmarshal(String v) throws Exception {
		if (v == null)
			return null;

		IBroker connector = null;
		try {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());

			ITradingService tradingService = (ITradingService) context.getService(serviceReference);
			connector = tradingService.getBroker(v);

			context.ungetService(serviceReference);
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading feed service", e);
			Activator.log(status);
		}

		if (connector == null) {
			Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Failed to load broker " + v, null);
			Activator.log(status);
			return new FailsafeBroker(v);
		}

		return connector;
	}
}
