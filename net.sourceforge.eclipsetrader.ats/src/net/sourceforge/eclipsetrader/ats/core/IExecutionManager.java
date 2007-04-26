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

package net.sourceforge.eclipsetrader.ats.core;

import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Interface for execution manager components.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public interface IExecutionManager {

	/**
	 * Starts the execution manager.
	 */
	public void start(Account account, ITradingProvider tradingProvider);

	/**
	 * Stops the execution manager.
	 */
	public void stop(Account account, ITradingProvider tradingProvider);

	/**
	 * Adds a listener to the collection of listeners that receive notifications when
	 * an order event is generated.
	 * 
	 * @param listener an order event listener.
	 * @see net.sourceforge.eclipsetrader.ats.core.events.OrderEvent
	 */
	public void addOrderListener(IOrderListener listener);

	/**
	 * Adds a listener to the collection of listeners that receive notifications when
	 * an order event for the given security is generated.
	 * 
	 * @param security the security.
	 * @param listener an order event listener.
	 * @see net.sourceforge.eclipsetrader.ats.core.events.OrderEvent
	 */
	public void addOrderListener(Security security, IOrderListener listener);

	/**
	 * Removes a listener from the collection of listeners that receive notifications when
	 * an order event is generated.
	 * 
	 * @param listener an order event listener.
	 * @see net.sourceforge.eclipsetrader.ats.core.events.OrderEvent
	 */
	public void removeOrderListener(IOrderListener listener);

	/**
	 * Removes a listener from the collection of listeners that receive notifications when
	 * an order event for the given security is generated.
	 * 
	 * @param security the security.
	 * @param listener an order event listener.
	 * @see net.sourceforge.eclipsetrader.ats.core.events.OrderEvent
	 */
	public void removeOrderListener(Security security, IOrderListener listener);

	public void addPositionListener(IPositionListener listener);

	public void addPositionListener(Security security, IPositionListener listener);

	public void removePositionListener(IPositionListener listener);

	public void removePositionListener(Security security, IPositionListener listener);

	/**
	 * Execute the signal.
	 * 
	 * @param signal the signal to execute.
	 */
	public void execute(Signal signal);

	public Order createOrder(Signal signal);

	public int getPosition(Security security);

	public double getPositionValue(Security security);

	//    public ITradingProvider getTradingProvider();

	public Order[] getOrders();
}