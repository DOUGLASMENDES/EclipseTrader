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

package org.eclipsetrader.core.internal.markets;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class MarketHolidayTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalDate() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketHoliday object = new MarketHoliday(date.getTime(), null);
        assertEquals(prefix + "<day date=\"2007-11-06\"/>", marshal(object));
    }

    public void testUnmarshalDate() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day date=\"2007-11-06\"/>");
        assertEquals("2007-11-06", new DateAdapter().marshal(object.getDate()));
    }

    public void testMarshalDescription() throws Exception {
        MarketHoliday object = new MarketHoliday(null, "Holiday description");
        assertEquals(prefix + "<day>Holiday description</day>", marshal(object));
    }

    public void testUnmarshalDescription() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day>Holiday description</day>");
        assertEquals("Holiday description", object.getDescription());
    }

    public void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketHoliday object = new MarketHoliday(null, null, date.getTime(), null);
        assertEquals(prefix + "<day open=\"10:00\"/>", marshal(object));
    }

    public void testUnmarshalOpenTime() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day open=\"10:00\"/>");
        assertEquals("10:00", new TimeAdapter().marshal(object.getOpenTime()));
    }

    public void testGetCombinedDateAndOpenTime() throws Exception {
        MarketHoliday object = new MarketHoliday(getTime(2007, 11, 1), "Test", getTime(10, 0), getTime(16, 0));
        assertEquals(getTime(2007, 11, 1, 10, 0), object.getOpenTime());
    }

    public void testGetCombinedDateAndCloseTime() throws Exception {
        MarketHoliday object = new MarketHoliday(getTime(2007, 11, 1), "Test", getTime(10, 0), getTime(16, 0));
        assertEquals(getTime(2007, 11, 1, 16, 0), object.getCloseTime());
    }

    public void testMarshalCloseTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 30, 0);
        MarketHoliday object = new MarketHoliday(null, null, null, date.getTime());
        assertEquals(prefix + "<day close=\"10:30\"/>", marshal(object));
    }

    public void testUnmarshalCloseTime() throws Exception {
        MarketHoliday object = unmarshal(prefix + "<day close=\"10:30\"/>");
        assertEquals("10:30", new TimeAdapter().marshal(object.getCloseTime()));
    }

    private String marshal(MarketHoliday object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketHoliday unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketHoliday.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketHoliday) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
