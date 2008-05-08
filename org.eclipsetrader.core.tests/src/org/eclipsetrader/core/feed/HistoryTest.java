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

package org.eclipsetrader.core.feed;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.Security;

public class HistoryTest extends TestCase {

	public void testGetFirst() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(13, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(12, Calendar.NOVEMBER, 2007), 100.0, 110.0, 90.0, 95.0, 200000L),
				new OHLC(getTime(11, Calendar.NOVEMBER, 2007), 400.0, 410.0, 390.0, 395.0, 300000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[2], history.getFirst());
    }

	public void testGetLast() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(13, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(12, Calendar.NOVEMBER, 2007), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(11, Calendar.NOVEMBER, 2007), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[0], history.getLast());
    }

	public void testGetHighest() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(13, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(12, Calendar.NOVEMBER, 2007), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(11, Calendar.NOVEMBER, 2007), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[2], history.getHighest());
    }

	public void testGetLowest() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(13, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(12, Calendar.NOVEMBER, 2007), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(11, Calendar.NOVEMBER, 2007), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[1], history.getLowest());
    }

	public void testGetSubset() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(11, Calendar.NOVEMBER, 2007), 400.0, 410.0, 390.0, 395.0, 100000L),
				new OHLC(getTime(12, Calendar.NOVEMBER, 2007), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(13, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(14, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(15, Calendar.NOVEMBER, 2007), 200.0, 210.0, 190.0, 195.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		IHistory subset = history.getSubset(getTime(12, Calendar.NOVEMBER, 2007), getTime(14, Calendar.NOVEMBER, 2007));
		assertEquals(3, subset.getOHLC().length);
		assertSame(bars[1], subset.getOHLC()[0]);
		assertSame(bars[2], subset.getOHLC()[1]);
		assertSame(bars[3], subset.getOHLC()[2]);
    }

	private Date getTime(int day, int month, int year) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
}
