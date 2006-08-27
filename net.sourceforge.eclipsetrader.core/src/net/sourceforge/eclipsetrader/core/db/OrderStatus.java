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

public class OrderStatus
{
    public static OrderStatus NEW = new OrderStatus(0);
    public static OrderStatus PARTIAL = new OrderStatus(1);
    public static OrderStatus FILLED = new OrderStatus(2);
    public static OrderStatus CANCELED = new OrderStatus(3);
    public static OrderStatus REJECTED = new OrderStatus(4);
    public static OrderStatus PENDING_CANCEL = new OrderStatus(5);
    public static OrderStatus PENDING_NEW = new OrderStatus(6);
    int value;

    public OrderStatus(int value)
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
        if (!(o instanceof OrderStatus))
            return false;
        return value == ((OrderStatus)o).value;
    }
}
