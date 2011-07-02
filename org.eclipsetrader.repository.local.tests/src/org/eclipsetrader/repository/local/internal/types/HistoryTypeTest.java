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
import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.feed.Split;

public class HistoryTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        HistoryType object = new HistoryType();
        assertEquals(prefix + "<history/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        HistoryType object = unmarshal(prefix + "<history/>");
        assertNotNull(object);
        assertEquals(0, object.getData().size());
    }

    public void testMarshalDate() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(getTime(2007, Calendar.NOVEMBER, 5, 0, 0), null, null, null, null, null),
        });
        assertEquals(prefix + "<history><bar date=\"2007-11-05 00:00:00\"/></history>", marshal(object));
    }

    public void testUnmarshalDate() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar date=\"2007-11-05 00:00:00\"/></history>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 0, 0), object.getData().get(0).getDate());
    }

    public void testMarshalOpen() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(null, 12.5, null, null, null, null),
        });
        assertEquals(prefix + "<history><bar open=\"12.5\"/></history>", marshal(object));
    }

    public void testUnmarshalOpen() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar open=\"12.5\"/></history>");
        assertEquals(12.5, object.getData().get(0).getOpen());
    }

    public void testMarshalHigh() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(null, null, 12.5, null, null, null),
        });
        assertEquals(prefix + "<history><bar high=\"12.5\"/></history>", marshal(object));
    }

    public void testUnmarshalHigh() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar high=\"12.5\"/></history>");
        assertEquals(12.5, object.getData().get(0).getHigh());
    }

    public void testMarshalLow() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(null, null, null, 12.5, null, null),
        });
        assertEquals(prefix + "<history><bar low=\"12.5\"/></history>", marshal(object));
    }

    public void testUnmarshalLow() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar low=\"12.5\"/></history>");
        assertEquals(12.5, object.getData().get(0).getLow());
    }

    public void testMarshalClose() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(null, null, null, null, 12.5, null),
        });
        assertEquals(prefix + "<history><bar close=\"12.5\"/></history>", marshal(object));
    }

    public void testUnmarshalClose() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar close=\"12.5\"/></history>");
        assertEquals(12.5, object.getData().get(0).getClose());
    }

    public void testMarshalVolume() throws Exception {
        HistoryType object = new HistoryType(null, new IOHLC[] {
            new OHLC(null, null, null, null, null, 12500L),
        });
        assertEquals(prefix + "<history><bar volume=\"12500\"/></history>", marshal(object));
    }

    public void testUnmarshalVolume() throws Exception {
        HistoryType object = unmarshal(prefix + "<history><bar volume=\"12500\"/></history>");
        assertEquals(new Long(12500), object.getData().get(0).getVolume());
    }

    public void testMarshalSplits() throws Exception {
        HistoryType object = new HistoryType(null, null, new ISplit[] {
            new Split(getTime(2008, Calendar.JULY, 23, 0, 0), 1.0, 2.0),
        }, null);
        assertEquals(prefix + "<history><split new-quantity=\"2\" old-quantity=\"1\" date=\"2008-07-23\"/></history>", marshal(object));
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private String marshal(HistoryType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private HistoryType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(HistoryType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (HistoryType) unmarshaller.unmarshal(new StringReader(string));
    }
}
