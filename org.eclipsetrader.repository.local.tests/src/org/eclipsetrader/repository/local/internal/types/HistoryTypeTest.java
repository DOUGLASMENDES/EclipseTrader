/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;

public class HistoryTypeTest extends TestCase {
	private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	public void testMarshalEmpty() throws Exception {
		HistoryType object = new HistoryType();
		assertEquals(prefix + "<history><data/></history>", marshal(object));
	}

	public void testUnmarshalEmpty() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data/></history>");
		assertNotNull(object);
		assertEquals(0, object.getData().size());
	}

	public void testMarshalDate() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11, 0, 0), null, null, null, null, null),
		});
		assertEquals(prefix + "<history><data><bar date=\"2007-11-11 00:00:00\"/></data></history>", marshal(object));
	}

	public void testUnmarshalDate() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar date=\"2007-11-11 00:00:00\"/></data></history>");
		assertEquals(getTime(2007, Calendar.NOVEMBER, 11, 0, 0), object.getData().get(0).getDate());
	}

	public void testMarshalOpen() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(null, 12.5, null, null, null, null),
		});
		assertEquals(prefix + "<history><data><bar open=\"12.5\"/></data></history>", marshal(object));
	}

	public void testUnmarshalOpen() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar open=\"12.5\"/></data></history>");
		assertEquals(12.5, object.getData().get(0).getOpen());
	}

	public void testMarshalHigh() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(null, null, 12.5, null, null, null),
		});
		assertEquals(prefix + "<history><data><bar high=\"12.5\"/></data></history>", marshal(object));
	}

	public void testUnmarshalHigh() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar high=\"12.5\"/></data></history>");
		assertEquals(12.5, object.getData().get(0).getHigh());
	}

	public void testMarshalLow() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(null, null, null, 12.5, null, null),
		});
		assertEquals(prefix + "<history><data><bar low=\"12.5\"/></data></history>", marshal(object));
	}

	public void testUnmarshalLow() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar low=\"12.5\"/></data></history>");
		assertEquals(12.5, object.getData().get(0).getLow());
	}

	public void testMarshalClose() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(null, null, null, null, 12.5, null),
		});
		assertEquals(prefix + "<history><data><bar close=\"12.5\"/></data></history>", marshal(object));
	}

	public void testUnmarshalClose() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar close=\"12.5\"/></data></history>");
		assertEquals(12.5, object.getData().get(0).getClose());
	}

	public void testMarshalVolume() throws Exception {
		HistoryType object = new HistoryType(new IOHLC[] {
				new OHLC(null, null, null, null, null, 12500L),
		});
		assertEquals(prefix + "<history><data><bar volume=\"12500\"/></data></history>", marshal(object));
	}

	public void testUnmarshalVolume() throws Exception {
		HistoryType object = unmarshal(prefix + "<history><data><bar volume=\"12500\"/></data></history>");
		assertEquals(new Long(12500), object.getData().get(0).getVolume());
	}

	private Date getTime(int year, int month, int day, int hour, int minute) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, hour, minute, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}

	private String marshal(HistoryType object) throws Exception {
		StringWriter string = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
		marshaller.marshal(object, string);
		return string.toString();
	}

	private HistoryType unmarshal(String string) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(HistoryType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (HistoryType) unmarshaller.unmarshal(new StringReader(string));
	}
}
