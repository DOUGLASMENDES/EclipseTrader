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

public class ScriptStoreTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        ScriptStore object = new ScriptStore();
        assertEquals(prefix + "<script/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        ScriptStore object = unmarshal(prefix + "<script/>");
        assertNotNull(object);
    }

    private String marshal(ScriptStore object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ScriptStore unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ScriptStore.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ScriptStore) unmarshaller.unmarshal(new StringReader(string));
    }
}
