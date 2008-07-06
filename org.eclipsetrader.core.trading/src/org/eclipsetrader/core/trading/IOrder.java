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

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Basic interface for trading orders.
 *
 * @since 1.0
 */
public interface IOrder {

	public static final String PROP_ID = "id";
	public static final String PROP_STATUS = "status";
	public static final String PROP_FILLED_QUANTITY = "filledQuantity";
	public static final String PROP_AVERAGE_PRICE = "averagePrice";

	public String getId();

	public Date getDate();

	public IBrokerConnector getBroker();

	public IOrderRoute getRoute();

	public IAccount getAccount();

	public ISecurity getSecurity();

	public OrderType getType();

	public OrderSide getSide();

	public Long getQuantity();

	public Double getPrice();

	public Double getStopPrice();

	public OrderValidity getValidity();

	public Date getExpire();

	public void submit() throws BrokerException;

	public void cancel() throws BrokerException;

	public void modify() throws BrokerException;

	public OrderStatus getStatus();

	public Long getFilledQuantity();

	public Double getAveragePrice();
}
