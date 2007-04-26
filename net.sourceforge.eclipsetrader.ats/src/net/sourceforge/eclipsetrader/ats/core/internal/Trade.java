/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.core.internal;

import java.util.Date;

import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.core.db.Security;

public class Trade {
	Strategy strategy;

	Security security;

	int bars;

	Date enterDate;

	double enterPrice;

	String enterMessage;

	int quantity;

	Date exitDate;

	double exitPrice;

	String exitMessage;

	public Trade() {
	}

	public Trade(Strategy strategy, Security security, Date date, int quantity, double price, String message) {
		setEntry(strategy, security, date, quantity, price, message);
	}

	public void setEntry(Strategy strategy, Security security, Date date, int quantity, double price, String message) {
		this.strategy = strategy;
		this.security = security;
		enterDate = date;
		enterPrice = price;
		this.quantity = quantity;
		enterMessage = message;
	}

	public void setExit(Date date, double price, String message) {
		exitDate = date;
		exitPrice = price;
		exitMessage = message;
	}

	public int getBars() {
		return bars;
	}

	public Date getEnterDate() {
		return enterDate;
	}

	public String getEnterMessage() {
		return enterMessage;
	}

	public double getEnterPrice() {
		return enterPrice;
	}

	public void setEnterMessage(String enterMessage) {
		this.enterMessage = enterMessage;
	}

	public Date getExitDate() {
		return exitDate;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public void setExitMessage(String exitMessage) {
		this.exitMessage = exitMessage;
	}

	public double getExitPrice() {
		return exitPrice;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public Security getSecurity() {
		return security;
	}

	public int getQuantity() {
		return quantity;
	}
}
