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

import org.eclipsetrader.core.internal.trading.OrderType;

public interface IOrderType {

    public static final IOrderType Market = new OrderType("market", Messages.IOrderType_Market); //$NON-NLS-1$
    public static final IOrderType Limit = new OrderType("limit", Messages.IOrderType_Limit); //$NON-NLS-1$
    public static final IOrderType Stop = new OrderType("stop", Messages.IOrderType_Stop); //$NON-NLS-1$
    public static final IOrderType StopLimit = new OrderType("stop-limit", Messages.IOrderType_StopLimit); //$NON-NLS-1$

    public String getId();

    public String getName();
}
