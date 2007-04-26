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

public class FourDownDaysStrategy extends BaseComponent implements IBarListener {
	int count;

	double prevClose;

	int quantity = 100;

	IComponentContext context;

	public FourDownDaysStrategy() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#start(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	public void start(IComponentContext context) {
		this.context = context;

		prevClose = -1;
		count = 0;

		context.addBarListener(this);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#stop(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	public void stop(IComponentContext context) {
		context.removeBarListener(this);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barClose(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barClose(BarEvent e) {
		if (prevClose == -1) {
			prevClose = e.bar.getClose();
			return;
		}

		if (!hasPosition()) {
			if (prevClose > e.bar.getClose())
				count++;
			else
				count = 0;

			if (count == 4) {
				Order order = createMarketOrder(SignalSide.BUY, quantity);
				order.setText("Four Down Days - Buy");
				order.sendNew();
			}
		} else {
			Order order = createMarketOrder(SignalSide.SELL, quantity);
			order.setText("Four Down Days - Sell");
			order.sendNew();
		}

		prevClose = e.bar.getClose();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barOpen(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barOpen(BarEvent e) {
	}
}
