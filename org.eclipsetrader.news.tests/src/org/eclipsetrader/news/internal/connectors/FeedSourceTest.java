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

package org.eclipsetrader.news.internal.connectors;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class FeedSourceTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        FeedSource object = new FeedSource();
        assertEquals(prefix + "<source enabled=\"false\"/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        FeedSource object = unmarshal(prefix + "<source/>");
        assertNotNull(object);
    }

    private String marshal(FeedSource object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private FeedSource unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(FeedSource.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (FeedSource) unmarshaller.unmarshal(new StringReader(string));
    }
}
