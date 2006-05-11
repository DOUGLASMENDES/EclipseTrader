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

public class AccountGroup extends PersistentObject
{
    private AccountGroup parent;
    private String description;
    private ObservableList groups = new ObservableList();
    private ObservableList accounts;

    public AccountGroup()
    {
    }

    public AccountGroup(Integer id)
    {
        super(id);
    }

    public AccountGroup getParent()
    {
        return parent;
    }

    public void setParent(AccountGroup parent)
    {
        this.parent = parent;
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

    public ObservableList getAccounts()
    {
        if (accounts == null)
        {
            accounts = new ObservableList();
            if (getRepository() != null)
            {
                for (Iterator iter = getRepository().allAccounts().iterator(); iter.hasNext(); )
                {
                    Account account = (Account) iter.next();
                    if (this.equals(account.getGroup()))
                        accounts.add(account);
                }
            }
        }
        return accounts;
    }

    public void setAccounts(ObservableList accounts)
    {
        this.accounts = accounts;
    }
}
