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
 * Composite, encapsulating month and year values.
 * It is used to specify option expiration date.
 * <p/>
 * This class is immutable.
 */
public final class MonthAndYear {

	/**
	 * Month.
	 * Jan = 1, Feb = 2, ... Dec = 12.
	 */
	public final int month;

	/**
	 * Year.
	 */
	public final int year;

	/**
	 * Constructor of the composite object.
	 */
	public MonthAndYear(int month, int year) {

		this.month = month;
		this.year = year;

		if(month < 1 || month > 12) {
			throw new IllegalArgumentException(
				"invalid month value: " + month);
		}

		if(year <= 0) {
			throw new IllegalArgumentException(
				"invalid year value: " + year);
		}
	}

	@Override
	public String toString() {
		return "MonthAndYear: month=" + month + ", year=" + year;
	}
}
