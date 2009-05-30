/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;

public class Helper {

	private Helper() {
	}

	public static IOHLC[] dailyHistory(int days) {
		List<IOHLC> list = new ArrayList<IOHLC>(days);

		double close = 30.0 + Math.random() * 50.0;
		double volatility = close / 100.0 * 20.0;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		for (int i = 0; i < days; i++) {
			while (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
				c.add(Calendar.DATE, -1);

			Double v = new Double(Math.random() * 100);

			double open;
			if ((v.intValue() & 1) == 0) {
				open = close + Math.random() * volatility / 2.0;
			}
			else {
				open = close - Math.random() * volatility / 2.0;
			}
			double high = open + Math.random() * volatility / 2.0;
			double low = open - Math.random() * volatility / 2.0;
			close = low + Math.random() * volatility;

			list.add(0, new OHLC(c.getTime(), open, high, low, close, 0L));

			c.add(Calendar.DATE, -1);
		}

		return list.toArray(new IOHLC[list.size()]);
	}
}
