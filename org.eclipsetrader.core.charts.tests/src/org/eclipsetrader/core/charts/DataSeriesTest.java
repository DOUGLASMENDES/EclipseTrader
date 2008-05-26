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

package org.eclipsetrader.core.charts;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;

public class DataSeriesTest extends TestCase {
	private IAdaptable[] sampleValues = new IAdaptable[] {
		new Value(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
		new Value(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
		new Value(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
	};

	public void testGetFirst() throws Exception {
		DataSeries series = new DataSeries("Test", sampleValues);
		assertSame(sampleValues[0], series.getFirst());
    }

	public void testGetLast() throws Exception {
		DataSeries series = new DataSeries("Test", sampleValues);
		assertSame(sampleValues[2], series.getLast());
    }

	public void testGetHighest() throws Exception {
		DataSeries series = new DataSeries("Test", sampleValues);
		assertSame(sampleValues[1], series.getHighest());
    }

	public void testGetLowest() throws Exception {
		DataSeries series = new DataSeries("Test", sampleValues);
		assertSame(sampleValues[2], series.getLowest());
    }

	public void testGetSeries() throws Exception {
		IAdaptable[] sampleValues = new IAdaptable[] {
				new Value(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
				new Value(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
				new Value(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
				new Value(getTime(14, Calendar.NOVEMBER, 2007), 15.0),
				new Value(getTime(15, Calendar.NOVEMBER, 2007), 10.0),
			};
		DataSeries series = new DataSeries("Test", sampleValues);
		IDataSeries subSeries = series.getSeries(sampleValues[1], sampleValues[3]);
		IAdaptable[] values = subSeries.getValues();
		assertEquals(3, values.length);
		assertSame(sampleValues[1], values[0]);
		assertSame(sampleValues[2], values[1]);
		assertSame(sampleValues[3], values[2]);
    }

	private class Value implements IAdaptable {
		private Date date;
		private Double value;

		public Value(Date date, Double value) {
		    this.date = date;
		    this.value = value;
	    }

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
		@SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
    		if (date != null && adapter.isAssignableFrom(date.getClass()))
    			return date;
    		if (value != null && adapter.isAssignableFrom(value.getClass()))
    			return value;
	        return null;
        }
	}

	private Date getTime(int day, int month, int year) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
}
