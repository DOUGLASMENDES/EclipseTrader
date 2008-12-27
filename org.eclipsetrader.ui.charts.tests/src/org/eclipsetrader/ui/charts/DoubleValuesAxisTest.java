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

import junit.framework.TestCase;

import org.eclipsetrader.ui.charts.DoubleValuesAxis;

public class DoubleValuesAxisTest extends TestCase {

	public void testComputeSize() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		assertEquals(100, axis.computeSize(100));
	}

	public void testMapToAxis() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		axis.marginHeight = 0;
		axis.addValues(new Object[] { 0.0, 10.0, 20.0, 15.0, 30.0, 100.0 });
		axis.computeSize(100);
		assertEquals(90, axis.mapToAxis(10.0));
		assertEquals(70, axis.mapToAxis(30.0));
	}

	public void testMapToValue() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		axis.marginHeight = 0;
		axis.addValues(new Object[] { 0.0, 10.0, 20.0, 15.0, 30.0, 100.0 });
		axis.computeSize(100);
		assertEquals(10.0, axis.mapToValue(90));
		assertEquals(30.0, axis.mapToValue(70));
	}

	public void testMapToAxisWithMargin() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		axis.marginHeight = 5;
		axis.addValues(new Object[] { 0.0, 10.0, 20.0, 15.0, 30.0, 100.0 });
		axis.computeSize(110);
		assertEquals(90 + 5, axis.mapToAxis(10.0));
		assertEquals(70 + 5, axis.mapToAxis(30.0));
	}

	public void testMapToValueWithMargin() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		axis.marginHeight = 5;
		axis.addValues(new Object[] { 0.0, 10.0, 20.0, 15.0, 30.0, 100.0 });
		axis.computeSize(110);
		assertEquals(10.0, axis.mapToValue(90 + 5));
		assertEquals(30.0, axis.mapToValue(70 + 5));
	}

	public void testMapToAxisWithNullScale() throws Exception {
		DoubleValuesAxis axis = new DoubleValuesAxis();
		axis.marginHeight = 0;
		axis.computeSize(100);
		assertEquals(0, axis.mapToAxis(10.0));
	}
}
