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

import org.eclipsetrader.core.views.Column;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class ColumnTypeTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        ColumnType object = new ColumnType();
        assertEquals(prefix + "<column/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        ColumnType object = unmarshal(prefix + "<column/>");
        assertNotNull(object);
    }

    public void testMarshalName() throws Exception {
        ColumnType object = new ColumnType(new Column("Test", null));
        assertEquals(prefix + "<column>Test</column>", marshal(object));
    }

    public void testUnmarshalName() throws Exception {
        ColumnType object = unmarshal(prefix + "<column>Test</column>");
        assertEquals("Test", object.getElement().getName());
    }

    public void testMarshalDataProviderFactory() throws Exception {
        ColumnType object = new ColumnType(new Column(null, new IDataProviderFactory() {

            @Override
            public IDataProvider createProvider() {
                return null;
            }

            @Override
            public String getId() {
                return "test.id";
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class[] getType() {
                return new Class[0];
            }
        }));
        assertEquals(prefix + "<column factory=\"test.id\"/>", marshal(object));
    }

    private String marshal(ColumnType object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ColumnType unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ColumnType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ColumnType) unmarshaller.unmarshal(new StringReader(string));
    }
}
