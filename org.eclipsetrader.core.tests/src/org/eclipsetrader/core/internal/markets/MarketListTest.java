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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class MarketListTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmptyList() throws Exception {
        assertEquals(prefix + "<list/>", marshal(new MarketList()));
    }

    public void testUnmarshalEmptyList() throws Exception {
        MarketList object = unmarshal(prefix + "<list/>");
        assertEquals(0, object.getList().size());
    }

    public void testMarshalList() throws Exception {
        MarketList object = new MarketList();
        object.getList().add(new Market("Test", null, null));
        assertEquals(prefix + "<list><market name=\"Test\"/></list>", marshal(object));
    }

    public void testUnmarshalList() throws Exception {
        MarketList object = unmarshal(prefix + "<list><market name=\"Test\"/></list>");
        assertEquals(1, object.getList().size());
        assertEquals("Test", object.getList().get(0).getName());
    }

    private String marshal(MarketList object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private MarketList unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MarketList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MarketList) unmarshaller.unmarshal(new StringReader(string));
    }
}
