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

package org.eclipsetrader.repository.local.internal.stores;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class SecurityStoreTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        SecurityStore object = new SecurityStore();
        assertEquals(prefix + "<security/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        SecurityStore object = unmarshal(prefix + "<security/>");
        assertNotNull(object);
    }

    public void testMarshalUnknownProperties() throws Exception {
        SecurityStore object = new SecurityStore();
        StoreProperties properties = new StoreProperties();
        properties.setProperty("option-expiration", "2008/08/30");
        object.putProperties(properties, null);
        assertEquals(prefix + "<security><properties><property name=\"option-expiration\">2008/08/30</property></properties></security>", marshal(object));
    }

    public void testUnmarshalUnknownProperties() throws Exception {
        SecurityStore object = unmarshal(prefix + "<security><properties><property name=\"option-expiration\">2008/08/30</property></properties></security>");
        IStoreProperties properties = object.fetchProperties(null);
        assertEquals("2008/08/30", properties.getProperty("option-expiration"));
    }

    private String marshal(SecurityStore object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private SecurityStore unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SecurityStore.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (SecurityStore) unmarshaller.unmarshal(new StringReader(string));
    }
}
