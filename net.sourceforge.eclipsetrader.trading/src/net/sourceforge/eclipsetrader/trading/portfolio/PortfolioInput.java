/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.portfolio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;

public class PortfolioInput
{
    Object[] elements = new Object[0];
    List groups = new ArrayList();
    List accounts = new ArrayList();
    IStructuredViewerListener listener;
    private ICollectionObserver groupsCollectionObserver = new ICollectionObserver() {
        public void itemAdded(Object o)
        {
            AccountGroup newGroup = (AccountGroup)o;

            Object items[] = groups.toArray();
            for (int i = 0; i < items.length; i++)
            {
                AccountGroup arg1 = ((AccountGroupTreeNode)items[i]).value;
                if (AccountGroupComparator.getInstance().compare(newGroup, arg1) < 0)
                {
                    groups.add(i, new AccountGroupTreeNode(null, newGroup));
                    updateChildrens();
                    return;
                }
            }
            
            groups.add(new AccountGroupTreeNode(null, newGroup));
            updateChildrens();
        }

        public void itemRemoved(Object o)
        {
            AccountGroup newGroup = (AccountGroup)o;

            Object items[] = groups.toArray();
            for (int i = 0; i < items.length; i++)
            {
                if (newGroup == ((AccountGroupTreeNode)items[i]).value);
                {
                    ((AccountGroupTreeNode)items[i]).dispose();
                    groups.remove(i);
                    updateChildrens();
                    return;
                }
            }
        }
    };
    private ICollectionObserver accountsCollectionObserver = new ICollectionObserver() {
        public void itemAdded(Object o)
        {
            Account newAccount = (Account)o;

            Object items[] = accounts.toArray();
            for (int i = 0; i < items.length; i++)
            {
                Account arg1 = ((AccountTreeNode)items[i]).value;
                if (AccountComparator.getInstance().compare(newAccount, arg1) < 0)
                {
                    accounts.add(i, new AccountTreeNode(null, newAccount));
                    updateChildrens();
                    return;
                }
            }
            
            accounts.add(new AccountTreeNode(null, newAccount));
            updateChildrens();
        }

        public void itemRemoved(Object o)
        {
            Account newAccount = (Account)o;

            Object items[] = accounts.toArray();
            for (int i = 0; i < items.length; i++)
            {
                if (newAccount == ((AccountTreeNode)items[i]).value);
                {
                    ((AccountTreeNode)items[i]).dispose();
                    accounts.remove(i);
                    updateChildrens();
                    return;
                }
            }
        }
    };

    public PortfolioInput()
    {
        List list = new ArrayList(CorePlugin.getRepository().allAccountGroups());
        Collections.sort(list, AccountGroupComparator.getInstance());
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            groups.add(new AccountGroupTreeNode(null, (AccountGroup)iter.next()));

        list = new ArrayList(CorePlugin.getRepository().allAccounts());
        Collections.sort(list, AccountComparator.getInstance());
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            accounts.add(new AccountTreeNode(null, (Account)iter.next()));
        
        updateChildrens();
        
        CorePlugin.getRepository().allAccountGroups().addCollectionObserver(groupsCollectionObserver);
        CorePlugin.getRepository().allAccounts().addCollectionObserver(accountsCollectionObserver);
    }
    
    public void dispose()
    {
        CorePlugin.getRepository().allAccountGroups().removeCollectionObserver(groupsCollectionObserver);
        CorePlugin.getRepository().allAccounts().removeCollectionObserver(accountsCollectionObserver);
        listener = null;

        Object[] items = groups.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountGroupTreeNode)items[i]).dispose();

        items = accounts.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountTreeNode)items[i]).dispose();
    }
    
    public Object[] getElements()
    {
        return elements;
    }

    public void setListener(IStructuredViewerListener listener)
    {
        this.listener = listener;

        Object[] items = groups.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountGroupTreeNode)items[i]).setListener(listener);

        items = accounts.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountTreeNode)items[i]).setListener(listener);
    }
    
    void updateChildrens()
    {
        List list = new ArrayList();
        list.addAll(groups);
        list.addAll(accounts);
        elements = list.toArray();
    }
    
    public AccountTreeNode getAccountNode(Integer id)
    {
        Object[] items = accounts.toArray();
        for (int i = 0; i < items.length; i++)
        {
            if (((AccountTreeNode)items[i]).value.getId().equals(id))
                return (AccountTreeNode)items[i];
        }

        return null;
    }
    
    public AccountGroupTreeNode getAccountGroupNode(Integer id)
    {
        Object[] items = groups.toArray();
        for (int i = 0; i < items.length; i++)
        {
            AccountGroupTreeNode node = ((AccountGroupTreeNode)items[i]).getAccountGroupNode(id);
            if (node != null)
                return node;
        }

        return null;
    }
}
