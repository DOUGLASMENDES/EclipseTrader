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

package org.eclipsetrader.core.ats;

import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBrokerConnector;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.OrderSide;

/**
 * Defines the contex associated with running trade strategies.
 *
 * @since 1.0
 */
public interface ITradeSystemContext {

	/**
	 * Gets the security to trade.
	 *
	 * @return the security.
	 */
	public ISecurity getSecurity();

	/**
	 * Gets the pricing environment instance.
	 *
	 * @return the pricing environment.
	 */
	public IPricingEnvironment getPricingEnvironment();

	/**
	 * Gets the bar factory instance.
	 *
	 * @return the bar factory.
	 */
	public IBarFactory getBarFactory();

	/**
	 * Gets the broker.
	 *
	 * @return the broker.
	 */
	public IBrokerConnector getBroker();

	/**
	 * Gets the trade strategy parameters.
	 *
	 * @return the trade strategy parameters.
	 */
	public ITradeStrategyParameters getStrategyParameters();

	/**
	 * Prepare a market-type order.
	 *
	 * @param side the order side.
	 * @param quantity the quantity to trade.
	 * @return the order monitor instance.
	 *
	 * @throws BrokerException
	 */
	public IOrderMonitor prepareOrder(OrderSide side, Long quantity) throws BrokerException;

	/**
	 * Prepare a limit-type order.
	 *
	 * @param side the order side.
	 * @param quantity the quantity to trade.
	 * @param price the limit price.
	 * @return the order monitor instance.
	 *
	 * @throws BrokerException
	 */
	public IOrderMonitor prepareOrder(OrderSide side, Long quantity, Double price) throws BrokerException;
}
