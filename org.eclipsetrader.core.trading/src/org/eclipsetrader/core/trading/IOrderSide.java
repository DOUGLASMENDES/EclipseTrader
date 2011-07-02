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

import org.eclipsetrader.core.internal.trading.OrderSide;

public interface IOrderSide {

    public static final IOrderSide Buy = new OrderSide("buy", Messages.IOrderSide_Buy); //$NON-NLS-1$
    public static final IOrderSide Sell = new OrderSide("sell", Messages.IOrderSide_Sell); //$NON-NLS-1$
    public static final IOrderSide BuyCover = new OrderSide("buy-cover", Messages.IOrderSide_BuyCover); //$NON-NLS-1$
    public static final IOrderSide SellShort = new OrderSide("sell-short", Messages.IOrderSide_SellShort); //$NON-NLS-1$

    public String getId();

    public String getName();
}
