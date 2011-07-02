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

package org.eclipsetrader.news.internal.repository;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.tests.TestSecurity;

public class HeadLineTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testLinkEquality() throws Exception {
        HeadLine object = new HeadLine(null, null, "Headline 1", null, "http://www.somesite.net/rss/link");
        assertTrue(object.equals(new HeadLine(null, null, "Headline 2", null, "http://www.somesite.net/rss/link")));
        assertTrue(object.equals(new HeadLine(null, null, "Headline 1", null, "http://www.somesite.net/rss/link2")));
    }

    public void testSourceAndTextEquality() throws Exception {
        HeadLine object = new HeadLine(null, "Source", "Headline", null, "http://www.somesite.net/rss/link1");
        assertTrue(object.equals(new HeadLine(null, "Source", "Headline", null, "http://www.somesite.net/rss/link2")));
        assertFalse(object.equals(new HeadLine(null, "Source 1", "Headline", null, "http://www.somesite.net/rss/link2")));
        assertFalse(object.equals(new HeadLine(null, "Source", "Headline 2", null, "http://www.somesite.net/rss/link2")));
    }

    public void testMarshalEmpty() throws Exception {
        HeadLine object = new HeadLine();
        assertEquals(prefix + "<headline readed=\"false\"/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline/>");
        assertNotNull(object);
    }

    public void testMarshalText() throws Exception {
        HeadLine object = new HeadLine(null, null, "News Text", null, null);
        assertEquals(prefix + "<headline readed=\"false\"><text>News Text</text></headline>", marshal(object));
    }

    public void testUnmarshalText() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline><text>News Text</text></headline>");
        assertEquals("News Text", object.getText());
    }

    public void testMarshalSource() throws Exception {
        HeadLine object = new HeadLine(null, "Source Text", null, null, null);
        assertEquals(prefix + "<headline readed=\"false\"><source>Source Text</source></headline>", marshal(object));
    }

    public void testUnmarshalSource() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline><source>Source Text</source></headline>");
        assertEquals("Source Text", object.getSource());
    }

    public void testMarshalLink() throws Exception {
        HeadLine object = new HeadLine(null, null, null, null, "http://localhost/news/1.html");
        assertEquals(prefix + "<headline readed=\"false\"><link>http://localhost/news/1.html</link></headline>", marshal(object));
    }

    public void testUnmarshalLink() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline><link>http://localhost/news/1.html</link></headline>");
        assertEquals("http://localhost/news/1.html", object.getLink());
    }

    public void testUnmarshalReaded() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline readed=\"true\"/>");
        assertTrue(object.isReaded());
    }

    public void testMarshalDate() throws Exception {
        HeadLine object = new HeadLine(getTime(2008, Calendar.JANUARY, 15, 16, 23), null, null, null, null);
        assertEquals(prefix + "<headline readed=\"false\" date=\"2008-01-15 16:23:00\"/>", marshal(object));
    }

    public void testUnmarshalDate() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline date=\"2008-01-15 16:23:00\"/>");
        assertEquals(getTime(2008, Calendar.JANUARY, 15, 16, 23), object.getDate());
    }

    public void testMarshalMembers() throws Exception {
        HeadLine object = new HeadLine(null, null, null, new ISecurity[] {
                new TestSecurity("Test1", null, new URI("local:securities#1")),
                new TestSecurity("Test2", null, new URI("local:securities#2")),
        }, null);
        assertEquals(prefix + "<headline readed=\"false\"><members><security>local:securities#1</security><security>local:securities#2</security></members></headline>", marshal(object));
    }

    public void testUnmarshalMembers() throws Exception {
        HeadLine object = unmarshal(prefix + "<headline><members><security>local:securities#1</security><security>local:securities#2</security></members></headline>");
        ISecurity[] members = object.getMembers();
        assertEquals(2, members.length);
    }

    private String marshal(HeadLine object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private HeadLine unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(HeadLine.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (HeadLine) unmarshaller.unmarshal(new StringReader(string));
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
