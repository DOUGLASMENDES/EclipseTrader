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

package org.otfeed.event;

import java.util.EnumSet;
import java.util.Set;
import java.util.Collections;


/**
 * Enumerates misc properties of {@link OTTrade}.
 */
public enum TradePropertyEnum {
	
	/** Trade provides opening price for the day */
	OPEN			(0x01),

	/** Trade provides the high price for the day */
	HIGH			(0x02),

	/** Trade provides the low price for the day */
	LOW				(0x04),

	/** Trade provides the high price for the day */
	CLOSE			(0x08),

	/** Trade updates the last trade price */
	UPDATE_LAST		(0x10),

	/** Set if volume is the updated consolidated volume */
	UPDATE_VOLUME	(0x20),

	/** Trade is the cancel for the previous trade */
	CANCEL			(0x40),

	/** Trade is from an ECN book */
	FROM_BOOK		(0x80);

	public final int code;
	
	private TradePropertyEnum(int code) {
		this.code = code;
	}

	public static final Set<TradePropertyEnum> combine(TradePropertyEnum ... vals) {
		Set<TradePropertyEnum> set = EnumSet.noneOf(TradePropertyEnum.class);

		for(int i = 0; i < vals.length; i++) {
			if (set.add(vals[i]) == false) {
				throw new IllegalArgumentException(
					"duplicate value: " + vals[i]);
			}
		}

		return Collections.unmodifiableSet(set);
	}
}
