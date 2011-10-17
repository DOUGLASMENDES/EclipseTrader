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

import org.eclipsetrader.core.charts.repository.IChartTemplate;

public class ChartTemplateTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalName() throws Exception {
        ChartTemplate object = new ChartTemplate("Test");
        assertEquals(prefix + "<chart><name>Test</name></chart>", marshal(object));
    }

    public void testUnmarshalName() throws Exception {
        ChartTemplate object = unmarshal(prefix + "<chart><name>Test</name></chart>");
        assertEquals("Test", object.getName());
        assertEquals(0, object.getSections().length);
    }

    public void testMarshalSection() throws Exception {
        ChartTemplate object = new ChartTemplate(null);
        object.setSections(new ChartSection[] {
                new ChartSection(null, "Section 1"),
                new ChartSection(null, "Section 2"),
        });
        assertEquals(prefix + "<chart><section name=\"Section 1\"/><section name=\"Section 2\"/></chart>", marshal(object));
    }

    public void testUnmarshalSection() throws Exception {
        IChartTemplate object = unmarshal(prefix + "<chart><section name=\"Section 1\"/><section name=\"Section 2\"/></chart>");
        assertEquals(2, object.getSections().length);
    }

    private String marshal(ChartTemplate object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ChartTemplate unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ChartTemplate.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ChartTemplate) unmarshaller.unmarshal(new StringReader(string));
    }
}
