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
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Order;

public class MovingAverageCrossoverStrategy extends BaseComponent implements IBarListener {
	int shortPeriod = 12;

	int longPeriod = 26;

	int quantity = 100;

	double previousLongValue = 0;

	double longValue = 0;

	double previousShortValue = 0;

	double shortValue = 0;

	int loop = 0;

	public MovingAverageCrossoverStrategy() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#start(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	@Override
	public void start(IComponentContext context) {
		super.start(context);

		Bar[] bars = context.getSecurity().getIntradayHistory().toArray();
		for (int i = 0; i < bars.length; i++)
			updateAverages(bars[i].getClose());

		context.addBarListener(this);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IComponent#stop(net.sourceforge.eclipsetrader.ats.core.IComponentContext)
	 */
	@Override
	public void stop(IComponentContext context) {
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
		if (loop >= longPeriod) {
			previousShortValue = shortValue;
			previousLongValue = longValue;
		}

		updateAverages(e.price);

		if (loop >= longPeriod) {
			if (!hasPosition()) {
				if (previousLongValue <= previousShortValue && longValue > shortValue) {
					Order order = createMarketOrder(SignalSide.BUY, quantity);
					order.setText("Buy at MA crossup");
					order.sendNew();
				}
			}
			else {
				if (previousLongValue >= previousShortValue && longValue < shortValue) {
					Order order = createMarketOrder(SignalSide.SELL, quantity);
					order.setText("Sell at MA crossdown");
					order.sendNew();
				}
			}
		}
	}

	void updateAverages(double price) {
		if (loop < shortPeriod) {
			shortValue += price;
			if ((loop + 1) >= shortPeriod)
				shortValue /= shortPeriod;
		}
		else {
			double smoother = 2.0 / (shortPeriod + 1);
			shortValue = (smoother * (price - shortValue)) + shortValue;
		}

		if (loop < longPeriod) {
			longValue += price;
			if ((loop + 1) >= longPeriod)
				longValue /= longPeriod;
		}
		else {
			double smoother = 2.0 / (longPeriod + 1);
			longValue = (smoother * (price - longValue)) + longValue;
		}

		loop++;
	}
}
