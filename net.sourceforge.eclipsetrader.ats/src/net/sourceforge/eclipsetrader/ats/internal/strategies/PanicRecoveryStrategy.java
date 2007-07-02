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

package net.sourceforge.eclipsetrader.ats.internal.strategies;

import net.sourceforge.eclipsetrader.ats.core.BaseComponent;
import net.sourceforge.eclipsetrader.ats.core.IComponentContext;
import net.sourceforge.eclipsetrader.ats.core.SignalSide;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.core.db.Order;

public class PanicRecoveryStrategy extends BaseComponent implements IBarListener {
	double percent = 5;

	int quantity = 100;

	Order buyOrder;

	public PanicRecoveryStrategy() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#start(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	@Override
	public void start(IComponentContext context) {
		super.start(context);
		context.addBarListener(this);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#stop(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	@Override
	public void stop(IComponentContext context) {
		context.removeBarListener(this);
		super.stop(context);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barOpen(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barOpen(BarEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barClose(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barClose(BarEvent e) {
		if (!hasPosition()) {
			if (buyOrder != null)
				buyOrder.cancelRequest();

			double buyPrice = e.bar.getClose() * (1 - (percent / 100));
			buyOrder = createLimitOrder(SignalSide.BUY, quantity, buyPrice);
			buyOrder.setText("Panic Recovery - Buy");
			buyOrder.sendNew();
		} else {
			Order sellOrder = createMarketOrder(SignalSide.SELL, quantity);
			sellOrder.setText("Panic Recovery - Sell");
			sellOrder.sendNew();
		}
	}
}
