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

package org.eclipsetrader.internal.brokers.paper.transactions;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class OrderElementTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        OrderElement object = new OrderElement();
        assertEquals(prefix + "<order/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        OrderElement object = unmarshal(prefix + "<order/>");
        assertNotNull(object);
    }

    private String marshal(OrderElement object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private OrderElement unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(OrderElement.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (OrderElement) unmarshaller.unmarshal(new StringReader(string));
    }
}
