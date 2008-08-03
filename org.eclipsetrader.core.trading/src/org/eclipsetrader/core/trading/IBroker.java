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

/**
 * Brokers extension point interface.
 *
 * @since 1.0
 */
public interface IBroker {

	/**
	 * Gets the unique plugin id.
	 *
	 * @return the plugin id.
	 */
	public String getId();

	/**
	 * Gets the broker's display name.
	 *
	 * @return the name.
	 */
	public String getName();

	/**
	 * Connects to the remote server.
	 */
	public void connect();

	/**
	 * Disconnects from the remote server.
	 */
	public void disconnect();

	/**
	 * Prepare an order for submission.
	 * <p>The order is validated but not submitted to the broker. Clients
	 * use <code>IOrderMonitor.submit()</code> to actually submit the order.
	 *
	 * @param order the order to prepare.
	 * @return the <code>IOrderMonitor</code> instance.
	 * @throws BrokerException if the order can't be processed.
	 */
	public IOrderMonitor prepareOrder(IOrder order) throws BrokerException;

	/**
	 * Gets all order monitors managed by the receiver.
	 *
	 * @return the order monitors.
	 */
	public IOrderMonitor[] getOrders();

	/**
	 * Gets a possibly empty array of allowed order types.
	 *
	 * @return the allowed order types.
	 */
	public IOrderType[] getAllowedTypes();

	/**
	 * Gets a possibly empty array of allowed order sides.
	 *
	 * @return the allowed order sides.
	 */
	public IOrderSide[] getAllowedSides();

	/**
	 * Gets a possibly empty array of allowed order validities.
	 *
	 * @return the allowed order validities.
	 */
	public IOrderValidity[] getAllowedValidity();

	/**
	 * Check if the receiver can trade the given security.
	 *
	 * @param security the security to check.
	 * @return <code>true</code> if the receive can trade the security.
	 */
	public boolean canTrade(ISecurity security);
}
