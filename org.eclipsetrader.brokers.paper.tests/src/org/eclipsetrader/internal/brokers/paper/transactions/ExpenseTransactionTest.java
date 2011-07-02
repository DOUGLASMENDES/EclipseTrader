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
import java.util.Currency;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.Cash;

public class ExpenseTransactionTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        ExpenseTransaction object = new ExpenseTransaction();
        assertEquals(prefix + "<expense/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        ExpenseTransaction object = unmarshal(prefix + "<expense/>");
        assertNotNull(object);
    }

    public void testMarshalAmount() throws Exception {
        ExpenseTransaction object = new ExpenseTransaction(new Cash(1.5, Currency.getInstance("USD")));
        String result = marshal(object);
        assertTrue(result.indexOf("amount=\"1.5\"") != -1);
        assertTrue(result.indexOf("currency=\"USD\"") != -1);
    }

    public void testUnmarshalAmount() throws Exception {
        ExpenseTransaction object = unmarshal(prefix + "<expense amount=\"1.5\" currency=\"USD\"/>");
        assertEquals(1.5, object.getAmount().getAmount());
        assertEquals(Currency.getInstance("USD"), object.getAmount().getCurrency());
    }

    private String marshal(ExpenseTransaction object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private ExpenseTransaction unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ExpenseTransaction.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ExpenseTransaction) unmarshaller.unmarshal(new StringReader(string));
    }
}
