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

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import junit.framework.TestCase;

public class PropertyTypeTest extends TestCase {

    public void testCreateFromDate() throws Exception {
        PropertyType property = PropertyType.create("date", getTime(2008, Calendar.JULY, 10, 12, 55));
        assertEquals("date", property.getName());
        assertEquals(Date.class.getName(), property.getType());
        assertEquals("20080710125500", property.getValue());
    }

    public void testConvertToDate() throws Exception {
        PropertyType property = new PropertyType("date", Date.class.getName(), "20080710125500");
        assertEquals(getTime(2008, Calendar.JULY, 10, 12, 55), PropertyType.convert(property));
    }

    public void testCreateFromDouble() throws Exception {
        PropertyType property = PropertyType.create("strike", 27.5);
        assertEquals("strike", property.getName());
        assertEquals(Double.class.getName(), property.getType());
        assertEquals(Double.toString(27.5), property.getValue());
    }

    public void testConvertToDouble() throws Exception {
        PropertyType property = new PropertyType("strike", Double.class.getName(), Double.toString(27.5));
        assertEquals(new Double(27.5), PropertyType.convert(property));
    }

    public void testCreateFromInteger() throws Exception {
        PropertyType property = PropertyType.create("period", 5);
        assertEquals("period", property.getName());
        assertEquals(Integer.class.getName(), property.getType());
        assertEquals("5", property.getValue());
    }

    public void testConvertToInteger() throws Exception {
        PropertyType property = new PropertyType("period", Integer.class.getName(), "5");
        assertEquals(new Integer(5), PropertyType.convert(property));
    }

    public void testCreateFromBoolean() throws Exception {
        PropertyType property = PropertyType.create("mandatory", true);
        assertEquals("mandatory", property.getName());
        assertEquals(Boolean.class.getName(), property.getType());
        assertEquals("true", property.getValue());
    }

    public void testConvertToBoolean() throws Exception {
        PropertyType property = new PropertyType("mandatory", Boolean.class.getName(), "true");
        assertEquals(Boolean.TRUE, PropertyType.convert(property));
    }

    public void testCreateFromCurrency() throws Exception {
        PropertyType property = PropertyType.create("currency", Currency.getInstance("USD"));
        assertEquals("currency", property.getName());
        assertEquals(Currency.class.getName(), property.getType());
        assertEquals("USD", property.getValue());
    }

    public void testConvertToCurrency() throws Exception {
        PropertyType property = new PropertyType("currency", Currency.class.getName(), "USD");
        assertEquals(Currency.getInstance("USD"), PropertyType.convert(property));
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
