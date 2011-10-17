/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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
     * Gets a possibly empty array of allowed order routes.
     *
     * @return the allowed order routes.
     */
    public IOrderRoute[] getAllowedRoutes();

    /**
     * Check if the receiver can trade the given security.
     *
     * @param security the security to check.
     * @return <code>true</code> if the receive can trade the security.
     */
    public boolean canTrade(ISecurity security);

    /**
     * Gets the security that the receiver trades with the given symbol.
     *
     * @param symbol the symbol.
     * @return the security, or <code>null</code> if no securities can be traded with the given symbol.
     */
    public ISecurity getSecurityFromSymbol(String symbol);

    /**
     * Gets the symbol used by the receiver to trade the given security.
     *
     * @param security the security to trade.
     * @return the symbol used to trade.
     */
    public String getSymbolFromSecurity(ISecurity security);

    /**
     * Adds a listener to list of listeners that receive notifications about order changes.
     *
     * @param listener the listener to add.
     */
    public void addOrderChangeListener(IOrderChangeListener listener);

    /**
     * Removes a listener from the list of listeners that receive notifications about order changes.
     *
     * @param listener the listener to remove.
     */
    public void removeOrderChangeListener(IOrderChangeListener listener);

    /**
     * Gets the accounts managed by the receiver.
     * 
     * @return the possibly empty accounts array.
     */
    public IAccount[] getAccounts();
}
