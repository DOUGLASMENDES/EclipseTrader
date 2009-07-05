/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

/**
 * General purpose bar generator.
 * 
 * @since 1.0
 */
public class BarGenerator extends Observable {
	private TimeSpan timeSpan;

	Date dateOpen;
	Double open;
	Double high;
	Double low;
	Double close;
	Long volume;

	Date dateClose;

	/**
	 * Constructs a new instance of the generator using the given time span to aggregate trades.
	 * 
	 * @param timeSpan the aggregation time span.
	 */
	public BarGenerator(TimeSpan timeSpan) {
		this.timeSpan = timeSpan;
	}

	/**
	 * Adds a new trade to the current bar.
	 * <p>If the new trade causes a pending bar to close (the trade time is past the expected close time)
	 * the method returns the closed bar, otherwise it returns <code>null</code>.</p>
	 * 
	 * @param trade the trade to add.
	 */
	public void addTrade(ITrade trade) {
		Calendar c = Calendar.getInstance();
		c.setTime(trade.getTime());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (dateOpen != null && dateClose != null) {
			Date time = c.getTime();

			if (!time.before(dateOpen) && time.before(dateClose)) {
				if (high == null || trade.getPrice() > high)
					high = trade.getPrice();
				if (low == null || trade.getPrice() < low)
					low = trade.getPrice();
				close = trade.getPrice();

				if (trade.getSize() != null)
					volume = volume != null ? volume + trade.getSize() : trade.getSize();
			}

			if (!time.before(dateClose)) {
				setChanged();
				notifyObservers(new Bar(dateOpen, timeSpan, open, high, low, close, volume));
				dateOpen = null;
			}
		}

		if (dateOpen == null) {
			int round = c.get(Calendar.MINUTE) % timeSpan.getLength();
			c.add(Calendar.MINUTE, -round);
			dateOpen = c.getTime();
			c.add(Calendar.MINUTE, timeSpan.getLength());
			dateClose = c.getTime();

			open = trade.getPrice();
			high = trade.getPrice();
			low = trade.getPrice();
			close = trade.getPrice();
			volume = trade.getSize();

			setChanged();
			notifyObservers(new BarOpen(dateOpen, timeSpan, open));
		}
	}

	/**
	 * Forces the current pending bar to close.
	 */
	public void forceBarClose() {
		if (dateOpen == null)
			return;

		setChanged();
		notifyObservers(new Bar(dateOpen, timeSpan, open, high, low, close, volume));

		dateOpen = null;
	}

	/**
	 * Gets if the there is a pending bar that is expired (current time is past the expected
	 * close time).
	 * 
	 * @return <code>true</code> if there is a pending expired bar.
	 */
	public boolean isBarExpired() {
		if (dateOpen == null || dateClose == null)
			return false;
		return new Date().after(dateClose);
	}
}
