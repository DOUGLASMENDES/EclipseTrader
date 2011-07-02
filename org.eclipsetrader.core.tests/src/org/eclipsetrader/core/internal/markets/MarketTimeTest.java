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

public class MarketTimeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testIsExcludedWithEmptyDates() throws Exception {
        MarketTime object = new MarketTime(null, null, "Test");
        assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
    }

    public void testIsExcluded() throws Exception {
        MarketTime object = new MarketTime(null, null, "Test");
        object.setExclude(new MarketTimeExclude[] {
            new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6))
        });
        assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
        assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 6)));
        assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 7)));
    }

    public void testIsExcludedRange() throws Exception {
        MarketTime object = new MarketTime(null, null, "Test");
        object.setExclude(new MarketTimeExclude[] {
            new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6), getTime(2007, Calendar.NOVEMBER, 8))
        });
        assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 5)));
        assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 6)));
        assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 7)));
        assertTrue(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 8)));
        assertFalse(object.isExcluded(getTime(2007, Calendar.NOVEMBER, 9)));
    }

    public void testMarshalDescription() throws Exception {
        MarketTime object = new MarketTime(null, null, "Test");
        assertEquals(prefix + "<time description=\"Test\"/>", marshal(object));
    }

    public void testUnmarshalDescription() throws Exception {
        MarketTime object = unmarshal(prefix + "<time description=\"Test\"/>");
        assertEquals("Test", object.getDescription());
    }

    public void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        MarketTime object = new MarketTime(date.getTime(), null, null);
        assertEquals(prefix + "<time open=\"10:00\"/>", marshal(object));
    }

    public void testUnmarshalOpenTime() throws Exception {
        MarketTime object = unmarshal(prefix + "<time open=\"10:00\"/>");
        assertEquals("10:00", new TimeAdapter().marshal(object.getOpenTime()));
    }

    public void testMarshalCloseTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 30, 0);
        MarketTime object = new MarketTime(null, date.getTime(), null);
        assertEquals(prefix + "<time close=\"10:30\"/>", marshal(object));
    }

    public void testUnmarshalCloseTime() throws Exception {
        MarketTime object = unmarshal(prefix + "<time close=\"10:30\"/>");
        assertEquals("10:30", new TimeAdapter().marshal(object.getCloseTime()));
    }

    private String marshal(MarketTime object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketTime unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketTime.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketTime) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
