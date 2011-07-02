/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core;

import java.util.Currency;
import java.util.Date;

import org.eclipsetrader.core.instruments.ICurrencyExchange;

public interface ICurrencyService {

    public Currency[] getAvailableCurrencies();

    public Cash convert(Cash cash, Currency currency);

    public Cash convert(Cash cash, Currency currency, Date date);

    public void addExchange(ICurrencyExchange exchange);

    public void removeExchange(ICurrencyExchange exchange);
}
