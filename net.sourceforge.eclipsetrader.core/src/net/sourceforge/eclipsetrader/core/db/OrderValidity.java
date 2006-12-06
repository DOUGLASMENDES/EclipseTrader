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

public class OrderValidity
{
    public static OrderValidity DAY = new OrderValidity(1);
    public static OrderValidity IMMEDIATE_OR_CANCEL = new OrderValidity(2);
    public static OrderValidity AT_OPENING = new OrderValidity(3);
    public static OrderValidity AT_CLOSING = new OrderValidity(4);
    public static OrderValidity GOOD_TILL_CANCEL = new OrderValidity(5);
    public static OrderValidity GOOD_TILL_DATE = new OrderValidity(6);
    int value;

    public OrderValidity(int value)
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
        if (!(o instanceof OrderValidity))
            return false;
        return value == ((OrderValidity)o).value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return value;
    }
}
