/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.core;

import java.util.Calendar;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.db.Security;

public class Signal {
	Date date;

	Security security;

	SignalSide side;

	SignalType type;

	SignalStatus status;

	double price;

	int quantity;

	String text;

	String message;

	public Signal() {
	}

	public Signal(Security security, SignalSide side, int quantity) {
		this(Calendar.getInstance().getTime(), security, SignalType.MARKET, side, quantity, 0, "");
	}

	public Signal(Security security, SignalType type, SignalSide side, int quantity, double price) {
		this(Calendar.getInstance().getTime(), security, type, side, quantity, price, "");
	}

	public Signal(Security security, SignalType type, SignalSide side, int quantity, double price, String text) {
		this(Calendar.getInstance().getTime(), security, type, side, quantity, price, text);
	}

	public Signal(Date date, Security security, SignalType type, SignalSide side, int quantity, double price, String text) {
		this.date = date;
		this.security = security;
		this.type = type;
		this.side = side;
		this.quantity = quantity;
		this.price = price;
		this.text = text;
		this.status = SignalStatus.NEW;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public SignalSide getSide() {
		return side;
	}

	public void setSide(SignalSide side) {
		this.side = side;
	}

	public SignalStatus getStatus() {
		return status;
	}

	public void setStatus(SignalStatus status) {
		this.status = status;
	}

	public SignalType getType() {
		return type;
	}

	public void setType(SignalType type) {
		this.type = type;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
