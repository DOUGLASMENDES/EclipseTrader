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
package org.otfeed.command;

import org.otfeed.protocol.request.Check;

/**
 * Represents aggregation time span.
 * <p/>
 * Time span can range from 2 ticks, to years.
 */
public class AggregationSpan {
	
	public final TimeUnitEnum units;
	public final int          length;
	
	/**
	 * Creates a new aggreagation span.
	 * 
	 * @param units time units (DAYS, WEEKS, etc.). A special
	 * unit of {@link TimeUnitEnum#TICKS TICKS} will mean that 
	 * the aggregation will be done over that many ticks.
	 * 
	 * @param length length of aggregation span.
	 */
	public AggregationSpan(TimeUnitEnum units, int length) {
		Check.notNull(units, "null units noit allowed");
		
		if(units == TimeUnitEnum.TICKS && length < 2) {
			throw new IllegalArgumentException("tick span must be 2 or greater");
		} else if(length < 1) {
			throw new IllegalArgumentException("span length must be greater than zero");
		}
		this.units  = units;
		this.length = length;
	}
	
	/**
	 * Returns aggregation span measured in years.
	 * 
	 * @param num number of years (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan years(int num) {
		return new AggregationSpan(TimeUnitEnum.YEARS, num);
	}

	/**
	 * Returns a one-year aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan years() {
		return years(1);
	}
	
	/**
	 * Returns aggregation span measured in months.
	 * 
	 * @param num number of months (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan months(int num) {
		return new AggregationSpan(TimeUnitEnum.MONTHS, num);
	}

	/**
	 * Returns a one-month aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan months() {
		return months(1);
	}

	/**
	 * Returns aggregation span measured in weeks.
	 * 
	 * @param num number of weeks (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan weeks(int num) {
		return new AggregationSpan(TimeUnitEnum.WEEKS, num);
	}

	/**
	 * Returns a one-week aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan weeks() {
		return weeks(1);
	}

	/**
	 * Returns aggregation span measured in days.
	 * 
	 * @param num number of days (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan days(int num) {
		return new AggregationSpan(TimeUnitEnum.DAYS, num);
	}

	/**
	 * Returns a one-day aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan days() {
		return days(1);
	}

	/**
	 * Returns aggregation span measured in hours.
	 * 
	 * @param num number of hours (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan hours(int num) {
		return new AggregationSpan(TimeUnitEnum.HOURS, num);
	}

	/**
	 * Returns a one-hour aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan hours() {
		return hours(1);
	}

	/**
	 * Returns aggregation span measured in minutes.
	 * 
	 * @param num number of minutes (1 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan minutes(int num) {
		return new AggregationSpan(TimeUnitEnum.MINUTES, num);
	}

	/**
	 * Returns a one-minute aggregation span.
	 * 
	 * @return aggregation span.
	 */
	public static AggregationSpan minutes() {
		return minutes(1);
	}

	/**
	 * Returns aggregation span measured in ticks.
	 * 
	 * @param num number of ticks (2 or greater).
	 * @return aggregation span.
	 */
	public static AggregationSpan ticks(int num) {
		return new AggregationSpan(TimeUnitEnum.TICKS, num);
	}
	
	@Override
	public String toString() {
		return "(" + length + " " + units + ")";
	}
}
