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

package org.eclipsetrader.core.trading;

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Basic interface for trading orders.
 *
 * @since 1.0
 */
public interface IOrder {

    public Date getDate();

    public IOrderRoute getRoute();

    public IAccount getAccount();

    public ISecurity getSecurity();

    public IOrderType getType();

    public IOrderSide getSide();

    public Long getQuantity();

    public Double getPrice();

    public Double getStopPrice();

    public IOrderValidity getValidity();

    public Date getExpire();

    public String getReference();
}
