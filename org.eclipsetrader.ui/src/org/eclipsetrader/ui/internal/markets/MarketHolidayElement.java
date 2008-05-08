/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.markets;

import java.util.Date;

import org.eclipsetrader.core.internal.markets.MarketHoliday;

public class MarketHolidayElement {
	private Date date;
	private Date openTime;
	private Date closeTime;
	private String description;

	public MarketHolidayElement() {
	}

	public MarketHolidayElement(MarketHoliday day) {
		date = day.getDate();
		openTime = day.getOpenTime();
		closeTime = day.getCloseTime();
		description = day.getDescription();
	}

	public MarketHoliday getMarketHoliday() {
		return new MarketHoliday(date, description, openTime, closeTime);
	}

	public Date getDate() {
    	return date;
    }

	public void setDate(Date date) {
    	this.date = date;
    }

	public String getDescription() {
    	return description;
    }

	public void setDescription(String description) {
    	this.description = description;
    }

	public Date getOpenTime() {
    	return openTime;
    }

	public void setOpenTime(Date openTime) {
    	this.openTime = openTime;
    }

	public Date getCloseTime() {
    	return closeTime;
    }

	public void setCloseTime(Date closeTime) {
    	this.closeTime = closeTime;
    }
}
