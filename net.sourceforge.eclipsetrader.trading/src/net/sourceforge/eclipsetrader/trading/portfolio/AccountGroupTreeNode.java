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

import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;

public class AccountGroupTreeNode
{
    AccountGroup value;
    AccountGroupTreeNode parent;
    Object[] childrens = new Object[0];
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
                    groups.add(i, new AccountGroupTreeNode(AccountGroupTreeNode.this, newGroup));
                    updateChildrens();
                    if (listener != null)
                        listener.refresh(this);
                    return;
                }
            }
            
            groups.add(new AccountGroupTreeNode(AccountGroupTreeNode.this, newGroup));
            updateChildrens();
            if (listener != null)
                listener.refresh(this);
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
                    if (listener != null)
                        listener.refresh(this);
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
                    accounts.add(i, new AccountTreeNode(AccountGroupTreeNode.this, newAccount));
                    updateChildrens();
                    if (listener != null)
                        listener.refresh(this);
                    return;
                }
            }
            
            accounts.add(new AccountTreeNode(AccountGroupTreeNode.this, newAccount));
            updateChildrens();
            if (listener != null)
                listener.refresh(this);
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
                    if (listener != null)
                        listener.refresh(this);
                    return;
                }
            }
        }
    };
    
    AccountGroupTreeNode(AccountGroupTreeNode parent, AccountGroup group)
    {
        this.parent = parent;
        this.value = group;

        List list = new ArrayList(value.getGroups());
        Collections.sort(list, AccountGroupComparator.getInstance());
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            groups.add(new AccountGroupTreeNode(this, (AccountGroup)iter.next()));

        list = new ArrayList(value.getAccounts());
        Collections.sort(list, AccountComparator.getInstance());
        for (Iterator iter = list.iterator(); iter.hasNext(); )
            accounts.add(new AccountTreeNode(this, (Account)iter.next()));

        updateChildrens();
        
        value.getGroups().addCollectionObserver(groupsCollectionObserver);
        value.getAccounts().addCollectionObserver(accountsCollectionObserver);
    }
    
    public void dispose()
    {
        value.getGroups().removeCollectionObserver(groupsCollectionObserver);
        value.getAccounts().removeCollectionObserver(accountsCollectionObserver);
        listener = null;

        Object[] items = groups.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountGroupTreeNode)items[i]).dispose();

        items = accounts.toArray();
        for (int i = 0; i < items.length; i++)
            ((AccountTreeNode)items[i]).dispose();
    }
    
    public AccountGroupTreeNode getParent()
    {
        return parent;
    }

    public boolean hasChildren()
    {
        return childrens.length != 0;
    }
    
    public Object[] getChildren()
    {
        return childrens;
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
        childrens = list.toArray();
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
        if (value.getId().equals(id))
            return this;
        
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
