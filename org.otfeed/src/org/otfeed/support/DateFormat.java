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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date format helper. Default format is "MM/dd/yyyy HH:mm.ss".
 * See documantation for <code>java.text.SimpleDateFormat</code> 
 * for the explanation of possible patterns.
 */
public class DateFormat implements IFormat<Date> {
	
	private static final String DEFAULT_FORMAT = "MM/dd/yyyy HH:mm.ss";

	private SimpleDateFormat format;
	
	/**
	 * Creates new DateFormat. The default pattern is
	 * "MM/dd/yyyy HHH:mm.ss"
	 */
	public DateFormat() {
		this(DEFAULT_FORMAT);
	}

	/**
	 * Creates new DateFormat from a given pattern.
	 * @param pattern
	 */
	public DateFormat(String pattern) {
		setPattern(pattern);
	}
	
	/**
	 * Determines the date formatting/parsing pattern.
	 * 
	 * @return pattern value.
	 */
	public String getPattern() { 
		return format.toPattern(); 
	}
	
	/**
	 * Sets pattern value.
	 * 
	 * @param pattern
	 */
	public void setPattern(String pattern) {
		format = new SimpleDateFormat(pattern);
	}

	/**
	 * Formats Date object to string.
	 */
	public String format(Date obj) {
		return format.format(obj);
	}

	/**
	 * Parses Date object from a string.
	 */
	public Date parse(String val) {
		try {
			return format.parse(val);
		} catch (ParseException e) {
			throw new IllegalArgumentException("parsing error: " + e.getMessage());
		}
	}
}
