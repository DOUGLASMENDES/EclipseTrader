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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipsetrader.core.feed.IBackfillConnector;
import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;

@XmlRootElement(name = "market")
@XmlType(name = "org.eclipsetrader.core.markets.Market")
public class Market extends PlatformObject implements IMarket {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "timeZone")
    @XmlJavaTypeAdapter(TimeZoneAdapter.class)
    private TimeZone timeZone;

    @XmlAttribute(name = "weekDays")
    @XmlJavaTypeAdapter(WeekdaysAdapter.class)
    private Set<Integer> weekDays;

    @XmlElement(name = "liveFeed")
    private MarketConnector liveFeedConnector;

    @XmlElement(name = "backfill")
    private MarketBackfillConnector backfillConnector;

    @XmlElement(name = "intraday-backfill")
    private MarketBackfillConnector intradayBackfillConnector;

    @XmlElementWrapper(name = "schedule")
    @XmlElementRef
    private SortedSet<MarketTime> schedule;

    @XmlElementWrapper(name = "holidays")
    @XmlElementRef
    private SortedSet<MarketHoliday> holidays;

    @XmlElementWrapper(name = "members")
    @XmlElement(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private List<ISecurity> members;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected Market() {
    }

    public Market(String name, Collection<MarketTime> schedule) {
        this(name, schedule, null);
    }

    public Market(String name, Collection<MarketTime> schedule, TimeZone timeZone) {
        this.name = name;
        this.schedule = schedule != null ? new TreeSet<MarketTime>(schedule) : null;
        this.timeZone = timeZone;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getName()
     */
    @Override
    @XmlTransient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        Object oldValue = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PROP_NAME, oldValue, this.name);
    }

    @XmlTransient
    public MarketTime[] getSchedule() {
        return schedule.toArray(new MarketTime[schedule.size()]);
    }

    public void setSchedule(MarketTime[] schedule) {
        Object oldValue = this.schedule.toArray(new MarketTime[this.schedule.size()]);
        this.schedule = schedule != null ? new TreeSet<MarketTime>(Arrays.asList(schedule)) : null;
        propertyChangeSupport.firePropertyChange(PROP_SCHEDULE, oldValue, this.schedule.toArray(new MarketTime[this.schedule.size()]));
    }

    @XmlTransient
    public Integer[] getWeekDays() {
        if (weekDays == null) {
            return new Integer[0];
        }
        return weekDays.toArray(new Integer[weekDays.size()]);
    }

    public void setWeekDays(Integer[] weekDays) {
        Object oldValue = this.weekDays;
        this.weekDays = new HashSet<Integer>(Arrays.asList(weekDays));
        propertyChangeSupport.firePropertyChange(PROP_WEEKDAYS, oldValue, this.weekDays);
    }

    @XmlTransient
    public MarketHoliday[] getHolidays() {
        if (holidays == null) {
            return new MarketHoliday[0];
        }
        return holidays.toArray(new MarketHoliday[holidays.size()]);
    }

    public void setHolidays(MarketHoliday[] holidays) {
        Object oldValue = this.holidays;
        this.holidays = new TreeSet<MarketHoliday>(Arrays.asList(holidays));
        propertyChangeSupport.firePropertyChange(PROP_HOLIDAYS, oldValue, this.holidays);
    }

    @XmlTransient
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        Object oldValue = this.timeZone;
        this.timeZone = timeZone;
        propertyChangeSupport.firePropertyChange(PROP_TIMEZONE, oldValue, this.timeZone);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.markets.IMarket#isOpen()
     */
    @Override
    public boolean isOpen() {
        return isOpen(Calendar.getInstance().getTime());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.model.markets.IMarket#isOpen(java.util.Date)
     */
    @Override
    public boolean isOpen(Date time) {
        MarketDay day = getMarketDayFor(time);
        return day.isOpen(time);
    }

    protected Date getCombinedDateTime(Date date, Date time) {
        Calendar normalized = Calendar.getInstance(timeZone != null ? timeZone : TimeZone.getDefault());
        Calendar refTime = Calendar.getInstance();
        refTime.setTime(date);
        normalized.set(Calendar.YEAR, refTime.get(Calendar.YEAR));
        normalized.set(Calendar.MONTH, refTime.get(Calendar.MONTH));
        normalized.set(Calendar.DAY_OF_MONTH, refTime.get(Calendar.DAY_OF_MONTH));
        refTime = Calendar.getInstance();
        refTime.setTime(time);
        normalized.set(Calendar.HOUR_OF_DAY, refTime.get(Calendar.HOUR_OF_DAY));
        normalized.set(Calendar.MINUTE, refTime.get(Calendar.MINUTE));
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized.getTime();
    }

    protected String getHolidayDescriptionFor(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.MILLISECOND, 0);

        if (holidays != null) {
            Calendar holiday = Calendar.getInstance();
            for (MarketHoliday day : holidays) {
                holiday.setTime(day.getDate());
                if (calendar.get(Calendar.YEAR) == holiday.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == holiday.get(Calendar.MONTH) && calendar.get(Calendar.DAY_OF_MONTH) == holiday.get(Calendar.DAY_OF_MONTH)) {
                    return day.getDescription();
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getToday()
     */
    @Override
    public IMarketDay getToday() {
        return getMarketDayFor(Calendar.getInstance().getTime());
    }

    protected MarketDay getMarketDayFor(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.MILLISECOND, 0);

        if (holidays != null) {
            Calendar holiday = Calendar.getInstance();
            for (MarketHoliday day : holidays) {
                holiday.setTime(day.getDate());
                if (calendar.get(Calendar.YEAR) == holiday.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == holiday.get(Calendar.MONTH) && calendar.get(Calendar.DAY_OF_MONTH) == holiday.get(Calendar.DAY_OF_MONTH)) {
                    if (day.getOpenTime() == null || day.getCloseTime() == null) {
                        return new MarketDay(null, null, day.getDescription());
                    }
                    return new MarketDay(getCombinedDateTime(day.getOpenTime(), day.getOpenTime()), getCombinedDateTime(day.getCloseTime(), day.getCloseTime()), day.getDescription());
                }
            }
        }

        if (weekDays != null) {
            if (!weekDays.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                return new MarketDay(null, null, null);
            }
        }

        if (schedule != null) {
            for (MarketTime marketTime : schedule) {
                Date openTime = getCombinedDateTime(time, marketTime.getOpenTime());
                Date closeTime = getCombinedDateTime(time, marketTime.getCloseTime());
                if (!marketTime.isExcluded(time) && (time.equals(openTime) || time.after(openTime)) && time.before(closeTime)) {
                    return new MarketDay(openTime, closeTime, marketTime.getDescription());
                }
            }
            for (MarketTime marketTime : schedule) {
                Date openTime = getCombinedDateTime(time, marketTime.getOpenTime());
                Date closeTime = getCombinedDateTime(time, marketTime.getCloseTime());
                if (openTime.equals(time) || openTime.after(time)) {
                    return new MarketDay(openTime, closeTime, null);
                }
            }
        }

        return new MarketDay(null, null, null);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getNextDay()
     */
    @Override
    public IMarketDay getNextDay() {
        return getNextMarketDayFor(Calendar.getInstance().getTime());
    }

    protected MarketDay getNextMarketDayFor(Date time) {
        Calendar calendar = Calendar.getInstance(timeZone != null ? timeZone : TimeZone.getDefault());
        calendar.setTime(time);
        calendar.set(Calendar.MILLISECOND, 0);

        MarketDay day = getMarketDayFor(time);
        if (day.getOpenTime() != null && time.before(day.getOpenTime())) {
            return day;
        }

        if (schedule != null && schedule.size() != 0) {
            Calendar refTime = Calendar.getInstance();
            refTime.setTime(schedule.first().getOpenTime());
            calendar.set(Calendar.HOUR_OF_DAY, refTime.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, refTime.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            for (int i = 0; i < 7; i++) {
                calendar.add(Calendar.DATE, 1);
                day = getMarketDayFor(calendar.getTime());
                if (day.isOpen(calendar.getTime())) {
                    return day;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#addMember(org.eclipsetrader.core.instruments.ISecurity[])
     */
    @Override
    public void addMembers(ISecurity[] securities) {
        if (members == null) {
            members = new ArrayList<ISecurity>();
        }

        Object oldValue = getMembers();

        for (ISecurity security : securities) {
            if (!members.contains(security)) {
                members.add(security);
            }
        }

        propertyChangeSupport.firePropertyChange(PROP_MEMBERS, oldValue, getMembers());
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#removeMember(org.eclipsetrader.core.instruments.ISecurity[])
     */
    @Override
    public void removeMembers(ISecurity[] securities) {
        if (members != null) {
            Object oldValue = getMembers();
            members.removeAll(Arrays.asList(securities));
            propertyChangeSupport.firePropertyChange(PROP_MEMBERS, oldValue, getMembers());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getMembers()
     */
    @Override
    @XmlTransient
    public ISecurity[] getMembers() {
        if (members == null) {
            return new ISecurity[0];
        }
        return members.toArray(new ISecurity[members.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#hasMember(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean hasMember(ISecurity security) {
        return members != null && members.contains(security);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getLiveFeedConnector()
     */
    @Override
    @XmlTransient
    public IFeedConnector getLiveFeedConnector() {
        return liveFeedConnector != null ? liveFeedConnector.getConnector() : null;
    }

    public void setLiveFeedConnector(IFeedConnector liveFeedConnector) {
        Object oldValue = this.liveFeedConnector != null ? this.liveFeedConnector.getConnector() : null;
        this.liveFeedConnector = liveFeedConnector != null ? new MarketConnector(liveFeedConnector) : null;
        propertyChangeSupport.firePropertyChange(PROP_LIVE_FEED_CONNECTOR, oldValue, this.liveFeedConnector != null ? this.liveFeedConnector.getConnector() : null);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getBackfillConnector()
     */
    @Override
    @XmlTransient
    public IBackfillConnector getBackfillConnector() {
        return backfillConnector != null ? backfillConnector.getConnector() : null;
    }

    public void setBackfillConnector(IBackfillConnector backfillConnector) {
        this.backfillConnector = backfillConnector != null ? new MarketBackfillConnector(backfillConnector) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.markets.IMarket#getIntradayBackfillConnector()
     */
    @Override
    @XmlTransient
    public IBackfillConnector getIntradayBackfillConnector() {
        return intradayBackfillConnector != null ? intradayBackfillConnector.getConnector() : null;
    }

    public void setIntradayBackfillConnector(IBackfillConnector intradayBackfillConnector) {
        this.intradayBackfillConnector = intradayBackfillConnector != null ? new MarketBackfillConnector(intradayBackfillConnector) : null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 7 * name.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(PropertyChangeSupport.class)) {
            return propertyChangeSupport;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return super.getAdapter(adapter);
    }
}
