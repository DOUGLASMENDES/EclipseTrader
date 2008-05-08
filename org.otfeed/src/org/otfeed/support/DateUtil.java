/**
 * Copyright 2007 Mike Kroutikov.
 *
 * This program is free software; you can redistribute it and/or modify
 *   it under the terms of the Lesser GNU General Public License as 
 *   published by the Free Software Foundation; either version 3 of
 *   the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Lesser GNU General Public License for more details.
 *
 *   You should have received a copy of the Lesser GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.otfeed.support;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class: provides misc helper functions to conveniently
 * instantiate Date class. Note that this class implicitly uses 
 * current time zone, and, therefore may not be appropriate for the 
 * applications that may be deployed in different timezones.
 */
public final class DateUtil {
	private DateUtil() { } // no instancies!

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 * @param hour hour.
	 * @param minute minute.
	 * @param second second.
	 * @return Date object.
	 */
	public static final Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute, second);
		
		return calendar.getTime();
	}

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 * @param hour hour.
	 * @param minute minute.
	 * @return Date object.
	 */
	public static final Date getDate(int year, int month, int day, int hour, int minute) {
		return getDate(year, month, day, hour, minute, 0);
	}

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 * @param hour hour.
	 * @return Date object.
	 */
	public static final Date getDate(int year, int month, int day, int hour) {
		return getDate(year, month, day, hour, 0, 0);
	}

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 * @return Date object.
	 */
	public static final Date getDate(int year, int month, int day) {
		return getDate(year, month, day, 0, 0, 0);
	}

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @param month month.
	 * @return Date object.
	 */
	public static final Date getDate(int year, int month) {
		return getDate(year, month, 0, 0, 0, 0);
	}

	/**
	 * Builds Date object from date/time components.
	 * 
	 * @param year year.
	 * @return Date object.
	 */
	public static final Date getDate(int year) {
		return getDate(year, 0, 0, 0, 0, 0);
	}
}
