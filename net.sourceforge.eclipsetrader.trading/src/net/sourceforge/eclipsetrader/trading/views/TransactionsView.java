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
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Transaction;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.AccountSelection;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.dialogs.TransactionDialog;
import net.sourceforge.eclipsetrader.trading.internal.DeleteTransactionAction;
import net.sourceforge.eclipsetrader.trading.internal.TransactionAction;
import net.sourceforge.eclipsetrader.trading.internal.TransactionSettingsAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class TransactionsView extends ViewPart implements ICollectionObserver
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.transactions";
    private Account account;
    private Table table;
    private Color evenForeground = new Color(null, 0, 0, 0);
    private Color evenBackground = new Color(null, 255, 255, 255);
    private Color oddForeground = new Color(null, 0, 0, 0);
    private Color oddBackground = new Color(null, 240, 240, 240);
    private Color negativeForeground = new Color(null, 240, 0, 0);
    private Color positiveForeground = new Color(null, 0, 192, 0);
    private NumberFormat nf = NumberFormat.getInstance();
    private NumberFormat pf = NumberFormat.getInstance();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Action deleteAction = new DeleteTransactionAction(this);
    private Action propertiesAction = new TransactionSettingsAction(this);
    private Comparator comparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            return ((Transaction)arg0).getDate().compareTo(((Transaction)arg1).getDate());
        }
    };
    
    public TransactionsView()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        pf.setGroupingUsed(true);
        pf.setMinimumIntegerDigits(1);
        pf.setMinimumFractionDigits(4);
        pf.setMaximumFractionDigits(4);
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
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateSelection();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (table.getItem(new Point(e.x, e.y)) == null)
                {
                    table.deselectAll();
                    updateSelection();
                }
            }
            public void mouseDoubleClick(MouseEvent e)
            {
                TableItem[] selection = table.getSelection();
                if (selection.length == 1)
                {
                    Transaction transaction = (Transaction)selection[0].getData(); 
                    TransactionDialog dlg = new TransactionDialog(account, getViewSite().getShell());
                    dlg.open(transaction);
                }
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);

        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Date / Time");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Code");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Description");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Operation");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Quantity");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Price");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Expenses");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Total");
        
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();

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
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
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

                if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType))
                {
                    Security[] securities = (Security[]) event.data;
                    for (int i = 0; i < securities.length; i++)
                    {
                        TransactionDialog dlg = new TransactionDialog(account, getViewSite().getShell());
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
                menuManager.add(new TransactionAction(TransactionsView.this));
                menuManager.add(new Separator());
                menuManager.add(deleteAction);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(propertiesAction);
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        table.setMenu(menuMgr.createContextMenu(table));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        account = (Account)CorePlugin.getRepository().load(Account.class, new Integer(Integer.parseInt(getViewSite().getSecondaryId())));
        if (account != null)
        {
            setTitleToolTip(account.getDescription());
            setContentDescription(account.getDescription());
            getSite().getSelectionProvider().setSelection(new AccountSelection(account));

            parent.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    updateView();
                }
            });
            
            account.getTransactions().addCollectionObserver(this);
        }
        else
            getSite().getSelectionProvider().setSelection(new NullSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        table.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (account != null)
            account.getTransactions().removeCollectionObserver(this);
        super.dispose();
    }

    private void updateView()
    {
        table.setRedraw(false);
        
        Object[] items = account.getTransactions().toArray();
        for (int i = 0; i < items.length; i++)
        {
            TableItem tableItem = new TransactionTableItem((Transaction)items[i], table, SWT.NONE);
            tableItem.setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
            tableItem.setForeground(((i & 1) == 1) ? oddForeground : evenForeground);
        }
        
        table.setRedraw(true);

        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
    }
    
    private void updateSelection()
    {
        TableItem[] selection = table.getSelection();
        deleteAction.setEnabled(selection.length != 0);
        propertiesAction.setEnabled(selection.length == 1);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(final Object o)
    {
        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!table.isDisposed())
                {
                    TableItem[] items = table.getItems();
                    for (int i = 0; i < items.length; i++)
                    {
                        if (comparator.compare(o, items[i].getData()) < 0)
                        {
                            new TransactionTableItem((Transaction)o, table, SWT.NONE, i);
                            for (i = 0; i < table.getItemCount(); i++)
                            {
                                table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
                                table.getItem(i).setForeground(((i & 1) == 1) ? oddForeground : evenForeground);
                            }
                            for (i = 1; i < table.getColumnCount(); i++)
                                table.getColumn(i).pack();
                            return;
                        }
                    }
                    new TransactionTableItem((Transaction)o, table, SWT.NONE);
                    for (int i = 0; i < table.getItemCount(); i++)
                    {
                        table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
                        table.getItem(i).setForeground(((i & 1) == 1) ? oddForeground : evenForeground);
                    }
                    for (int i = 1; i < table.getColumnCount(); i++)
                        table.getColumn(i).pack();
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(final Object o)
    {
        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!table.isDisposed())
                {
                    TableItem[] items = table.getItems();
                    for (int i = 0; i < items.length; i++)
                    {
                        if (o.equals(items[i].getData()))
                            items[i].dispose();
                    }
                    for (int i = 0; i < table.getItemCount(); i++)
                    {
                        table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
                        table.getItem(i).setForeground(((i & 1) == 1) ? oddForeground : evenForeground);
                    }
                }
            }
        });
    }

    public Account getAccount()
    {
        return account;
    }

    public Table getTable()
    {
        return table;
    }
    
    private class TransactionTableItem extends TableItem implements Observer
    {
        private Transaction transaction;

        public TransactionTableItem(Transaction transaction, Table parent, int style, int index)
        {
            super(parent, style, index);
            init(transaction);
        }

        public TransactionTableItem(Transaction transaction, Table parent, int style)
        {
            super(parent, style);
            init(transaction);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        private void init(Transaction transaction)
        {
            this.transaction = transaction;
            setData(transaction);
            update();

            transaction.addObserver(this);
            addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    TransactionTableItem.this.transaction.deleteObserver(TransactionTableItem.this);
                }
            });
        }
        
        private void update()
        {
            setText(1, df.format(transaction.getDate()));
            setText(2, transaction.getSecurity().getCode());
            setText(3, transaction.getSecurity().getDescription());
            setText(4, transaction.getQuantity() >= 0 ? "Buy" : "Sell");
            setText(5, String.valueOf(Math.abs(transaction.getQuantity())));
            setText(6, pf.format(transaction.getPrice()));
            setText(7, nf.format(transaction.getExpenses()));
            setText(8, nf.format(transaction.getAmount()));
            setForeground(8, transaction.getAmount() >= 0 ? positiveForeground : negativeForeground);
        }

        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    update();
                }
            });
        }
    }
}
