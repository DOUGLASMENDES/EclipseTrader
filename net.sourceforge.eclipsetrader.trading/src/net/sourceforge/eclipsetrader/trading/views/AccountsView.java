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

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.actions.NewAccountGroupAction;
import net.sourceforge.eclipsetrader.trading.internal.DeleteAccountAction;
import net.sourceforge.eclipsetrader.trading.wizards.accounts.AccountSettingsAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class AccountsView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.accounts";
    private Tree tree;
    private NumberFormat nf = NumberFormat.getInstance();
    private Action deleteAction = new DeleteAccountAction(this);
    private Action propertiesAction = new AccountSettingsAction(this);
    private Comparator groupComparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            return ((AccountGroup)arg0).getDescription().compareTo(((AccountGroup)arg1).getDescription());
        }
    };
    private Comparator accountComparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            return ((Account)arg0).getDescription().compareTo(((Account)arg1).getDescription());
        }
    };
    
    public AccountsView()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
        
        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        tree = new Tree(content, SWT.FULL_SELECTION|SWT.SINGLE);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateSelection();
            }
        });
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Balance");
        tree.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                int width = tree.getSize().x - 60;
                if (tree.getVerticalBar() != null)
                    width -= tree.getVerticalBar().getSize().x + 5;
                if (width < 100) width = 100;
                tree.getColumn(0).setWidth(width);
                tree.getColumn(1).setWidth(60 - (tree.getVerticalBar().getSize().x + 5));
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (tree.getItem(new Point(e.x, e.y)) == null)
                {
                    tree.deselectAll();
                    updateSelection();
                }
            }
        });

        getSite().setSelectionProvider(new SelectionProvider());

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new NewAccountGroupAction(AccountsView.this));
                menuManager.add(new Separator("group1")); //$NON-NLS-1$
                menuManager.add(new Separator("group2")); //$NON-NLS-1$
                menuManager.add(new Separator("group3")); //$NON-NLS-1$
                menuManager.add(new Separator("group4")); //$NON-NLS-1$
                menuManager.add(new Separator("group5")); //$NON-NLS-1$
                menuManager.add(deleteAction);
                menuManager.add(new Separator("group6")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(propertiesAction);
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        tree.setMenu(menuMgr.createContextMenu(tree));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("delete", deleteAction);
        actionBars.setGlobalActionHandler("properties", propertiesAction);

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                updateSelection();
                CorePlugin.getRepository().allAccountGroups().addCollectionObserver(AccountsView.this);
                CorePlugin.getRepository().allAccounts().addCollectionObserver(AccountsView.this);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        tree.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        CorePlugin.getRepository().allAccountGroups().removeCollectionObserver(AccountsView.this);
        CorePlugin.getRepository().allAccounts().removeCollectionObserver(AccountsView.this);
        super.dispose();
    }

    private void updateView()
    {
        List list = CorePlugin.getRepository().allAccountGroups();
        Collections.sort(list, groupComparator);
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            AccountGroup account = (AccountGroup)iter.next();
            if (account.getParent() == null)
                new GroupTreeItem(account, tree, SWT.NONE);
        }

        list = CorePlugin.getRepository().allAccounts();
        Collections.sort(list, accountComparator);
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Account account = (Account)iter.next();
            if (account.getGroup() == null)
                new AccountTreeItem(account, tree, SWT.NONE);
        }
    }

    private void updateSelection()
    {
        TreeItem[] selection = tree.getSelection();
        if (selection != null && selection.length == 1)
        {
            Object item = selection[0].getData();
            if (item instanceof Account)
                getSite().getSelectionProvider().setSelection(new AccountSelection((Account)item));
            else if (item instanceof AccountGroup)
                getSite().getSelectionProvider().setSelection(new AccountGroupSelection((AccountGroup)item));
            else
                getSite().getSelectionProvider().setSelection(new NullSelection());
        }
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());
        
        deleteAction.setEnabled(!(getSite().getSelectionProvider().getSelection() instanceof NullSelection));
        propertiesAction.setEnabled(!(getSite().getSelectionProvider().getSelection() instanceof NullSelection));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        if (o instanceof Account)
        {
            Account account = (Account)o;
            if (account.getGroup() != null)
                return;

            TreeItem items[] = tree.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (!(items[i].getData() instanceof Account))
                    continue;
                Account arg1 = (Account)items[i].getData();
                if (accountComparator.compare(account, arg1) < 0)
                {
                    new AccountTreeItem(account, tree, SWT.NONE, i);
                    updateSelection();
                    return;
                }
            }
            
            new AccountTreeItem(account, tree, SWT.NONE);
            updateSelection();
        }
        else if (o instanceof AccountGroup)
        {
            AccountGroup group = (AccountGroup)o;
            if (group.getParent() != null)
                return;

            TreeItem items[] = tree.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (!(items[i].getData() instanceof AccountGroup))
                {
                    new GroupTreeItem((AccountGroup)o, tree, SWT.NONE, i);
                    updateSelection();
                    return;
                }
                AccountGroup arg1 = (AccountGroup)items[i].getData();
                if (groupComparator.compare(group, arg1) < 0)
                {
                    new GroupTreeItem(group, tree, SWT.NONE, i);
                    updateSelection();
                    return;
                }
            }
            
            new GroupTreeItem(group, tree, SWT.NONE);
            updateSelection();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (items[i].getData().equals(o))
                items[i].dispose();
        }
        updateSelection();
    }
    
    public Tree getTree()
    {
        return tree;
    }
    
    private class AccountTreeItem extends TreeItem implements Observer
    {
        private Account account;
        
        public AccountTreeItem(Account account, Tree parent, int style, int index)
        {
            super(parent, style, index);
            init(account);
        }

        public AccountTreeItem(Account account, Tree parent, int style)
        {
            super(parent, style);
            init(account);
        }

        public AccountTreeItem(Account account, TreeItem parentItem, int style, int index)
        {
            super(parentItem, style, index);
            init(account);
        }

        public AccountTreeItem(Account account, TreeItem parentItem, int style)
        {
            super(parentItem, style);
            init(account);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TreeItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        private void init(Account account)
        {
            this.account = account;
            
            setText(account.getDescription());
            setText(1, nf.format(account.getBalance()));
            setData(account);

            account.addObserver(this);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    AccountTreeItem.this.account.deleteObserver(AccountTreeItem.this);
                }
            });
        }

        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!isDisposed())
                    {
                        setText(account.getDescription());
                        setText(1, nf.format(account.getBalance()));
                    }
                }
            });
        }
    }
    
    private class GroupTreeItem extends TreeItem implements ICollectionObserver, Observer
    {
        private AccountGroup group;

        public GroupTreeItem(AccountGroup group, Tree parent, int style, int index)
        {
            super(parent, style, index);
            init(group);
        }

        public GroupTreeItem(AccountGroup group, Tree parent, int style)
        {
            super(parent, style);
            init(group);
        }

        public GroupTreeItem(AccountGroup group, TreeItem parentItem, int style, int index)
        {
            super(parentItem, style, index);
            init(group);
        }

        public GroupTreeItem(AccountGroup group, TreeItem parentItem, int style)
        {
            super(parentItem, style);
            init(group);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TreeItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        private void init(AccountGroup group)
        {
            this.group = group;
            
            FontData fd = getFont().getFontData()[0];
            Font font = new Font(null, fd.getName(), fd.getHeight(), SWT.BOLD);
            setFont(font);
            
            setText(group.getDescription());
            setData(group);

            List list = group.getGroups();
            Collections.sort(list, groupComparator);
            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                AccountGroup grp = (AccountGroup)iter.next();
                new GroupTreeItem(grp, this, SWT.NONE);
            }

            list = group.getAccounts();
            Collections.sort(list, accountComparator);
            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                Account account = (Account)iter.next();
                new AccountTreeItem(account, this, SWT.NONE);
            }
            
            group.addObserver(this);
            group.getGroups().addCollectionObserver(this);
            group.getAccounts().addCollectionObserver(this);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    GroupTreeItem.this.group.deleteObserver(GroupTreeItem.this);
                    GroupTreeItem.this.group.getGroups().removeCollectionObserver(GroupTreeItem.this);
                    GroupTreeItem.this.group.getAccounts().removeCollectionObserver(GroupTreeItem.this);
                }
            });
        }

        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!isDisposed())
                        setText(group.getDescription());
                }
            });
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            if (o instanceof Account)
            {
                Account account = (Account)o;

                TreeItem items[] = getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (!(items[i].getData() instanceof Account))
                        continue;
                    Account arg1 = (Account)items[i].getData();
                    if (accountComparator.compare(account, arg1) < 0)
                    {
                        new AccountTreeItem(account, this, SWT.NONE, i);
                        updateSelection();
                        return;
                    }
                }
                
                new AccountTreeItem(account, this, SWT.NONE);
            }
            else if (o instanceof AccountGroup)
            {
                AccountGroup group = (AccountGroup)o;

                TreeItem items[] = getItems();
                for (int i = 0; i < items.length; i++)
                {
                    if (!(items[i].getData() instanceof AccountGroup))
                    {
                        new GroupTreeItem((AccountGroup)o, this, SWT.NONE, i);
                        return;
                    }
                    AccountGroup arg1 = (AccountGroup)items[i].getData();
                    if (groupComparator.compare(group, arg1) < 0)
                    {
                        new GroupTreeItem(group, this, SWT.NONE, i);
                        updateSelection();
                        return;
                    }
                }
                
                new GroupTreeItem(group, this, SWT.NONE);
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            TreeItem[] items = getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (o.equals(items[i].getData()))
                    items[i].dispose();
            }
        }
    }
}
