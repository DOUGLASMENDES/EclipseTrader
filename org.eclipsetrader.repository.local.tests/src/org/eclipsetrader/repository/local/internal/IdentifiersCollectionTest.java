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

package org.eclipsetrader.repository.local.internal;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.repository.local.internal.types.IdentifierType;

public class IdentifiersCollectionTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testAlwaysGetAnIdentifier() throws Exception {
        IdentifiersCollection collection = new IdentifiersCollection();
        assertNotNull(collection.getFeedIdentifierFromSymbol("MSFT"));
        assertNotNull(collection.getFeedIdentifierFromSymbol("IBM"));
    }

    public void testGetSameFeedIdentifierinstance() throws Exception {
        IdentifiersCollection collection = new IdentifiersCollection();
        IFeedIdentifier identifier = collection.getFeedIdentifierFromSymbol("MSFT");
        assertSame(identifier, collection.getFeedIdentifierFromSymbol("MSFT"));
    }

    public void testAddNewIdentifiersToCollection() throws Exception {
        IdentifiersCollection collection = new IdentifiersCollection();
        assertEquals(0, collection.getList().size());
        collection.getFeedIdentifierFromSymbol("MSFT");
        assertEquals(1, collection.getList().size());
        collection.getFeedIdentifierFromSymbol("MSFT");
        assertEquals(1, collection.getList().size());
    }

    public void testPutFeedIdentifierToCollection() throws Exception {
        IdentifiersCollection collection = new IdentifiersCollection();
        collection.putFeedIdentifier(new FeedIdentifier("MSFT", null));
        assertEquals(1, collection.getList().size());
    }

    public void testReplaceFeedIdentifier() throws Exception {
        IdentifiersCollection collection = new IdentifiersCollection();
        collection.putFeedIdentifier(new FeedIdentifier("MSFT", null));
        IdentifierType type = collection.getList().first();
        collection.putFeedIdentifier(new FeedIdentifier("MSFT", null));
        assertEquals(1, collection.getList().size());
        assertNotSame(type, collection.getList().first());
    }

    public void testMarshalEmpty() throws Exception {
        IdentifiersCollection object = new IdentifiersCollection();
        assertEquals(prefix + "<list/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        IdentifiersCollection object = unmarshal(prefix + "<list/>");
        assertEquals(0, object.getList().size());
    }

    public void testMarshalIdentifier() throws Exception {
        IdentifiersCollection object = new IdentifiersCollection();
        object.getFeedIdentifierFromSymbol("MSFT");
        assertEquals(prefix + "<list><identifier symbol=\"MSFT\"/></list>", marshal(object));
    }

    public void testUnmarshalidentifier() throws Exception {
        IdentifiersCollection object = unmarshal(prefix + "<list><identifier symbol=\"MSFT\"/></list>");
        assertEquals(1, object.getList().size());
        assertEquals("MSFT", object.getList().first().getIdentifier().getSymbol());
    }

    private String marshal(IdentifiersCollection object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private IdentifiersCollection unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersCollection.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IdentifiersCollection) unmarshaller.unmarshal(new StringReader(string));
    }
}
