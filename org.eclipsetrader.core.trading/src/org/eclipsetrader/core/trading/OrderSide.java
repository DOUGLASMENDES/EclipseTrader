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

package org.eclipsetrader.core.trading;

public enum OrderSide {
	Buy("buy"),
	Sell("sell"),
	SellShort("sell-short"),
	BuyCover("buy-cover");

	private String name;

	private OrderSide(String name) {
	    this.name = name;
    }

	public String getName() {
    	return name;
    }
}
