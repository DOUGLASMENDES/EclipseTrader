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

package net.sourceforge.eclipsetrader.core.db;

public class OrderSide
{
    public static OrderSide BUY = new OrderSide(1);
    public static OrderSide SELL = new OrderSide(2);
    public static OrderSide SELLSHORT = new OrderSide(3);
    public static OrderSide BUYCOVER = new OrderSide(4);
    int value;

    public OrderSide(int value)
    {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return String.valueOf(value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof OrderSide))
            return false;
        return value == ((OrderSide)o).value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return value;
    }
}
