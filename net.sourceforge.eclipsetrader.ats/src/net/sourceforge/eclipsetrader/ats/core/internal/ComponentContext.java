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

import net.sourceforge.eclipsetrader.ats.core.IComponentContext;
import net.sourceforge.eclipsetrader.ats.core.IExecutionManager;
import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.Signal;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IMarketListener;
import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.Security;

/**
 * Default implementation of IComponentContext interface.
 */
public class ComponentContext implements IComponentContext {
	Security security;

	Bar[] bars;

	IMarketManager marketManager;

	IExecutionManager executionManager;

	public ComponentContext(Security security, Bar[] bars, IMarketManager marketManager, IExecutionManager executionManager) {
		this.security = security;
		this.bars = bars;
		this.marketManager = marketManager;
		this.executionManager = executionManager;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#addBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void addBarListener(IBarListener l) {
		marketManager.addBarListener(getSecurity(), l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#addMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void addMarketListener(IMarketListener l) {
		marketManager.addMarketListener(getSecurity(), l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#addOrderListener(net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void addOrderListener(IOrderListener listener) {
		executionManager.addOrderListener(security, listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#addPositionListener(net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void addPositionListener(IPositionListener listener) {
		executionManager.addPositionListener(security, listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#getBars()
	 */
	public Bar[] getBars() {
		return bars;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#getPosition()
	 */
	public int getPosition() {
		return executionManager.getPosition(security);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#getPositionValue()
	 */
	public double getPositionValue() {
		return executionManager.getPositionValue(security);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#getSecurity()
	 */
	public Security getSecurity() {
		return security;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#removeBarListener(net.sourceforge.eclipsetrader.ats.core.events.IBarListener)
	 */
	public void removeBarListener(IBarListener l) {
		marketManager.removeBarListener(getSecurity(), l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#removeMarketListener(net.sourceforge.eclipsetrader.ats.core.events.IMarketListener)
	 */
	public void removeMarketListener(IMarketListener l) {
		marketManager.removeMarketListener(getSecurity(), l);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#removeOrderListener(net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void removeOrderListener(IOrderListener listener) {
		executionManager.removeOrderListener(security, listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#removePositionListener(net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void removePositionListener(IPositionListener listener) {
		executionManager.removePositionListener(security, listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponentContext#createOrder(net.sourceforge.eclipsetrader.ats.core.Signal)
	 */
	public Order createOrder(Signal signal) {
		return executionManager.createOrder(signal);
	}
}
