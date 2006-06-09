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

package net.sourceforge.eclipsetrader.trading.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PatternSearchItem
{
    private String code;
    private String description;
    private Date date;
    private Double price;
    private String pattern;
    private String opportunity;
    private List children = new ArrayList();

    public PatternSearchItem()
    {
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOpportunity()
    {
        return opportunity;
    }

    public void setOpportunity(String opportunity)
    {
        this.opportunity = opportunity;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public Double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = new Double(price);
    }

    public void setPrice(Double price)
    {
        this.price = price;
    }

    public List getChildren()
    {
        return children;
    }
}
