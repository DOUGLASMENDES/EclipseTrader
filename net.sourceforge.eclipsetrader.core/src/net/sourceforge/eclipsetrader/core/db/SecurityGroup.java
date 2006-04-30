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

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.ObservableList;

public class SecurityGroup extends PersistentObject
{
    private String description;
    private ObservableList groups = new ObservableList();
    private ObservableList securities;

    public SecurityGroup()
    {
    }

    public SecurityGroup(Integer id)
    {
        super(id);
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

    public ObservableList getSecurities()
    {
        if (securities == null)
        {
            securities = new ObservableList();
            for (Iterator iter = getRepository().allSecurities().iterator(); iter.hasNext(); )
            {
                Security security = (Security) iter.next();
                if (this.equals(security.getGroup()))
                    securities.add(security);
            }
        }
        return securities;
    }

    public void setSecurities(ObservableList securities)
    {
        this.securities = securities;
    }
}
