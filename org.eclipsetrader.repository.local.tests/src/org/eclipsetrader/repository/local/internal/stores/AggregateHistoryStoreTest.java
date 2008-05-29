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

package org.eclipsetrader.repository.local.internal.stores;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.StoreProperties;

public class AggregateHistoryStoreTest extends TestCase {

	public void testToURI() throws Exception {
		AggregateHistoryStore store = new AggregateHistoryStore(1, null) {
            @Override
            protected File getFile() {
	            return new File("test.xml");
            }

            @Override
        	@SuppressWarnings("unchecked")
            protected void marshal(Object object, Class clazz, File file) {
            }
		};
		StoreProperties storeProperties = new StoreProperties();
		storeProperties.setProperty(IPropertyConstants.TIME_SPAN, TimeSpan.minutes(1));
		storeProperties.setProperty(IPropertyConstants.BARS, new IOHLC[] {
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
			});
		store.putProperties(storeProperties, null);
		assertEquals("local:securities/history/20080522/1min#1", store.toURI().toString());
    }

	private Date getTime(int year, int month, int day, int hour, int minute) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, hour, minute, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}
}
