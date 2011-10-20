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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class HistoryDataEditorModelTest extends TestCase {

    @Override
    public void run(final TestResult result) {
        Display display = Display.getDefault();
        Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {

            @Override
            public void run() {
                HistoryDataEditorModelTest.super.run(result);
            }
        });
    }

    public void testInitialSettings() throws Exception {
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));
        assertEquals(1, model.getList().size());
        assertTrue(((HistoryDataElement) model.getList().get(0)).isEmpty());
    }

    public void testSet() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));

        model.set(bars);

        assertEquals(4, model.getList().size());
    }

    public void testAddNewEmptyElementWhenLastIsEdited() throws Exception {
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));

        HistoryDataElement lastElement = model.getLastElement();
        lastElement.setDate(getTime(2007, Calendar.NOVEMBER, 14));

        assertEquals(2, model.getList().size());
        assertNotSame(lastElement, model.getLastElement());
        assertNotNull(model.getLastElement());
    }

    public void testDontAddNewEmptyElementTwice() throws Exception {
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));

        HistoryDataElement lastElement = model.getLastElement();
        lastElement.setDate(getTime(2007, Calendar.NOVEMBER, 14));
        lastElement.setClose(1.0);

        assertEquals(2, model.getList().size());
    }

    public void testSetAndAddNewElement() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));

        model.set(bars);

        HistoryDataElement lastElement = model.getLastElement();
        lastElement.setDate(getTime(2007, Calendar.NOVEMBER, 14));

        assertEquals(5, model.getList().size());
    }

    public void testToOHLC() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        HistoryDataEditorModel model = new HistoryDataEditorModel(TimeSpan.days(1));

        model.set(bars);

        IOHLC[] newBars = model.toOHLC();

        assertEquals(bars.length, newBars.length);
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
