/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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

public class DataSeriesTest extends TestCase {

    private IAdaptable[] sampleValues = new IAdaptable[] {
        new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
        new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
        new NumberValue(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
    };

    private Date getTime(int day, int month, int year) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

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
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 10.0),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 20.0),
            new NumberValue(getTime(13, Calendar.NOVEMBER, 2007), 5.0),
            new NumberValue(getTime(14, Calendar.NOVEMBER, 2007), 15.0),
            new NumberValue(getTime(15, Calendar.NOVEMBER, 2007), 10.0),
        };
        DataSeries series = new DataSeries("Test", sampleValues);
        IDataSeries subSeries = series.getSeries(sampleValues[1], sampleValues[3]);
        IAdaptable[] values = subSeries.getValues();
        assertEquals(3, values.length);
        assertSame(sampleValues[1], values[0]);
        assertSame(sampleValues[2], values[1]);
        assertSame(sampleValues[3], values[2]);
    }

    public void testCrossAbove() throws Exception {
        IAdaptable[] sampleValues1 = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 3.7692),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7794),
        };
        DataSeries series1 = new DataSeries("Test1", sampleValues1);

        IAdaptable[] sampleValues2 = new IAdaptable[] {
            new NumberValue(getTime(11, Calendar.NOVEMBER, 2007), 3.7762),
            new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7793),
        };
        DataSeries series2 = new DataSeries("Test2", sampleValues2);

        int result = series1.cross(series2, new NumberValue(getTime(12, Calendar.NOVEMBER, 2007), 3.7794));
        assertEquals(result, IDataSeries.ABOVE);
    }
}
