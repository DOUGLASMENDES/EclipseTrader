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

package net.sourceforge.eclipsetrader.core.db.trading;

import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;

public class TradingSystemGroup extends PersistentObject
{
    private TradingSystemGroup parent;
    private String description;
    private ObservableList groups = new ObservableList();
    private ObservableList tradingSystems = new ObservableList();

    public TradingSystemGroup()
    {
    }

    public TradingSystemGroup(Integer id)
    {
        super(id);
    }

    public TradingSystemGroup getParent()
    {
        return parent;
    }

    public void setParent(TradingSystemGroup parent)
    {
        this.parent = parent;
        setChanged();
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

    public ObservableList getGroups()
    {
        return groups;
    }

    public void setGroups(ObservableList groups)
    {
        this.groups = groups;
    }

    public ObservableList getTradingSystems()
    {
        return tradingSystems;
    }

    public void setTradingSystems(ObservableList strategies)
    {
        this.tradingSystems = strategies;
    }
}
