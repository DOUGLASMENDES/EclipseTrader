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

/**
 * Enumerates time units used for aggregation.
 * {@link #TICKS} is a special "quasi-time" unit, specifying
 * how many ticks to aggregate. For example, asking for
 * {@link #DAYS} with interval 1 will return trade quotes
 * aggregated during the day; {@link #MINUTES} with interval 5
 * will return quotes aggregated on a 5-minute basis;
 * {@link #TICKS} with interval 10 will return quotes 
 * aggregated on a 10-tick basis.
 * 
 * See also {@link org.otfeed.command.HistDataCommand HistDataCommand}.
 */
public enum TimeUnitEnum {

	/** Ticks */
	TICKS   (2),
	
	/** Minutes */
	MINUTES (3),
	
	/** Hours */
	HOURS   (4),
	
	/** Days */
	DAYS    (5),
	
	/** Weeks */
	WEEKS   (6),
	
	/** Months */
	MONTHS  (7),
	
	/** Years */
	YEARS   (8);

	public final int code;
	
	private TimeUnitEnum(int code) {
		this.code = code;
	}
}
