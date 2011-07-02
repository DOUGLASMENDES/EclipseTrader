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

package org.eclipsetrader.yahoo.internal.core.repository;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class IdentifiersListTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testUnMarshalEmpty() throws Exception {
        IdentifiersList object = unmarshal(prefix + "<list/>");
        assertTrue(object != null);
        assertSame(object, IdentifiersList.getInstance());
    }

    public void testUnMarshalIdentifier() throws Exception {
        IdentifiersList object = unmarshal(prefix + "<list><identifier symbol=\"F.MI\"><prices volume=\"38628972\" time=\"2008-01-09 13:59:00\" open=\"16.14\" low=\"15.99\" last=\"16.37\" high=\"16.53\" bid=\"16.36\" ask=\"16.37\"/></identifier></list>");
        assertEquals(1, object.getIdentifiers().size());
    }

    private IdentifiersList unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(IdentifiersList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (IdentifiersList) unmarshaller.unmarshal(new StringReader(string));
    }
}
