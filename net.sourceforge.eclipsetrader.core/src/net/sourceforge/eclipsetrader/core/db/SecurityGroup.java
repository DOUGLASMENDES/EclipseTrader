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

import java.util.Currency;

import net.sourceforge.eclipsetrader.core.ObservableList;

public class SecurityGroup extends PersistentObject
{
    String code = "";
    String description = "";
    Currency currency;
    SecurityGroup group;
    ObservableList groups = new ObservableList();
    ObservableList securities = new ObservableList();

    public SecurityGroup()
    {
    }

    public SecurityGroup(Integer id)
    {
        super(id);
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        setChanged();
    }

    public Currency getCurrency()
    {
        if (currency == null && group != null)
            return group.getCurrency();
        return currency;
    }

    public void setCurrency(Currency currency)
    {
        this.currency = currency;
    }

    public SecurityGroup getGroup()
    {
        return group;
    }

    public void setGroup(SecurityGroup group)
    {
        this.group = group;
    }

    public ObservableList getGroups()
    {
        return groups;
    }

    public void setGroups(ObservableList groups)
    {
        this.groups = groups;
    }

    public ObservableList getSecurities()
    {
        return securities;
    }

    public void setSecurities(ObservableList securities)
    {
        this.securities = securities;
    }
}
