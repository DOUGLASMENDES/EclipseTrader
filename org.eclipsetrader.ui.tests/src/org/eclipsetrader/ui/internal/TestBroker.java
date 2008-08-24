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

package org.eclipsetrader.ui.internal;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;

public class TestBroker implements IBroker {
	private String id;
	private String name;

	public TestBroker(String id, String name) {
		this.id = id;
		this.name = name;
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
	 * @see org.eclipsetrader.core.trading.IBroker#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getName()
	 */
	public String getName() {
		return name;
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
}
