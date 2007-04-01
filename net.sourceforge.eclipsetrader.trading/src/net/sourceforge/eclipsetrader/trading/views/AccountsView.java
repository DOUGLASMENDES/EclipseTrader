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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.CurrencyConverter;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.actions.NewAccountAction;
import net.sourceforge.eclipsetrader.trading.actions.NewAccountGroupAction;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;
import net.sourceforge.eclipsetrader.trading.internal.DeleteAccountAction;
import net.sourceforge.eclipsetrader.trading.internal.TransactionAction;
import net.sourceforge.eclipsetrader.trading.views.transactions.TransactionsView;
import net.sourceforge.eclipsetrader.trading.wizards.accounts.AccountSettingsAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class AccountsView extends ViewPart implements Observer, ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.accounts";
    public static final String PREFS_ACCOUNT_COLUMNS_SIZE = "ACCOUNT_COLUMNS_SIZE";
    public static final String PREFS_ACCOUNT_EXPANDED_ITEMS = "ACCOUNT_EXPANDED_ITEMS";
    private Tree tree;
    private NumberFormat nf = NumberFormat.getInstance();
    private Action newTransactionAction = new TransactionAction(this);
    private Action deleteAction = new DeleteAccountAction(this);
    private Action propertiesAction = new AccountSettingsAction(this);
    private AccountTreeItemTransfer _accountTreeItemTransfer = new AccountTreeItemTransfer();
    private Map expandMap = new HashMap();
    private ControlListener columnControlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
            StringBuffer sizes = new StringBuffer();
            for (int i = 0; i < tree.getColumnCount(); i++)
                sizes.append(String.valueOf(tree.getColumn(i).getWidth()) + ";");
            TradingPlugin.getDefault().getPreferenceStore().setValue(PREFS_ACCOUNT_COLUMNS_SIZE, sizes.toString());
        }
    };
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
        tree.addTreeListener(new TreeListener() {
            public void treeCollapsed(TreeEvent e)
            {
                if (e.item instanceof GroupTreeItem)
                {
                    AccountGroup group = (AccountGroup)e.item.getData();
                    expandMap.remove("G" + String.valueOf(group.getId()));
                }
                else if (e.item instanceof AccountTreeItem)
                {
                    Account group = (Account)e.item.getData();
                    expandMap.remove("A" + String.valueOf(group.getId()));
                }
                saveExpandedStatus();
            }

            public void treeExpanded(TreeEvent e)
            {
                if (e.item instanceof GroupTreeItem)
                {
                    AccountGroup group = (AccountGroup)e.item.getData();
                    expandMap.put(String.valueOf(group.getId()), new Boolean(true));
                }
                saveExpandedStatus();
            }
            
            private void saveExpandedStatus()
            {
                StringBuffer sb = new StringBuffer();
                for (Iterator iter = expandMap.keySet().iterator(); iter.hasNext(); )
                {
                    if (sb.length() != 0)
                        sb.append(";");
                    sb.append((String)iter.next());
                }
                TradingPlugin.getDefault().getPreferenceStore().setValue(PREFS_ACCOUNT_EXPANDED_ITEMS, sb.toString());
                updateItemColors();
            }
        });
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Balance");
        column.addControlListener(columnControlListener);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (tree.getItem(new Point(e.x, e.y)) == null)
                {
                    tree.deselectAll();
                    updateSelection();
                }
            }

            public void mouseDoubleClick(MouseEvent e)
            {
                ISelection selection = getSite().getSelectionProvider().getSelection();
                if (selection instanceof AccountSelection)
                {
                    IWorkbenchPage page = getViewSite().getPage();
                    try {
                        page.showView(TransactionsView.VIEW_ID, String.valueOf(((AccountSelection)selection).getAccount().getId()), IWorkbenchPage.VIEW_ACTIVATE);
                    } catch (PartInitException e1) {
                        CorePlugin.logException(e1);
                    }
                }
            }
        });

        getSite().setSelectionProvider(new SelectionProvider());

        DragSource dragSource = new DragSource(tree, DND.DROP_COPY|DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] { _accountTreeItemTransfer });
        dragSource.addDragListener(new DragSourceListener() {
            private TreeItem[] selection;

            public void dragStart(DragSourceEvent event)
            {
                selection = tree.getSelection();
                if (selection.length == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event)
            {
                int count = 0;

                for (int i = 0; i < selection.length; i++)
                {
                    if (selection[i] instanceof AccountTreeItem)
                        count++;
                }
                
                AccountTreeItem[] items = new AccountTreeItem[count];
                count = 0;
                for (int i = 0; i < selection.length; i++)
                {
                    if (selection[i] instanceof AccountTreeItem)
                        items[count++] = (AccountTreeItem)selection[i]; 
                }
                event.data = items;
            }

            public void dragFinished(DragSourceEvent event)
            {
            }
        });

        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance(), _accountTreeItemTransfer });
        target.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event)
            {
                event.detail = DND.DROP_COPY;
                event.currentDataType = null;
                
                TransferData[] data = event.dataTypes;
                for (int i = 0; i < data.length; i++)
                {
                    if (SecurityTransfer.getInstance().isSupportedType(data[i]))
                    {
                        event.currentDataType = data[i];
                        break;
                    }
                }
                if (event.currentDataType == null)
                {
                    for (int i = 0; i < data.length; i++)
                    {
                        if (_accountTreeItemTransfer.isSupportedType(data[i]))
                        {
                            event.currentDataType = data[i];
                            break;
                        }
                    }
                }
            }

            public void dragOver(DropTargetEvent event)
            {
                TreeItem item = tree.getItem(tree.toControl(event.x, event.y));
                if (item != null && (item.getData() instanceof Account || _accountTreeItemTransfer.isSupportedType(event.currentDataType)))
                {
                    event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
                    TreeItem[] selection = { item };
                    tree.setSelection(selection);
                    updateSelection();
                }
                else
                {
                    event.feedback = DND.FEEDBACK_NONE;
                    tree.deselectAll();
                    updateSelection();
                }
            }

            public void dragOperationChanged(DropTargetEvent event)
            {
            }

            public void dragLeave(DropTargetEvent event)
            {
            }

            public void dropAccept(DropTargetEvent event)
            {
            }

            public void drop(DropTargetEvent event)
            {
                event.detail = DND.DROP_COPY;

                TreeItem item = tree.getItem(tree.toControl(event.x, event.y));
                if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType))
                {
                    if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType) && item != null && item.getData() instanceof Account)
                    {
                        Security[] securities = (Security[]) event.data;
                        for (int i = 0; i < securities.length; i++)
                        {
                            TransactionDialog dlg = new TransactionDialog((Account)item.getData(), getViewSite().getShell());
                            dlg.open(securities[i]);
                        }
                    }
                }
                if (_accountTreeItemTransfer.isSupportedType(event.currentDataType))
                {
                    Integer[] items = (Integer[]) event.data;
                    if (items != null)
                    {
                        AccountGroup group = null;
                        if (item != null)
                        {
                            if (item instanceof GroupTreeItem)
                                group = ((GroupTreeItem)item).getGroup();
                            else if (item instanceof AccountTreeItem)
                                group = ((AccountTreeItem)item).getAccount().getGroup();
                        }
                        for (int i = 0; i < items.length; i++)
                        {
                            Account account = (Account) CorePlugin.getRepository().load(Account.class, items[i]);
                            CorePlugin.getRepository().allAccounts().remove(account);
                            if (account.getGroup() != null)
                                account.getGroup().getAccounts().remove(account);
                            account.setGroup(group);
                            if (account.getGroup() != null)
                                account.getGroup().getAccounts().add(account);
                            CorePlugin.getRepository().allAccounts().add(account);
                            CorePlugin.getRepository().save(account);
                        }
                    }
                }
            }
        });

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new NewAccountAction(AccountsView.this));
                menuManager.add(new NewAccountGroupAction(AccountsView.this));
                menuManager.add(new Separator("group1")); //$NON-NLS-1$
                menuManager.add(newTransactionAction);
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
        
        IPreferenceStore preferenceStore = TradingPlugin.getDefault().getPreferenceStore();
        String[] values = preferenceStore.getString(PREFS_ACCOUNT_EXPANDED_ITEMS).split(";");
        for (int i = 0; i < values.length; i++)
            expandMap.put(values[i], new Boolean(true));

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                updateSelection();
                CorePlugin.getRepository().allAccountGroups().addCollectionObserver(AccountsView.this);
                CorePlugin.getRepository().allAccounts().addCollectionObserver(AccountsView.this);
                CurrencyConverter.getInstance().addObserver(AccountsView.this);
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
        CurrencyConverter.getInstance().deleteObserver(AccountsView.this);
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

        int index = 0;
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++)
            index = updateItemColors(items[i], index);
        
        String[] sizes = TradingPlugin.getDefault().getPreferenceStore().getString(PREFS_ACCOUNT_COLUMNS_SIZE).split(";");
        for (int i = 0; i < tree.getColumnCount(); i++)
        {
            if (i < sizes.length && sizes[i].length() != 0)
                tree.getColumn(i).setWidth(Integer.parseInt(sizes[i]));
            else
                tree.getColumn(i).setWidth(i == 0 ? 100 : 75);
        }
        if ("gtk".equals(SWT.getPlatform()))
            tree.getColumn(tree.getColumnCount() - 1).pack();
    }
    
    private void updateItemColors()
    {
        tree.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!tree.isDisposed())
                {
                    int index = 0;
                    TreeItem[] items = tree.getItems();
                    for (int i = 0; i < items.length; i++)
                        index = updateItemColors(items[i], index);
                }
            }
        });
    }
    
    private int updateItemColors(TreeItem treeItem, int index)
    {
//        treeItem.setBackground((index & 1) == 0 ? evenBackground : oddBackground);
//        treeItem.setForeground((index & 1) == 0 ? evenForeground : oddForeground);
        index++;

        if (treeItem instanceof GroupTreeItem)
        {
            AccountGroup group = (AccountGroup)treeItem.getData();
            if (expandMap.get(String.valueOf(group.getId())) != null)
                treeItem.setExpanded(true);
        }
        
        if (treeItem.getExpanded())
        {
            TreeItem[] items = treeItem.getItems();
            for (int i = 0; i < items.length; i++)
                index = updateItemColors(items[i], index);
        }
        
        return index;
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

        newTransactionAction.setEnabled(getSite().getSelectionProvider().getSelection() instanceof AccountSelection);
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
                    updateItemColors();
                    updateSelection();
                    return;
                }
            }
            
            new AccountTreeItem(account, tree, SWT.NONE);
            updateItemColors();
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
                    updateItemColors();
                    updateSelection();
                    return;
                }
                AccountGroup arg1 = (AccountGroup)items[i].getData();
                if (groupComparator.compare(group, arg1) < 0)
                {
                    new GroupTreeItem(group, tree, SWT.NONE, i);
                    updateItemColors();
                    updateSelection();
                    return;
                }
            }
            
            new GroupTreeItem(group, tree, SWT.NONE);
            updateItemColors();
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
        updateItemColors();
        updateSelection();
    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        tree.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!tree.isDisposed())
                {
                    TreeItem[] childs = tree.getItems();
                    for (int i = 0; i < childs.length; i++)
                        updateTree(childs[i]);
                }
            }
        });
    }
    
    void updateTree(TreeItem treeItem)
    {
        if (treeItem instanceof AccountTreeItem)
            ((AccountTreeItem)treeItem).update(null, null);
        else
        {
            TreeItem[] childs = treeItem.getItems();
            for (int i = 0; i < childs.length; i++)
                updateTree(childs[i]);
        }
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
            if (account.getCurrency() != null)
                setText(1, account.getCurrency().getSymbol() + " " + nf.format(account.getBalance()));
            else
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
        
        public Account getAccount()
        {
            return account;
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
                        if (account.getCurrency() != null)
                            setText(1, account.getCurrency().getSymbol() + " " + nf.format(account.getBalance()));
                        else
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
        
        public AccountGroup getGroup()
        {
            return group;
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
                        updateItemColors();
                        updateSelection();
                        return;
                    }
                }
                
                new AccountTreeItem(account, this, SWT.NONE);
                updateItemColors();
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
                        updateItemColors();
                        updateSelection();
                        return;
                    }
                }
                
                new GroupTreeItem(group, this, SWT.NONE);
                updateItemColors();
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
            updateItemColors();
        }
    }

    public class AccountTreeItemTransfer extends ByteArrayTransfer
    {
        private final String TYPENAME = AccountTreeItem.class.getName();
        private final int TYPEID = registerType(TYPENAME);

        private AccountTreeItemTransfer()
        {
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
         */
        protected String[] getTypeNames()
        {
            return new String[] { TYPENAME };
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
         */
        protected int[] getTypeIds()
        {
            return new int[] { TYPEID };
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
         */
        protected void javaToNative(Object object, TransferData transferData)
        {
            if (!checkMyType(object) || !isSupportedType(transferData))
                DND.error(DND.ERROR_INVALID_DATA);

            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream writeOut = new ObjectOutputStream(out);
                
                if (object instanceof AccountTreeItem)
                {
                    writeOut.writeInt(1);
                    writeOut.writeObject(((AccountTreeItem)object).getAccount().getId());
                }
                else if (object instanceof AccountTreeItem[])
                {
                    AccountTreeItem[] array = (AccountTreeItem[]) object;
                    writeOut.writeInt(array.length);
                    for (int i = 0; i < array.length; i++)
                        writeOut.writeObject(array[i].getAccount().getId());
                }
                
                byte[] buffer = out.toByteArray();
                writeOut.close();
                super.javaToNative(buffer, transferData);
            }
            catch (IOException e) {
                CorePlugin.logException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
         */
        protected Object nativeToJava(TransferData transferData)
        {
            if (isSupportedType(transferData))
            {
                byte[] buffer = (byte[]) super.nativeToJava(transferData);
                if (buffer == null)
                    return null;

                try
                {
                    ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                    ObjectInputStream readIn = new ObjectInputStream(in);

                    int length = readIn.readInt();
                    Integer[] security = new Integer[length];
                    for (int i = 0; i < length; i++)
                        security[i] = (Integer)readIn.readObject();
                    
                    readIn.close();
                    return security;
                }
                catch (Exception e) {
                    CorePlugin.logException(e);
                }
            }

            return null;
        }

        private boolean checkMyType(Object object)
        {
            return (object instanceof AccountTreeItem || object instanceof AccountTreeItem[]);
        }
    }
}
