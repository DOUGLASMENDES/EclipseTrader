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

public interface IOrderMonitor {

    public static final String PROP_ID = "id";
    public static final String PROP_STATUS = "status";
    public static final String PROP_FILLED_QUANTITY = "filledQuantity";
    public static final String PROP_AVERAGE_PRICE = "averagePrice";
    public static final String PROP_MESSAGE = "message";

    public IOrder getOrder();

    public IBroker getBrokerConnector();

    /**
     * Gets the broker assigned order id.
     *
     * @return the order id.
     */
    public String getId();

    public void addOrderMonitorListener(IOrderMonitorListener listener);

    public void removeOrderMonitorListener(IOrderMonitorListener listener);

    public void submit() throws BrokerException;

    public void cancel() throws BrokerException;

    public boolean allowModify();

    public void modify(IOrder order) throws BrokerException;

    public IOrderStatus getStatus();

    public Long getFilledQuantity();

    public Double getAveragePrice();

    public String getMessage();
}
