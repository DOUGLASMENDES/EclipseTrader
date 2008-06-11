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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipsetrader.ui.charts.DateValuesAxis;

public class DateValuesAxisTest extends TestCase {

	public void testComputeSize() throws Exception {
		DateValuesAxis axis = new DateValuesAxis();
		assertEquals(0, axis.computeSize(SWT.DEFAULT));
		axis.addValues(new Object[] {
				getTime(11, Calendar.NOVEMBER, 2007),
				getTime(12, Calendar.NOVEMBER, 2007),
			});
		assertEquals(10, axis.computeSize(SWT.DEFAULT));
    }

	public void testMapToAxis() throws Exception {
		DateValuesAxis axis = new DateValuesAxis();
		axis.addValues(new Object[] {
				getTime(11, Calendar.NOVEMBER, 2007),
				getTime(12, Calendar.NOVEMBER, 2007),
			});
		assertEquals(2, axis.mapToAxis(getTime(11, Calendar.NOVEMBER, 2007)));
		assertEquals(7, axis.mapToAxis(getTime(12, Calendar.NOVEMBER, 2007)));
    }

	public void testMapToValue() throws Exception {
		DateValuesAxis axis = new DateValuesAxis();
		axis.addValues(new Object[] {
				getTime(11, Calendar.NOVEMBER, 2007),
				getTime(12, Calendar.NOVEMBER, 2007),
			});
		assertEquals(getTime(11, Calendar.NOVEMBER, 2007), axis.mapToValue(0));
		assertEquals(getTime(11, Calendar.NOVEMBER, 2007), axis.mapToValue(1));
		assertEquals(getTime(11, Calendar.NOVEMBER, 2007), axis.mapToValue(2));
		assertEquals(getTime(11, Calendar.NOVEMBER, 2007), axis.mapToValue(3));
		assertEquals(getTime(11, Calendar.NOVEMBER, 2007), axis.mapToValue(4));
		assertEquals(getTime(12, Calendar.NOVEMBER, 2007), axis.mapToValue(5));
		assertEquals(getTime(12, Calendar.NOVEMBER, 2007), axis.mapToValue(6));
    }

	private Date getTime(int day, int month, int year) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
}
