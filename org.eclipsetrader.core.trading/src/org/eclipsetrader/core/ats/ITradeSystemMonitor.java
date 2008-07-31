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

/**
 * Monitors a running trade system instance.
 *
 * @since 1.0
 */
public interface ITradeSystemMonitor {

	/**
	 * Gets the monitored trade strategy.
	 *
	 * @return the trade strategy.
	 */
	public ITradeStrategy getTradeStrategy();

	/**
	 * Gets the context associated with the running strategy.
	 *
	 * @return the context.
	 */
	public ITradeSystemContext getTradeSystemContext();

	/**
	 * Stops the running strategy.
	 */
	public void stop();
}
