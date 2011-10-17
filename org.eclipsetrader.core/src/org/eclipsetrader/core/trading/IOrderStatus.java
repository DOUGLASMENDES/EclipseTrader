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

import org.eclipsetrader.core.internal.trading.OrderStatus;

public interface IOrderStatus {

    public static final IOrderStatus New = new OrderStatus("new", Messages.IOrderStatus_New);
    public static final IOrderStatus PendingNew = new OrderStatus("pending-new", Messages.IOrderStatus_PendingNew);
    public static final IOrderStatus Partial = new OrderStatus("partial", Messages.IOrderStatus_Partial);
    public static final IOrderStatus Filled = new OrderStatus("filled", Messages.IOrderStatus_Filled);
    public static final IOrderStatus Canceled = new OrderStatus("canceled", Messages.IOrderStatus_Canceled);
    public static final IOrderStatus Rejected = new OrderStatus("rejected", Messages.IOrderStatus_Rejected);
    public static final IOrderStatus PendingCancel = new OrderStatus("pending-cancel", Messages.IOrderStatus_PendingCancel);
    public static final IOrderStatus Expired = new OrderStatus("expired", Messages.IOrderStatus_Expired);

    public String getId();

    public String getName();
}
