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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IOHLC;

/**
 * Implementation of an axis that holds <code>Double</code> values.
 *
 * @since 1.0
 */
public class DoubleValuesAxis implements IAxis {
	public int marginHeight = 5;

	private Double scaleHigh;
	private Double scaleLow;
	private int height;
	private double span;
	private double[] scaleList;

	public DoubleValuesAxis() {
		scaleList = new double[] {
				.0001, .0002, .0005, .001, .002, .005, .01, .02, .05,
				.1, .2, .5, 1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 100.0, 250.0, 500.0,
				1000.0, 2500.0, 5000.0, 10000.0, 25000.0, 50000.0, 100000.0, 250000.0, 500000.0,
				1000000.0, 2500000.0, 5000000.0, 10000000.0, 25000000.0, 50000000.0, 100000000.0, 250000000.0, 500000000.0,
				1000000000.0, 2500000000.0, 5000000000.0, 10000000000.0, 25000000000.0, 50000000000.0, 100000000000.0, 250000000000.0, 500000000000.0,
			};
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#addValues(java.lang.Object[])
     */
    public void addValues(Object[] values) {
    	for (Object v : values) {
    		Number highValue = null;
    		Number lowValue = null;

    		if (v instanceof Number)
    			highValue = lowValue = (Number) v;
    		if (v instanceof IAdaptable) {
    			IOHLC ohlc = (IOHLC) ((IAdaptable) v).getAdapter(IOHLC.class);
    			if (ohlc != null) {
    				highValue = ohlc.getHigh();
    				lowValue = ohlc.getLow();
    			}
    			else
    				highValue = lowValue = (Number) ((IAdaptable) v).getAdapter(Number.class);
    		}

    		if (highValue != null) {
        		if (scaleHigh == null || highValue.doubleValue() > scaleHigh)
        			scaleHigh = highValue.doubleValue();
    		}
    		if (lowValue != null) {
        		if (scaleLow == null || lowValue.doubleValue() < scaleLow)
        			scaleLow = lowValue.doubleValue();
    		}
    	}

		if (scaleHigh != null && scaleLow != null)
			span = (height - marginHeight * 2) / (scaleHigh - scaleLow);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#clear()
     */
    public void clear() {
    	scaleHigh = null;
    	scaleLow = null;
    	span = 0;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.IAxis#computeSize(int)
	 */
	public int computeSize(int preferredSize) {
		height = preferredSize;
		if (scaleHigh != null && scaleLow != null)
			span = (height - marginHeight * 2) / (scaleHigh - scaleLow);
		return height;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.IAxis#mapToAxis(java.lang.Object)
	 */
	public int mapToAxis(Object value) {
		Number n = null;

		if (value instanceof Number)
			n = (Number) value;
		if (value instanceof IAdaptable)
			n = (Number) ((IAdaptable) value).getAdapter(Number.class);

		double t = n.doubleValue() - scaleLow;
		int y = (int) (t * span);
		return height - y - marginHeight;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.IAxis#mapToValue(int)
	 */
	public Object mapToValue(int position) {
		if (height == 0)
			return 0;
		int p = height - position - marginHeight;
		return scaleLow + (p / span);
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getFirstValue()
     */
    public Object getFirstValue() {
	    return scaleLow;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getLastValue()
     */
    public Object getLastValue() {
	    return scaleHigh;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.IAxis#getValues()
     */
    public Object[] getValues() {
		if (scaleHigh == null || scaleLow == null)
			return new Object[0];

		int ticks;
		for (ticks = 2; (ticks * 15) < height; ticks++);
		ticks--;
		if (ticks > 10)
			ticks = 10;

		double range = scaleHigh - scaleLow;
		double interval = 0;
		int loop;
		for (loop = 0; loop < scaleList.length; loop++) {
			interval = scaleList[loop];
			if ((range / interval) < ticks)
				break;
		}

		loop = 0;
		double t = 0 - (ticks * interval);
		List<Double> scaleArray = new ArrayList<Double>();

		if (interval > 0) {
			while (t <= scaleHigh) {
				t = t + interval;

				if (t >= scaleLow) {
					scaleArray.add(new Double(t));
					loop++;
				}
			}
		}

		return scaleArray.toArray(new Double[scaleArray.size()]);
    }
}
