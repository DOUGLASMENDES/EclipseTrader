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

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.GroupChartObject;

public class PatternChart extends GroupChartObject {

	public PatternChart(String name, IAdaptable[] values, int lookback, int[] outInteger) {
		for (int i = 0; i < outInteger.length; i++) {
			if (outInteger[i] == 0)
				continue;
			IOHLC[] outBars = new IOHLC[lookback];
			for (int o = 0; o < outBars.length; o++)
				outBars[o] = (IOHLC) values[i + o].getAdapter(IOHLC.class);
			add(new PatternBox(outBars, null, name, outInteger[i] > 0 ? "Bullish" : "Bearish"));
		}
	}
}
