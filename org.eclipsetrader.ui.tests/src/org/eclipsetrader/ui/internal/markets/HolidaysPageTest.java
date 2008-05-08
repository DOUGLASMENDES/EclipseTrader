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

package org.eclipsetrader.ui.internal.markets;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketHoliday;
import org.eclipsetrader.core.internal.markets.MarketTime;

public class HolidaysPageTest extends TestCase {
	private Shell shell;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getCurrent());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	public void testIsValid() throws Exception {
		HolidaysPage page = new HolidaysPage();
		page.createContents(shell);
		assertTrue(page.isValid());
    }

	public void testFillTableFromElement() throws Exception {
		HolidaysPage page = new HolidaysPage();
		final Market market = new Market("Market", Arrays.asList(new MarketTime[] { new MarketTime(getTime(9, 30), getTime(16, 0)) }));
	    market.setHolidays(new MarketHoliday[] {
	    		new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday 1"),
	    		new MarketHoliday(getTime(2008, Calendar.JANUARY, 1), "Holiday 2"),
	    	});
		page.setElement(new IAdaptable() {
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
            	if (adapter.isAssignableFrom(Market.class))
            		return market;
	            return null;
            }
		});
		page.createContents(shell);
		assertEquals(2, page.viewer.getTable().getItemCount());
	}

	private Date getTime(int hour, int minute) {
	    Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, hour);
		date.set(Calendar.MINUTE, minute);
		date.set(Calendar.SECOND, 0);
		return date.getTime();
	}

	private Date getTime(int year, int month, int day) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
}
