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

package org.eclipsetrader.core.internal.markets;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.eclipsetrader.core.TestFeedConnector;

public class MarketTest extends TestCase {

    private String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public void testGetNormalizedTime() throws Exception {
        Market market = new Market();
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 9, 30), market.getCombinedDateTime(getTime(2007, Calendar.NOVEMBER, 5), getTime(9, 30)));
    }

    public void testGetNormalizedTimeInDifferentTimeZone() throws Exception {
        Market market = new Market();
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 15, 30), market.getCombinedDateTime(getTime(2007, Calendar.NOVEMBER, 5), getTime(9, 30)));
    }

    public void testIsOpen() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 0), getTime(9, 30)),
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 15)));
        assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 11, 0)));
    }

    public void testIsOpenAtOpenTime() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 9, 30)));
    }

    public void testIsOpenAtCloseTime() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 16, 0)));
    }

    public void testIsOpenEarly() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 6, 0)));
    }

    public void testIsOpenLate() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 17, 0)));
    }

    public void testIsOpenOnSunday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        });
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 4, 1, 0)));
    }

    public void testIsOpenOnHoliday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 24, 10, 0)));
        assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 25, 10, 0)));
        assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 26, 10, 0)));
    }

    public void testIsPartiallyOpenOnHoliday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 31, 9, 30), getTime(2007, Calendar.DECEMBER, 31, 12, 30), "Holiday")
        });
        assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 10, 0)));
        assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 28, 15, 0)));
        assertTrue(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 10, 0)));
        assertFalse(market.isOpen(getTime(2007, Calendar.DECEMBER, 31, 15, 0)));
    }

    public void testDontChangeTimeWithTimeZone() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        assertEquals(getTime(9, 30), market.getSchedule()[0].getOpenTime());
        assertEquals(getTime(16, 0), market.getSchedule()[0].getCloseTime());
    }

    public void testIsOpenWithDifferentTimeZone() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 10, 0)));
        assertTrue(market.isOpen(getTime(2007, Calendar.NOVEMBER, 6, 18, 0)));
        assertFalse(market.isOpen(getTime(2007, Calendar.NOVEMBER, 23, 10, 0)));
    }

    public void testMarshalDescription() throws Exception {
        Market object = new Market("Test", null, null);
        assertEquals(prefix + "<market name=\"Test\"/>", marshal(object));
    }

    public void testUnmarshalDescription() throws Exception {
        Market object = unmarshal(prefix + "<market name=\"Test\"/>");
        assertEquals("Test", object.getName());
    }

    public void testMarshalOpenTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 0, 0);
        Market object = new Market(null, Arrays.asList(new MarketTime[] {
            new MarketTime(date.getTime(), null)
        }));
        assertEquals(prefix + "<market><schedule><time open=\"10:00\"/></schedule></market>", marshal(object));
    }

    public void testUnmarshalOpenTime() throws Exception {
        Market object = unmarshal(prefix + "<market><schedule><time open=\"10:00\"/></schedule></market>");
        assertEquals("10:00", new TimeAdapter().marshal(object.getSchedule()[0].getOpenTime()));
    }

    public void testMarshalCloseTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(2007, Calendar.NOVEMBER, 6, 10, 30, 0);
        Market object = new Market(null, Arrays.asList(new MarketTime[] {
            new MarketTime(null, date.getTime())
        }));
        assertEquals(prefix + "<market><schedule><time close=\"10:30\"/></schedule></market>", marshal(object));
    }

    public void testUnmarshalCloseTime() throws Exception {
        Market object = unmarshal(prefix + "<market><schedule><time close=\"10:30\"/></schedule></market>");
        assertEquals("10:30", new TimeAdapter().marshal(object.getSchedule()[0].getCloseTime()));
    }

    public void testMarshalTimeZone() throws Exception {
        Market object = new Market(null, null, TimeZone.getTimeZone("America/New_York"));
        assertEquals(prefix + "<market timeZone=\"America/New_York\"/>", marshal(object));
    }

    public void testUnmarshalTimeZone() throws Exception {
        Market object = unmarshal(prefix + "<market timeZone=\"America/New_York\"/>");
        assertEquals("America/New_York", object.getTimeZone().getID());
    }

    public void testMarshalLiveFeedConnector() throws Exception {
        Market object = new Market(null, null, null);
        object.setLiveFeedConnector(new TestFeedConnector("test.id", "Test"));
        assertEquals(prefix + "<market><liveFeed id=\"test.id\"/></market>", marshal(object));
    }

    public void testUnmarshalLiveFeedConnector() throws Exception {
        Market object = unmarshal(prefix + "<market><liveFeed id=\"test.id\"/></market>");
        assertNotNull(object.getLiveFeedConnector());
        assertEquals("test.id", object.getLiveFeedConnector().getId());
    }

    public void testGetMarketDayForOpenDay() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 9, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 16, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetMarketTime() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
                new MarketTime(getTime(9, 0), getTime(9, 30), "Pre-open"),
                new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 9, 0), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 9, 30), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetMarketDayForCloseDay() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        });
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.NOVEMBER, 3));
        assertFalse(day.isOpen());
        assertNull(day.getOpenTime());
        assertNull(day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetNextMarketDayForOpenDay() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6, 10, 0));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 7, 9, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 7, 16, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetNextMarketDayForCloseDay() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        });
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 3));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 9, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 16, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetNextMarketDayForCloseDayInDifferentTimeZone() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        market.setWeekDays(new Integer[] {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
        });
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 3));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 15, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 5, 22, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetNextMarketDayWithDifferentTimeZone() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6, 10, 0));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 22, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetNextMarketDayForToday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.NOVEMBER, 6, 10, 0));
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 15, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.NOVEMBER, 6, 22, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    public void testGetMarketDayForHoliday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        MarketDay day = market.getMarketDayFor(getTime(2007, Calendar.DECEMBER, 25));
        assertFalse(day.isOpen());
        assertNull(day.getOpenTime());
        assertNull(day.getCloseTime());
        assertEquals("Holiday", day.getMessage());
    }

    public void testGetNextMarketDayForHoliday() throws Exception {
        Market market = new Market("Test", Arrays.asList(new MarketTime[] {
            new MarketTime(getTime(9, 30), getTime(16, 0))
        }));
        market.setHolidays(new MarketHoliday[] {
            new MarketHoliday(getTime(2007, Calendar.DECEMBER, 25), "Holiday")
        });
        MarketDay day = market.getNextMarketDayFor(getTime(2007, Calendar.DECEMBER, 25));
        assertEquals(getTime(2007, Calendar.DECEMBER, 26, 9, 30), day.getOpenTime());
        assertEquals(getTime(2007, Calendar.DECEMBER, 26, 16, 0), day.getCloseTime());
        assertNull(day.getMessage());
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    private String marshal(Market object) throws Exception {
        StringWriter string = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
        marshaller.marshal(object, string);
        return string.toString();
    }

    private Market unmarshal(String string) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Market.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Market) unmarshaller.unmarshal(new StringReader(string));
    }
}
