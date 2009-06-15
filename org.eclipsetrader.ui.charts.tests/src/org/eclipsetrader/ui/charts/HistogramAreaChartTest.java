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

package org.eclipsetrader.ui.charts;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.charts.HistogramAreaChart.Polygon;

public class HistogramAreaChartTest extends TestCase {

	public void testPolygonContainsPoint() throws Exception {
		HistogramAreaChart o = new HistogramAreaChart(null, null);
		Polygon p = o.new Polygon();
		p.setBounds(0, 5, 20, 10, 30);
		assertTrue(p.containsPoint(7, 10));
	}

	public void testPolygonContainsOutsidePoint() throws Exception {
		HistogramAreaChart o = new HistogramAreaChart(null, null);
		Polygon p = o.new Polygon();
		p.setBounds(0, 5, 20, 10, 30);
		assertFalse(p.containsPoint(7, 30));
	}

	public void testPolygonContainsPointWithDefaultY() throws Exception {
		HistogramAreaChart o = new HistogramAreaChart(null, null);
		Polygon p = o.new Polygon();
		p.setBounds(0, 5, 20, 10, 30);
		assertTrue(p.containsPoint(7, -1));
	}

	public void testGetTooltip() throws Exception {
		DateFormat dateFormat = DateFormat.getDateInstance();
		NumberFormat numberFormat = NumberFormat.getInstance();

		OHLC ohlc = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 0L);
		OHLCDataSeries dataSeries = new OHLCDataSeries("Test", new IOHLC[] {
			ohlc
		}, TimeSpan.days(1));

		HistogramAreaChart o = new HistogramAreaChart(dataSeries, null);

		String expected = dateFormat.format(ohlc.getDate()) + " O:" + numberFormat.format(ohlc.getOpen()) + " H:" + numberFormat.format(ohlc.getHigh()) + " L:" + numberFormat.format(ohlc.getLow()) + " C:" + numberFormat.format(ohlc.getHigh());
		assertEquals(expected, o.getToolTip());
	}

	public void testGetEmptySeriesTooltip() throws Exception {
		OHLCDataSeries dataSeries = new OHLCDataSeries("Test", new IOHLC[0], TimeSpan.days(1));
		HistogramAreaChart o = new HistogramAreaChart(dataSeries, null);
		assertEquals("Test", o.getToolTip());
	}

	public void testGetBarTooltip() throws Exception {
		DateFormat dateFormat = DateFormat.getDateInstance();
		NumberFormat numberFormat = NumberFormat.getInstance();

		OHLC ohlc = new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 0L);
		OHLCDataSeries dataSeries = new OHLCDataSeries("Test", new IOHLC[] {
			ohlc
		}, TimeSpan.days(1));

		HistogramAreaChart o = new HistogramAreaChart(dataSeries, null);
		Polygon p = o.new Polygon();
		p.setValue(new AdaptableWrapper(ohlc));

		String expected = dataSeries.getName() + "\r\nD:" + dateFormat.format(ohlc.getDate()) + "\r\nO:" + numberFormat.format(ohlc.getOpen()) + "\r\nH:" + numberFormat.format(ohlc.getHigh()) + "\r\nL:" + numberFormat.format(ohlc.getLow()) + "\r\nC:" + numberFormat.format(ohlc.getHigh());;
		assertEquals(expected, p.getToolTip());
	}
}
