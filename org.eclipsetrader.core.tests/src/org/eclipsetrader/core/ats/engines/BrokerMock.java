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

package org.eclipsetrader.core.ats.engines;

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


public class BrokerMock implements IBroker {

    public BrokerMock() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getId()
     */
    @Override
    public String getId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getName()
     */
    @Override
    public String getName() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#connect()
     */
    @Override
    public void connect() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#disconnect()
     */
    @Override
    public void disconnect() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader.core.trading.IOrder)
     */
    @Override
    public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getOrders()
     */
    @Override
    public IOrderMonitor[] getOrders() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
     */
    @Override
    public IOrderType[] getAllowedTypes() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
     */
    @Override
    public IOrderSide[] getAllowedSides() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
     */
    @Override
    public IOrderValidity[] getAllowedValidity() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
     */
    @Override
    public IOrderRoute[] getAllowedRoutes() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean canTrade(ISecurity security) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang.String)
     */
    @Override
    public ISecurity getSecurityFromSymbol(String symbol) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public String getSymbolFromSecurity(ISecurity security) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void addOrderChangeListener(IOrderChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    @Override
    public void removeOrderChangeListener(IOrderChangeListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
     */
    @Override
    public IAccount[] getAccounts() {
        return null;
    }

}
