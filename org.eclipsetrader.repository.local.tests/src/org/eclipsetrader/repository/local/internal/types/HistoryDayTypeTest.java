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

package org.eclipsetrader.repository.local.internal.types;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.TimeSpan;

public class HistoryDayTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        HistoryDayType object = new HistoryDayType();
        assertEquals(prefix + "<day/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        HistoryDayType object = unmarshal(prefix + "<day/>");
        assertNotNull(object);
        assertEquals(0, object.getPeriods().length);
    }

    public void testMarshalDate() throws Exception {
        HistoryDayType object = new HistoryDayType(null, getTime(2007, Calendar.NOVEMBER, 05, 0, 0));
        assertEquals(prefix + "<day date=\"2007-11-05\"/>", marshal(object));
    }

    public void testUnmarshalDate() throws Exception {
        HistoryDayType object = unmarshal(prefix + "<day date=\"2007-11-05\"/>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 05, 0, 0), object.getDate());
    }

    public void testMarshalHistoryPeriod() throws Exception {
        HistoryDayType object = new HistoryDayType();
        object.addHistory(new HistoryType(null, new IOHLC[] {
            new OHLC(getTime(2007, Calendar.NOVEMBER, 05, 12, 30), null, null, null, null, null),
        }, TimeSpan.minutes(1)));
        assertEquals(prefix + "<day><history period=\"1min\"><bar date=\"2007-11-05 12:30:00\"/></history></day>", marshal(object));
    }

    public void testUnmarshalHistoryPeriod() throws Exception {
        HistoryDayType object = unmarshal(prefix + "<day><history period=\"1min\"><bar date=\"2007-11-05 12:30:00\"/></history></day>");
        assertEquals(1, object.getPeriods().length);
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private String marshal(HistoryDayType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private HistoryDayType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(HistoryDayType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (HistoryDayType) unmarshaller.unmarshal(new StringReader(string));
    }
}
