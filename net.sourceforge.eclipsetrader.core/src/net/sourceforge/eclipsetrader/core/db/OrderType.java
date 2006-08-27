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

public class OrderType
{
    public static OrderType MARKET = new OrderType(1);
    public static OrderType LIMIT = new OrderType(2);
    public static OrderType STOP = new OrderType(3);
    public static OrderType STOPLIMIT = new OrderType(4);
    int value;

    public OrderType(int value)
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
        if (!(o instanceof OrderType))
            return false;
        return value == ((OrderType)o).value;
    }
}
