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

import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Market managers are responsible to monitor the securities registered
 * by trading system strategies and generate events.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public interface IMarketManager {

	/**
	 * Adds a listener for bar events to this market manager.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a bar event listener
	 */
	public void addBarListener(IBarListener listener);

	/**
	 * Adds a listener for bar events related to a security to this market manager.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param security the security
	 * @param listener a bar event listener
	 */
	public void addBarListener(Security security, IBarListener l);

	/**
	 * Adds a listener for market events to this market manager.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a market event listener
	 */
	public void addMarketListener(IMarketListener l);

	/**
	 * Adds a listener for market events related to a security to this market manager.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param security the security
	 * @param listener a market event listener
	 */
	public void addMarketListener(Security security, IMarketListener l);

	/**
	 * Adds a security to the collection of securities managed by this
	 * market manager.
	 * 
	 * @param security the security
	 */
	public void addSecurity(Security security);

	/**
	 * Disposes of this market manager.
	 */
	public void dispose();

	/**
	 * Returns the bars currently available for a security.
	 * 
	 * @param security a security
	 * @return the available bars
	 */
	public Bar[] getBars(Security security);

	/**
	 * Removes a listener for bar events from this market manager.
	 *
	 * @param listener a bar event listener
	 */
	public void removeBarListener(IBarListener l);

	/**
	 * Removes a listener for bar events related to a security from this market manager.
	 *
	 * @param security the security
	 * @param listener a bar event listener
	 */
	public void removeBarListener(Security security, IBarListener l);

	/**
	 * Removes a listener for market events from this market manager.
	 *
	 * @param listener a bar event listener
	 */
	public void removeMarketListener(IMarketListener l);

	/**
	 * Removes a listener for market events related to a security from this market manager.
	 *
	 * @param security the security
	 * @param listener a market event listener
	 */
	public void removeMarketListener(Security security, IMarketListener l);

	/**
	 * Removes a security from the collection of securities managed by this
	 * market manager.
	 * 
	 * @param security the security
	 */
	public void removeSecurity(Security security);

	/**
	 * Starts the market manager.
	 */
	public void start();

	/**
	 * Stops the market manager.
	 */
	public void stop();
}
