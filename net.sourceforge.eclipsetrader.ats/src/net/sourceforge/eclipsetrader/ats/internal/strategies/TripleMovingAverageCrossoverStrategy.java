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

public class TripleMovingAverageCrossoverStrategy extends BaseComponent implements IBarListener {
	int shortPeriod = 4;

	int middlePeriod = 9;

	int longPeriod = 18;

	int quantity = 100;

	double shortValue = 0;

	double previousShortValue = 0;

	double middleValue = 0;

	double previousMiddleValue = 0;

	double longValue = 0;

	double previousLongValue = 0;

	int collectedValues = 0;

	public TripleMovingAverageCrossoverStrategy() {
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
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barOpen(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barOpen(BarEvent e) {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barClose(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
	 */
	public void barClose(BarEvent e) {
		if (collectedValues >= longPeriod) {
			previousShortValue = shortValue;
			previousMiddleValue = middleValue;
			previousLongValue = longValue;
		}

		updateAverages(e.price);

		if (collectedValues >= longPeriod) {
			if (!hasPosition()) {
				if (previousMiddleValue >= previousShortValue && middleValue < shortValue) {
					Order order = createMarketOrder(SignalSide.BUY, quantity);
					order.setText("Buy at Triple-MA crossdown");
					order.sendNew();
				}
			}
			else {
				if (previousMiddleValue <= previousLongValue && middleValue > longValue) {
					Order order = createMarketOrder(SignalSide.SELL, quantity);
					order.setText("Sell at Triple-MA crossup");
					order.sendNew();
				}
			}
		}
	}

	void updateAverages(double price) {
		if (collectedValues < shortPeriod) {
			shortValue += price;
			if ((collectedValues + 1) >= shortPeriod)
				shortValue /= shortPeriod;
		}
		else {
			double smoother = 2.0 / (shortPeriod + 1);
			shortValue = (smoother * (price - shortValue)) + shortValue;
		}

		if (collectedValues < middlePeriod) {
			middleValue += price;
			if ((collectedValues + 1) >= middlePeriod)
				middleValue /= middlePeriod;
		}
		else {
			double smoother = 2.0 / (middlePeriod + 1);
			middleValue = (smoother * (price - middleValue)) + middleValue;
		}

		if (collectedValues < longPeriod) {
			longValue += price;
			if ((collectedValues + 1) >= longPeriod)
				longValue /= longPeriod;
		}
		else {
			double smoother = 2.0 / (longPeriod + 1);
			longValue = (smoother * (price - longValue)) + longValue;
		}

		collectedValues++;
	}
}
