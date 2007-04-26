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

package net.sourceforge.eclipsetrader.ats.core.events;

import java.util.Date;

import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Security;

public class BarEvent {
	public Date date;

	public Security security;

	public Bar bar;

	public double price;

	public BarEvent(Date date, Security security, double price) {
		this(date, security, null, price);
	}

	public BarEvent(Date date, Security security, Bar bar, double price) {
		this.date = date;
		this.security = security;
		this.bar = bar;
		this.price = price;
	}
}
