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
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.repository.local.TestRepositoryService;
import org.eclipsetrader.repository.local.TestSecurity;

public class HoldingTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        HoldingType object = new HoldingType();
        assertEquals(prefix + "<holding/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        HoldingType object = unmarshal(prefix + "<holding/>");
        assertNotNull(object);
    }

    public void testMarshalSecurity() throws Exception {
        HoldingType object = new HoldingType(new Holding(new TestSecurity("Test", null, "local:securities#1"), null, null, null));
        assertEquals(prefix + "<holding security=\"local:securities#1\"/>", marshal(object));
    }

    public void testUnmarshalSecurity() throws Exception {
        TestRepositoryService repositoryService = new TestRepositoryService();
        repositoryService.saveAdaptable(new ISecurity[] {
            new TestSecurity("Test", null, "local:securities#1")
        });
        SecurityAdapter.setRepositoryService(repositoryService);
        HoldingType object = unmarshal(prefix + "<holding security=\"local:securities#1\"/>");
        assertEquals("Test", object.getElement().getSecurity().getName());
    }

    public void testMarshalPosition() throws Exception {
        HoldingType object = new HoldingType(new Holding(null, 10000L, null, null));
        assertEquals(prefix + "<holding position=\"10000\"/>", marshal(object));
    }

    public void testUnmarshalPosition() throws Exception {
        HoldingType object = unmarshal(prefix + "<holding position=\"10000\"/>");
        assertEquals(new Long(10000), object.getElement().getPosition());
    }

    public void testMarshalPurchasePrice() throws Exception {
        HoldingType object = new HoldingType(new Holding(null, null, 5.75, null));
        assertEquals(prefix + "<holding price=\"5.75\"/>", marshal(object));
    }

    public void testUnmarshalPurchasePrice() throws Exception {
        HoldingType object = unmarshal(prefix + "<holding price=\"5.75\"/>");
        assertEquals(new Double(5.75), object.getElement().getPurchasePrice());
    }

    public void testMarshalDate() throws Exception {
        HoldingType object = new HoldingType(new Holding(null, null, null, getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0)));
        assertEquals(prefix + "<holding date=\"2007-11-06 15:30:00\"/>", marshal(object));
    }

    public void testUnMarshalTime() throws Exception {
        HoldingType object = unmarshal(prefix + "<holding date=\"2007-11-06 15:30:00\"/>");
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30, 0), object.getElement().getDate());
    }

    private String marshal(HoldingType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private HoldingType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(HoldingType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (HoldingType) unmarshaller.unmarshal(new StringReader(string));
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
