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

public class MarketTimeExcludeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude();
        assertEquals(prefix + "<exclude/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude/>");
        assertNull(object.getFromDate());
        assertNull(object.getToDate());
    }

    public void testMarshalSingleDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6));
        assertEquals(prefix + "<exclude date=\"2007-11-06\"/>", marshal(object));
    }

    public void testUnmarshalSingleDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude date=\"2007-11-06\"/>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getFromDate());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getToDate());
    }

    public void testMarshalFromDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(getTime(2007, Calendar.NOVEMBER, 6), null);
        assertEquals(prefix + "<exclude from=\"2007-11-06\"/>", marshal(object));
    }

    public void testUnmarshalFromDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude from=\"2007-11-06\"/>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getFromDate());
        assertNull(object.getToDate());
    }

    public void testMarshalToDate() throws Exception {
        MarketTimeExclude object = new MarketTimeExclude(null, getTime(2007, Calendar.NOVEMBER, 6));
        assertEquals(prefix + "<exclude to=\"2007-11-06\"/>", marshal(object));
    }

    public void testUnmarshalToDate() throws Exception {
        MarketTimeExclude object = unmarshal(prefix + "<exclude to=\"2007-11-06\"/>");
        assertNull(object.getFromDate());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6), object.getToDate());
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private String marshal(MarketTimeExclude object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketTimeExclude unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketTimeExclude.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketTimeExclude) unmarshaller.unmarshal(new StringReader(string));
    }
}
