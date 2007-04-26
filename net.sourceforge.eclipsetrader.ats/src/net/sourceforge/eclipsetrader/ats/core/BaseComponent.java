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

import net.sourceforge.eclipsetrader.core.db.Order;

public class BaseComponent implements IComponent {
	IComponentContext context;

	public BaseComponent() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#start(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	public void start(IComponentContext context) {
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#stop(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	public void stop(IComponentContext context) {
	}

	public boolean hasPosition() {
		return context.getPosition() != 0;
	}

	public Order createMarketOrder(SignalSide side, int quantity) {
		return context.createOrder(new Signal(context.getSecurity(), SignalType.MARKET, side, quantity, 0));
	}

	public Order createLimitOrder(SignalSide side, int quantity, double price) {
		return context.createOrder(new Signal(context.getSecurity(), SignalType.LIMIT, side, quantity, price));
	}
}
