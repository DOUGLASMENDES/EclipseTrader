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
 * Composite, encapsulating a price range.
 * Used as predicate when searching for options.
 * </p>
 * @see OptionChainCommand
 * @see OptionChainSnapshotCommand
 * @see OptionChainWithSnapshotCommand
 */
public final class PriceRange {

	/**
	 * Lower boundary of the price range.
	 */
	public final double min;

	/**
	 * Upper boundary of the price range.
	 */
	public final double max;

	/**
	 * Constructs composite object.
	 */
	public PriceRange(double min, double max) {

		this.min = min;
		this.max = max;

		if(min > max || min < 0.0) {
			throw new IllegalArgumentException(
				"max must not be less than min, and min must not be negative");
		}
	}

	@Override
	public String toString() {
		return "PriceRange: min=" + min + ", max=" + max;
	}
}
