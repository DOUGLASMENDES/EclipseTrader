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

package org.eclipsetrader.internal.brokers.paper.transactions;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class StockTransactionTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testConstructor() throws Exception {
        StockTransaction transaction = new StockTransaction(null, 1000L, 1.5);
        assertEquals(1500.0, transaction.getAmount().getAmount());
    }

    public void testMarshalEmpty() throws Exception {
        StockTransaction object = new StockTransaction();
        assertEquals(prefix + "<stock/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        StockTransaction object = unmarshal(prefix + "<stock/>");
        assertNotNull(object);
    }

    private String marshal(StockTransaction object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private StockTransaction unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(StockTransaction.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (StockTransaction) unmarshaller.unmarshal(new StringReader(string));
    }
}
