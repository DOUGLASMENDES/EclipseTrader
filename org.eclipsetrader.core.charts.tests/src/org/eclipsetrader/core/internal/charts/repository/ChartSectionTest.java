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

package org.eclipsetrader.core.internal.charts.repository;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IElementSection;

public class ChartSectionTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalName() throws Exception {
        IChartSection object = new ChartSection(null, "Test");
        assertEquals(prefix + "<section name=\"Test\"/>", marshal(object));
    }

    public void testUnmarshalName() throws Exception {
        IChartSection object = unmarshal(prefix + "<section name=\"Test\"/>");
        assertEquals("Test", object.getName());
    }

    public void testMarshalId() throws Exception {
        IChartSection object = new ChartSection("id1", null);
        assertEquals(prefix + "<section id=\"id1\"/>", marshal(object));
    }

    public void testUnmarshalId() throws Exception {
        IChartSection object = unmarshal(prefix + "<section id=\"id1\"/>");
        assertEquals("id1", object.getId());
    }

    public void testMarshalElement() throws Exception {
        IChartSection object = new ChartSection();
        object.setElements(new IElementSection[] {
            new ElementSection("id1", null)
        });
        assertEquals(prefix + "<section><element id=\"id1\"/></section>", marshal(object));
    }

    public void testUnmarshalElement() throws Exception {
        IChartSection object = unmarshal(prefix + "<section><element id=\"id1\"/></section>");
        assertEquals(1, object.getElements().length);
    }

    private String marshal(IChartSection object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private IChartSection unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartSection.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IChartSection) unmarshaller.unmarshal(new StringReader(string));
    }
}
