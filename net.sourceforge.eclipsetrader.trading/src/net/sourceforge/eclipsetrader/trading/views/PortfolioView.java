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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.AccountGroup;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Transaction;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.AccountGroupSelection;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.PortfolioPositionSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;
import net.sourceforge.eclipsetrader.trading.internal.ClosePositionAction;
import net.sourceforge.eclipsetrader.trading.internal.TransactionAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class PortfolioView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.portfolio";
    private Tree tree;
    private Color negativeForeground = new Color(null, 240, 0, 0);
    private Color positiveForeground = new Color(null, 0, 192, 0);
    private Action closePositionAction = new ClosePositionAction(this);
    private Action newTransactionAction = new TransactionAction(this);
    private NumberFormat nf = NumberFormat.getInstance();
    private NumberFormat pf = NumberFormat.getInstance();
    private NumberFormat pcf = NumberFormat.getInstance();
    private Map expandMap = new HashMap();
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
    private ControlListener columnControlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
            StringBuffer sizes = new StringBuffer();
            for (int i = 0; i < tree.getColumnCount(); i++)
                sizes.append(String.valueOf(tree.getColumn(i).getWidth()) + ";");
            TradingPlugin.getDefault().getPreferenceStore().setValue("PORTFOLIO_COLUMNS_SIZE", sizes.toString());
        }
    };

    public PortfolioView()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        pf.setGroupingUsed(true);
        pf.setMinimumIntegerDigits(1);
        pf.setMinimumFractionDigits(4);
        pf.setMaximumFractionDigits(4);

        pcf.setGroupingUsed(true);
        pcf.setMinimumIntegerDigits(1);
        pcf.setMinimumFractionDigits(2);
        pcf.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(closePositionAction);
        menuManager.add(newTransactionAction);
        menuManager.add(new Separator("additions")); //$NON-NLS-1$
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
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
        content.setLayout(gridLayout);
        
        tree = new Tree(content, SWT.FULL_SELECTION|SWT.MULTI);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
                    expandMap.put(String.valueOf("G" + group.getId()), new Boolean(true));
                }
                else if (e.item instanceof AccountTreeItem)
                {
                    Account group = (Account)e.item.getData();
                    expandMap.put(String.valueOf("A" + group.getId()), new Boolean(true));
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
                TradingPlugin.getDefault().getPreferenceStore().setValue("PORTFOLIO_VIEW_EXPANDED_ITEMS", sb.toString());
                updateItemColors();
            }
        });
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.LEFT);
        column.setText("Code");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Position");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Price");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Last");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Value");
        column.addControlListener(columnControlListener);
        column = new TreeColumn(tree, SWT.RIGHT);
        column.setText("Gain / Loss");
        column.addControlListener(columnControlListener);

        getSite().setSelectionProvider(new SelectionProvider());

        // Drag and drop support
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        target.addDropListener(new DropTargetListener() {
            public void dragEnter(DropTargetEvent event)
            {
                event.detail = DND.DROP_COPY;
            }

            public void dragOver(DropTargetEvent event)
            {
                TreeItem item = tree.getItem(tree.toControl(event.x, event.y));
                if (item != null && item.getData() instanceof Account)
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
        });

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(closePositionAction);
                menuManager.add(newTransactionAction);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        tree.setMenu(menuMgr.createContextMenu(tree));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
        
        IPreferenceStore preferenceStore = TradingPlugin.getDefault().getPreferenceStore();
        String[] values = preferenceStore.getString("PORTFOLIO_VIEW_EXPANDED_ITEMS").split(";");
        for (int i = 0; i < values.length; i++)
            expandMap.put(values[i], new Boolean(true));

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!tree.isDisposed())
                {
                    updateView();
                    updateSelection();
                    CorePlugin.getRepository().allAccountGroups().addCollectionObserver(PortfolioView.this);
                    CorePlugin.getRepository().allAccounts().addCollectionObserver(PortfolioView.this);
                }
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
        CorePlugin.getRepository().allAccountGroups().removeCollectionObserver(PortfolioView.this);
        CorePlugin.getRepository().allAccounts().removeCollectionObserver(PortfolioView.this);
        super.dispose();
    }

    private void updateView()
    {
        tree.setRedraw(true);
        tree.removeAll();

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
        
        tree.setRedraw(false);

        int index = 0;
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++)
            index = updateItemColors(items[i], index);
        
        String[] sizes = TradingPlugin.getDefault().getPreferenceStore().getString("PORTFOLIO_COLUMNS_SIZE").split(";");
        for (int i = 0; i < tree.getColumnCount(); i++)
        {
            if (i < sizes.length && sizes[i].length() != 0)
                tree.getColumn(i).setWidth(Integer.parseInt(sizes[i]));
            else
                tree.getColumn(i).pack();
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
            if (expandMap.get("G" + String.valueOf(group.getId())) != null)
                treeItem.setExpanded(true);
        }
        else if (treeItem instanceof AccountTreeItem)
        {
            Account group = (Account)treeItem.getData();
            if (expandMap.get("A" + String.valueOf(group.getId())) != null)
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
            {
                TreeItem parent = selection[0].getParentItem();
                getSite().getSelectionProvider().setSelection(new PortfolioPositionSelection((Account)parent.getData(), ((PositionTreeItem)selection[0]).getPosition()));
            }
        }
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());

        newTransactionAction.setEnabled(getSite().getSelectionProvider().getSelection() instanceof AccountSelection);
        closePositionAction.setEnabled(getSite().getSelectionProvider().getSelection() instanceof PortfolioPositionSelection);
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
    
    private class PositionTreeItem extends TreeItem implements Observer
    {
        private PortfolioPosition item;

        public PositionTreeItem(PortfolioPosition item, TreeItem parentItem, int style, int index)
        {
            super(parentItem, style, index);
            init(item);
        }

        public PositionTreeItem(PortfolioPosition item, TreeItem parentItem, int style)
        {
            super(parentItem, style);
            init(item);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TreeItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        void init(PortfolioPosition item)
        {
            this.item = item;

            update();
            setData(item.getSecurity());

            this.item.getSecurity().addObserver(this);
            this.item.getSecurity().getQuoteMonitor().addObserver(this);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    PositionTreeItem.this.item.getSecurity().deleteObserver(PositionTreeItem.this);
                    PositionTreeItem.this.item.getSecurity().getQuoteMonitor().deleteObserver(PositionTreeItem.this);
                }
            });
        }
        
        PortfolioPosition getPosition()
        {
            return item;
        }
        
        void update()
        {
            setText(item.getSecurity().getDescription());
            setText(1, item.getSecurity().getCode());
            setText(2, nf.format(item.getQuantity()));
            setText(3, pf.format(item.getPrice()));
            
            setForeground(6, null);

            Quote quote = item.getSecurity().getQuote();
            if (quote != null)
            {
                setText(4, pf.format(quote.getLast()));
                setText(5, pcf.format(item.getMarketValue()));
                double gain = (item.getQuantity() > 0) ? item.getMarketValue() - item.getValue() : item.getValue() - item.getMarketValue();
                String s1 = pcf.format(gain);
                String s2 = pcf.format(gain / item.getValue() * 100.0);
                if (gain > 0)
                {
                    setText(6, "+" + s1 + " (+" + s2 + "%)");
                    setForeground(6, positiveForeground);
                }
                else
                {
                    setText(6, s1 + " (" + s2 + "%)");
                    setForeground(6, negativeForeground);
                }
            }
            else
            {
                setText(4, "");
                setText(5, "");
                setText(6, "");
            }
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
                        update();
                }
            });
        }
    }
    
    private class AccountTreeItem extends TreeItem implements ICollectionObserver, Observer
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
            
            FontData fd = getFont().getFontData()[0];
            Font font = new Font(null, fd.getName(), fd.getHeight(), SWT.BOLD);
            setFont(font);
            
            setText(account.getDescription());
            setData(account);
            
            for (Iterator iter = account.getPortfolio().iterator(); iter.hasNext(); )
                new PositionTreeItem((PortfolioPosition)iter.next(), this, SWT.NONE);
            
            account.addObserver(this);
            account.getTransactions().addCollectionObserver(this);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    AccountTreeItem.this.account.deleteObserver(AccountTreeItem.this);
                    AccountTreeItem.this.account.getTransactions().removeCollectionObserver(AccountTreeItem.this);
                }
            });
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            if (o instanceof Transaction)
            {
                int index = 0;
                PositionTreeItem treeItem = null;
                for (Iterator iter = account.getPortfolio().iterator(); iter.hasNext(); index++)
                {
                    PortfolioPosition position = (PortfolioPosition)iter.next();
                    if (index < getItemCount())
                    {
                        treeItem = (PositionTreeItem)getItem(index);
                        treeItem.init(position);
                    }
                    else
                        treeItem = new PositionTreeItem(position, this, SWT.NONE);
                }
                while(getItemCount() > index)
                    getItem(index).dispose();
                updateItemColors();
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            if (o instanceof Transaction)
            {
                int index = 0;
                PositionTreeItem treeItem = null;
                for (Iterator iter = account.getPortfolio().iterator(); iter.hasNext(); index++)
                {
                    PortfolioPosition position = (PortfolioPosition)iter.next();
                    if (index < getItemCount())
                    {
                        treeItem = (PositionTreeItem)getItem(index);
                        treeItem.init(position);
                    }
                    else
                        treeItem = new PositionTreeItem(position, this, SWT.NONE);
                }
                while(getItemCount() > index)
                    getItem(index).dispose();
                updateItemColors();
            }
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
                        setText(account.getDescription());
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
}
