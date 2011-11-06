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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class IdentifierTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        IdentifierType object = new IdentifierType();
        assertEquals(prefix + "<identifier/>", marshal(object));
    }

    public void testUnMarshalEmpty() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier/>");
        assertTrue(object != null);
    }

    public void testMarshalSymbol() throws Exception {
        IdentifierType object = new IdentifierType();
        object.setSymbol("AAPL");
        assertEquals(prefix + "<identifier symbol=\"AAPL\"/>", marshal(object));
    }

    public void testUnMarshalSymbol() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier symbol=\"AAPL\"/>");
        assertEquals("AAPL", object.getSymbol());
    }

    public void testMarshalPriceData() throws Exception {
        IdentifierType object = new IdentifierType();
        object.setPriceData(new PriceDataType());
        assertEquals(prefix + "<identifier><prices/></identifier>", marshal(object));
    }

    public void testUnMarshalPriceData() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier><prices/></identifier>");
        assertTrue(object.getPriceData() != null);
    }

    public void testAlwaysReturnCurrentValues() throws Exception {
        IdentifierType object = new IdentifierType();
        assertNotNull(object.getTrade());
        assertNotNull(object.getQuote());
        assertNotNull(object.getPriceData());
    }

    public void testDontReturnTodayOHLIfNotSet() throws Exception {
        IdentifierType object = new IdentifierType();
        assertNull(object.getTodayOHL());
    }

    private String marshal(IdentifierType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private IdentifierType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(IdentifierType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IdentifierType) unmarshaller.unmarshal(new StringReader(string));
    }
}
