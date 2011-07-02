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

package org.eclipsetrader.yahoo.internal.core.repository;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class PriceDataTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        PriceDataType object = new PriceDataType();
        assertEquals(prefix + "<prices/>", marshal(object));
    }

    public void testUnMarshalEmpty() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices/>");
        assertTrue(object != null);
    }

    public void testMarshalTime() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setTime(getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0));
        assertEquals(prefix + "<prices time=\"2007-11-06 15:30:00\"/>", marshal(object));
    }

    public void testUnMarshalTime() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices time=\"2007-11-06 15:30:00\"/>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0), object.getTime());
    }

    public void testMarshalLast() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setLast(15.75);
        assertEquals(prefix + "<prices last=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalLast() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices last=\"15.75\"/>");
        assertEquals(15.75, object.getLast());
    }

    public void testMarshalLastSize() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setLastSize(15000L);
        assertEquals(prefix + "<prices last-size=\"15000\"/>", marshal(object));
    }

    public void testUnMarshalLastSize() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices last-size=\"15000\"/>");
        assertEquals(new Long(15000), object.getLastSize());
    }

    public void testMarshalBid() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setBid(15.75);
        assertEquals(prefix + "<prices bid=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalBid() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices bid=\"15.75\"/>");
        assertEquals(15.75, object.getBid());
    }

    public void testMarshalBidSize() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setBidSize(15000L);
        assertEquals(prefix + "<prices bid-size=\"15000\"/>", marshal(object));
    }

    public void testUnMarshalBidSize() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices bid-size=\"15000\"/>");
        assertEquals(new Long(15000), object.getBidSize());
    }

    public void testMarshalAsk() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setAsk(15.75);
        assertEquals(prefix + "<prices ask=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalAsk() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices ask=\"15.75\"/>");
        assertEquals(15.75, object.getAsk());
    }

    public void testMarshalAskSize() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setAskSize(15000L);
        assertEquals(prefix + "<prices ask-size=\"15000\"/>", marshal(object));
    }

    public void testUnMarshalAskSize() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices ask-size=\"15000\"/>");
        assertEquals(new Long(15000), object.getAskSize());
    }

    public void testMarshalOpen() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setOpen(15.75);
        assertEquals(prefix + "<prices open=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalOpen() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices open=\"15.75\"/>");
        assertEquals(15.75, object.getOpen());
    }

    public void testMarshalHigh() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setHigh(15.75);
        assertEquals(prefix + "<prices high=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalHigh() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices high=\"15.75\"/>");
        assertEquals(15.75, object.getHigh());
    }

    public void testMarshalLow() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setLow(15.75);
        assertEquals(prefix + "<prices low=\"15.75\"/>", marshal(object));
    }

    public void testUnMarshalLow() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices low=\"15.75\"/>");
        assertEquals(15.75, object.getLow());
    }

    public void testMarshalVolume() throws Exception {
        PriceDataType object = new PriceDataType();
        object.setVolume(15000L);
        assertEquals(prefix + "<prices volume=\"15000\"/>", marshal(object));
    }

    public void testUnMarshalVolume() throws Exception {
        PriceDataType object = unmarshal(prefix + "<prices volume=\"15000\"/>");
        assertEquals(new Long(15000), object.getVolume());
    }

    private String marshal(PriceDataType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private PriceDataType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(PriceDataType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (PriceDataType) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(0);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, second);
        return date.getTime();
    }
}
