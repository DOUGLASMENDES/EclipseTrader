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

package net.sourceforge.eclipsetrader.trading.views.transactions;

import java.util.Iterator;

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
import net.sourceforge.eclipsetrader.trading.internal.TransactionAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Show the transactions registered for an account.
 * 
 * <p>The secondary-id holds the text representation of the account's database
 * id.</p>
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class TransactionsView extends ViewPart implements ICollectionObserver
{
    /**
     * View id.
     */
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.transactions"; //$NON-NLS-1$
    
    /**
     * Referenced account.
     */
    Account account;
    
    /**
     * Table widget.
     */
    Table table;
    
    /**
     * JFace viewer
     */
    TableViewer viewer;
    
    /**
     * Label provider.
     */
    TransactionLabelProvider labelProvider = new TransactionLabelProvider();
    
    /**
     * Even rows foreground color.
     */
    Color evenForeground = new Color(null, 0, 0, 0);
    
    /**
     * Even rows background color.
     */
    Color evenBackground = new Color(null, 255, 255, 255);
    
    /**
     * Odd rows foreground color.
     */
    Color oddForeground = new Color(null, 0, 0, 0);
    
    /**
     * Odd rows background color.
     */
    Color oddBackground = new Color(null, 240, 240, 240);
    
    /**
     * Delete transaction action.
     */
    Action deleteAction;
    
    /**
     * Edit transaction action.
     */
    Action propertiesAction;
    
    /**
     * Listener for drop operations.
     */
    DropTargetListener dropTargetListener = new DropTargetListener() {
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
    };

    public TransactionsView()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

        deleteAction = new Action() {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                if (!selection.isEmpty())
                {
                    if (MessageDialog.openConfirm(getViewSite().getShell(), getPartName(), Messages.TransactionsView_DeleteConfirmMessage))
                    {
                        for (Iterator itr = selection.iterator(); itr.hasNext(); )
                            account.getTransactions().remove(itr.next());
                        CorePlugin.getRepository().save(account);
                    }
                }
            }
        };
        deleteAction.setText(Messages.TransactionsView_Delete);
        deleteAction.setToolTipText(Messages.TransactionsView_DeleteTooltip);
        deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        deleteAction.setEnabled(false);

        propertiesAction = new Action() {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
                if (selection.size() == 1)
                {
                    Transaction transaction = (Transaction)selection.getFirstElement(); 
                    TransactionDialog dlg = new TransactionDialog(account, getViewSite().getShell());
                    dlg.open(transaction);
                }
            }
        };
        propertiesAction.setText(Messages.TransactionsView_Edit);
        propertiesAction.setEnabled(false);

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(deleteAction);
        
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        fillMenu(menuManager);
        
        site.getActionBars().updateActionBars();
        site.setSelectionProvider(new SelectionProvider());
        
        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        table = new Table(parent, SWT.FULL_SELECTION|SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (table.getItem(new Point(e.x, e.y)) == null)
                {
                    viewer.setSelection(new StructuredSelection());
                    updateSelection();
                }
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);
        column = new TableColumn(table, SWT.RIGHT);
        column.setText(Messages.TransactionsView_DateTime);
        column.setWidth(125);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TransactionsView_Code);
        column.setWidth(70);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TransactionsView_Description);
        column.setWidth(170);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TransactionsView_Operation);
        column.setWidth(70);
        column = new TableColumn(table, SWT.RIGHT);
        column.setText(Messages.TransactionsView_Quantity);
        column.setWidth(70);
        column = new TableColumn(table, SWT.RIGHT);
        column.setText(Messages.TransactionsView_Price);
        column.setWidth(70);
        column = new TableColumn(table, SWT.RIGHT);
        column.setText(Messages.TransactionsView_Expenses);
        column.setWidth(70);
        column = new TableColumn(table, SWT.RIGHT);
        column.setText(Messages.TransactionsView_Total);
        column.setWidth(80);
        
        viewer = new TableViewer(table);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(labelProvider);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                updateSelection();
            }
        });
        viewer.setSorter(new ViewerSorter() {
            public int compare(Viewer viewer, Object e1, Object e2)
            {
                return ((Transaction)e1).getDate().compareTo(((Transaction)e2).getDate());
            }
        });
        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event)
            {
                propertiesAction.run();
            }
        });

        // Drag and drop support
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        target.addDropListener(dropTargetListener);

        // Context menu
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                fillMenu(menuManager);
            }
        });
        table.setMenu(menuMgr.createContextMenu(table));
        getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

        account = (Account)CorePlugin.getRepository().load(Account.class, new Integer(Integer.parseInt(getViewSite().getSecondaryId())));
        if (account != null)
        {
            setContentDescription(getPartName());
            setTitleToolTip(getPartName());
            setPartName(account.getDescription());
            
            getSite().getSelectionProvider().setSelection(new AccountSelection(account));

            parent.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    viewer.setInput(account.getTransactions().toArray());
                    updateColors();
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
        table.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (account != null)
            account.getTransactions().removeCollectionObserver(this);

        evenForeground.dispose();
        evenBackground.dispose();
        oddForeground.dispose();
        oddBackground.dispose();
        
        labelProvider.dispose();
        
        super.dispose();
    }

    /**
     * Updates the action enablements based on the currenct selection.
     */
    protected void updateSelection()
    {
        int count = ((IStructuredSelection)viewer.getSelection()).size();
        deleteAction.setEnabled(count != 0);
        propertiesAction.setEnabled(count == 1);
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
                    viewer.add(o);
                    updateColors();
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
                    viewer.remove(o);
                    updateColors();
                }
            }
        });
    }

    /**
     * Fills the view's and popup menus with actions.
     * 
     * @param menuManager the menu to fill.
     */
    protected void fillMenu(IMenuManager menuManager)
    {
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new TransactionAction(TransactionsView.this));
        menuManager.add(new Separator());
        menuManager.add(deleteAction);
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(propertiesAction);
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
    }

    /**
     * Updates the table rows background colors. 
     */
    protected void updateColors()
    {
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
            items[i].setBackground((i & 1) == 0 ? evenBackground : oddBackground);
    }
}
