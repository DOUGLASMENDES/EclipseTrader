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

import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;

/**
 * Enumerates misc properties of {@link OTDividend}.
 */
public enum DividendPropertyEnum {
	
	APPROXIMATE (0x0001),
	ANNUAL      (0x0002),
	CANADIAN    (0x0004),
	EXTRA		(0x0008),
	FINAL		(0x0010),
	INCREASE	(0x0020),
	SEMIANNUAL	(0x0040),
	STOCK		(0x0080),
	SPECIAL		(0x0100);
	
	public final int code;
	
	private DividendPropertyEnum(int code) {
		this.code = code;
	}
	
	public static final Set<DividendPropertyEnum> combine(DividendPropertyEnum ... vals) {
		Set<DividendPropertyEnum> set = EnumSet.noneOf(DividendPropertyEnum.class);

		for(int i = 0; i < vals.length; i++) {
			if (set.add(vals[i]) == false) {
				throw new IllegalArgumentException(
					"duplicate value: " + vals[i]);
			}
		}

		return Collections.unmodifiableSet(set);
	}
}
