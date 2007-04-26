/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.core.internal;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderStatus;

public class NullTradingProvider implements ITradingProvider {

	public NullTradingProvider() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getRoutes()
	 */
	public List getRoutes() {
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getSides()
	 */
	public List getSides() {
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getTypes()
	 */
	public List getTypes() {
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getValidity()
	 */
	public List getValidity() {
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendCancelRequest(net.sourceforge.eclipsetrader.core.db.Order)
	 */
	public void sendCancelRequest(Order order) {
		order.setStatus(OrderStatus.CANCELED);
		order.notifyObservers();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendNew(net.sourceforge.eclipsetrader.core.db.Order)
	 */
	public void sendNew(Order order) {
		order.setProvider(this);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendReplaceRequest(net.sourceforge.eclipsetrader.core.db.Order)
	 */
	public void sendReplaceRequest(Order order) {
		order.notifyObservers();
	}
}
