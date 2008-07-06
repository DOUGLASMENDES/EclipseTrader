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

public interface IBrokerConnector {

	public String getId();

	public String getName();

	public void connect();

	public void disconnect();

	public void submitOrder(IOrder order) throws BrokerException;

	public void cancelOrder(IOrder order) throws BrokerException;

	public boolean allowModify();

	public void modifyOrder(IOrder order) throws BrokerException;

	public IOrder[] getOrders();

	public OrderType[] getAllowedTypes();

	public OrderSide[] getAllowedSides();

	public OrderValidity[] getAllowedValidity();
}
