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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;

public class IdentifierTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testCreateFromFeedIdentifier() throws Exception {
        IFeedIdentifier feedIdentifier = new FeedIdentifier("MSFT", null);
        IdentifierType object = new IdentifierType(feedIdentifier);
        assertEquals("MSFT", object.getSymbol());
        assertSame(feedIdentifier, object.getIdentifier());
    }

    public void testAlwaysReturnSameFeedIdentifier() throws Exception {
        IdentifierType object = new IdentifierType("MSFT");
        IFeedIdentifier feedIdentifier = object.getIdentifier();
        assertSame(feedIdentifier, object.getIdentifier());
    }

    public void testMarshalEmpty() throws Exception {
        IdentifierType object = new IdentifierType();
        assertEquals(prefix + "<identifier/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier/>");
        assertNotNull(object);
    }

    public void testMarshalSymbol() throws Exception {
        IdentifierType object = new IdentifierType("MSFT");
        assertEquals(prefix + "<identifier symbol=\"MSFT\"/>", marshal(object));
    }

    public void testUnmarshalSymbol() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier symbol=\"MSFT\"/>");
        assertEquals("MSFT", object.getSymbol());
    }

    public void testMarshalProperties() throws Exception {
        FeedProperties feedProperties = new FeedProperties();
        feedProperties.setProperty("Market", "Nasdaq");
        IdentifierType object = new IdentifierType(new FeedIdentifier(null, feedProperties));
        assertEquals(prefix + "<identifier><properties><property name=\"Market\">Nasdaq</property></properties></identifier>", marshal(object));
    }

    public void testUnmarshalProperties() throws Exception {
        IdentifierType object = unmarshal(prefix + "<identifier><properties><property name=\"Market\">Nasdaq</property></properties></identifier>");
        IFeedProperties feedProperties = (IFeedProperties) object.getIdentifier().getAdapter(IFeedProperties.class);
        assertEquals("Nasdaq", feedProperties.getProperty("Market"));
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
