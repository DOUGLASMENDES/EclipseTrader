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

package org.eclipsetrader.core.trading;

import org.eclipsetrader.core.instruments.ISecurity;

public interface IBrokerConnector {

	public String getId();

	public String getName();

	public void connect();

	public void disconnect();

	public IOrderMonitor prepareOrder(IOrder order) throws BrokerException;

	public IOrderMonitor[] getOrders();

	public OrderType[] getAllowedTypes();

	public OrderSide[] getAllowedSides();

	public OrderValidity[] getAllowedValidity();

	public boolean canTrade(ISecurity security);
}
