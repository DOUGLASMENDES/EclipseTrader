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

import org.eclipsetrader.ui.charts.HistogramAreaChart.Polygon;

import junit.framework.TestCase;

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
}
