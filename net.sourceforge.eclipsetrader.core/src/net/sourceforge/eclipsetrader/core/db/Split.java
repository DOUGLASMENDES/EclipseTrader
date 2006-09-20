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

import java.util.Calendar;
import java.util.Date;

public class Split
{
    Date date = Calendar.getInstance().getTime();
    int fromQuantity;
    int toQuantity;

    public Split()
    {
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public int getFromQuantity()
    {
        return fromQuantity;
    }

    public void setFromQuantity(int value)
    {
        this.fromQuantity = value;
    }

    public int getToQuantity()
    {
        return toQuantity;
    }

    public void setToQuantity(int toQuantity)
    {
        this.toQuantity = toQuantity;
    }
}
