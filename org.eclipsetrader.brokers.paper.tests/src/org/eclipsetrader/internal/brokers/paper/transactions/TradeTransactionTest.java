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

import org.eclipsetrader.core.trading.ITransaction;

public class TradeTransactionTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testMarshalEmpty() throws Exception {
        TradeTransaction object = new TradeTransaction();
        assertEquals(prefix + "<trade/>", marshal(object));
    }

    public void testUnmarshalEmpty() throws Exception {
        TradeTransaction object = unmarshal(prefix + "<trade/>");
        assertNotNull(object);
    }

    public void testMarshalExpenseTransaction() throws Exception {
        TradeTransaction object = new TradeTransaction(null, new ITransaction[0], new ExpenseTransaction());
        String result = marshal(object);
        assertTrue(result.indexOf("<details><expense/></details>") != -1);
    }

    public void testMarshalStockTransaction() throws Exception {
        TradeTransaction object = new TradeTransaction(null, new ITransaction[] {
            new StockTransaction()
        }, null);
        String result = marshal(object);
        assertTrue(result.indexOf("<details><stock/></details>") != -1);
    }

    public void testMarshalTradeTransaction() throws Exception {
        TradeTransaction object = new TradeTransaction(null, new ITransaction[] {
            new TradeTransaction()
        }, null);
        String result = marshal(object);
        assertTrue(result.indexOf("<details><trade/></details>") != -1);
    }

    private String marshal(TradeTransaction object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private TradeTransaction unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(TradeTransaction.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (TradeTransaction) unmarshaller.unmarshal(new StringReader(string));
    }
}
