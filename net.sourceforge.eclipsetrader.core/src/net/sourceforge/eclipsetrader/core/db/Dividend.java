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

/**
 * Security dividends data.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class Dividend
{
    Date date = Calendar.getInstance().getTime();
    double value;

    public Dividend()
    {
    }

    /**
     * Returns the dividend date.
     * 
     * @return the date
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Sets the dividend date.
     * 
     * @param date the date
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * Returns the dividend value.
     * 
     * @return the value
     */
    public double getValue()
    {
        return value;
    }

    /**
     * Sets the dividend date.
     * 
     * @param value the value
     */
    public void setValue(double value)
    {
        this.value = value;
    }
}
