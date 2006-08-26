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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.dialogs.OrdersViewColumnsDialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class OrdersView extends ViewPart implements IPropertyChangeListener
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.trading.orders";
    public static final String PREFS_ORDERS_COLUMNS = "ORDERSVIEW_COLUMNS";
    public static final String PREFS_COLUMNS_SIZE = "ORDERSVIEW_COLUMNS_SIZE";
    static final int PROVIDER = 0;
    static final int ORDER_ID = 1;
    static final int DATE_TIME = 2;
    static final int SYMBOL = 3;
    static final int SECURITY = 4;
    static final int SIDE = 5;
    static final int TYPE = 6;
    static final int QUANTITY = 7;
    static final int PRICE = 8;
    static final int STOP_PRICE = 9;
    static final int FILLED_QUANTITY = 10;
    static final int AVERAGE_PRICE = 11;
    static final int STATUS = 12;
    List columns = new ArrayList();
    TabFolder tabFolder;
    OrdersTable all;
    OrdersTable pending;
    OrdersTable filled;
    OrdersTable canceled;
    OrdersTable rejected;
    private Color canceledColor = new Color(null, 128, 128, 128);
    private Color rejectedColor = new Color(null, 208, 0, 0);
    private Color filledColor = new Color(null, 0, 208, 0);
    private Color partialColor = new Color(null, 128, 0, 0);
    private boolean ignoreResize = true;
    Action cancelRequest;
    Action editColumnsAction;
    private Logger logger = Logger.getLogger(getClass());
    private ControlListener columnControlListener = new ControlAdapter() {
        public void controlResized(ControlEvent e)
        {
            if (!ignoreResize)
            {
                Table table = all.table;
                
                TableColumn tableColumn = (TableColumn)e.getSource();
                int index = tableColumn.getParent().indexOf(tableColumn);
                if (index != STATUS)
                {
                    ignoreResize = true;
                    if (tableColumn.getParent() != all.table)
                        all.table.getColumn(index).setWidth(tableColumn.getWidth());
                    if (tableColumn.getParent() != pending.table)
                        pending.table.getColumn(index).setWidth(tableColumn.getWidth());
                    if (tableColumn.getParent() != filled.table)
                        filled.table.getColumn(index).setWidth(tableColumn.getWidth());
                    if (tableColumn.getParent() != canceled.table)
                        canceled.table.getColumn(index).setWidth(tableColumn.getWidth());
                    if (tableColumn.getParent() != rejected.table)
                        rejected.table.getColumn(index).setWidth(tableColumn.getWidth());
                    ignoreResize = false;
                }
                
                StringBuffer sizes = new StringBuffer();
                for (int i = 0; i < table.getColumnCount(); i++)
                    sizes.append(String.valueOf(table.getColumn(i).getWidth()) + ";");
                TradingPlugin.getDefault().getPreferenceStore().setValue(PREFS_COLUMNS_SIZE, sizes.toString());
            }
        }
    };

    public OrdersView()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        cancelRequest = new Action() {
            public void run()
            {
                Order[] selection = getSelectedOrders();
                for (int i = 0; i < selection.length; i++)
                {
                    if (selection[i].getStatus() != Order.STATUS_CANCELED)
                        selection[i].cancelRequest();
                }
            }
        };
        cancelRequest.setText("Cancel");
        cancelRequest.setToolTipText("Cancel");
        cancelRequest.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/elcl16/delete_edit.gif"));
        cancelRequest.setDisabledImageDescriptor(TradingPlugin.getImageDescriptor("icons/dlcl16/delete_edit.gif"));
        cancelRequest.setEnabled(false);

        editColumnsAction = new Action() {
            public void run()
            {
                OrdersViewColumnsDialog dlg = new OrdersViewColumnsDialog(getViewSite().getShell());
                dlg.open();
            }
        };
        editColumnsAction.setText("Edit Columns...");
        editColumnsAction.setEnabled(true);
        
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
        
        menuManager.appendToGroup("top", editColumnsAction);
        toolBarManager.appendToGroup("end", cancelRequest);
        
        String value = TradingPlugin.getDefault().getPreferenceStore().getString(PREFS_ORDERS_COLUMNS);
        String[] id = value.split(";");
        for (int i = 0; i < id.length; i++)
        {
            IOrdersLabelProvider provider = TradingPlugin.createOrdersLabelProvider(id[i]);
            if (provider != null)
            {
                columns.add(provider);
                logger.debug("Adding column [" + id[i] + "]");
            }
            else
                logger.warn("Cannot add column [" + id[i] + "]");
        }
        
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
        
        tabFolder = new TabFolder(content, SWT.BOTTOM);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("All");
        all = new OrdersTable(tabFolder, new ArrayList());
        tabItem.setControl(all.getControl());

        tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("Pending");
        Integer[] pendingFilter = { 
                new Integer(Order.STATUS_NEW), 
                new Integer(Order.STATUS_PARTIAL), 
                new Integer(Order.STATUS_PENDING_CANCEL), 
                new Integer(Order.STATUS_PENDING_NEW) 
            };
        pending = new OrdersTable(tabFolder, Arrays.asList(pendingFilter));
        tabItem.setControl(pending.getControl());
        
        tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("Filled");
        Integer[] filledFilter = { 
                new Integer(Order.STATUS_FILLED) 
            };
        filled = new OrdersTable(tabFolder, Arrays.asList(filledFilter));
        tabItem.setControl(filled.getControl());
        
        tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("Canceled");
        Integer[] canceledFilter = { 
                new Integer(Order.STATUS_CANCELED) 
            };
        canceled = new OrdersTable(tabFolder, Arrays.asList(canceledFilter));
        tabItem.setControl(canceled.getControl());
        
        tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("Rejected");
        Integer[] rejectedFilter = { 
                new Integer(Order.STATUS_REJECTED) 
            };
        rejected = new OrdersTable(tabFolder, Arrays.asList(rejectedFilter));
        tabItem.setControl(rejected.getControl());

        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateActionsEnablement();
            }
        });

        ignoreResize = false;
        TradingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        TradingPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }

    void updateActionsEnablement()
    {
        boolean cancelEnable = false;
        
        Order[] selection = getSelectedOrders();
        for (int i = 0; i < selection.length; i++)
        {
            if (selection[i].getStatus() != Order.STATUS_CANCELED && selection[i].getStatus() != Order.STATUS_FILLED)
                cancelEnable = true;
        }
        cancelRequest.setEnabled(cancelEnable);
    }
    
    Order[] getSelectedOrders()
    {
        List orders = new ArrayList();
        
        TabItem[] item = tabFolder.getSelection();
        if (item.length != 0)
        {
            Table table = (Table)item[0].getControl();
            int[] selection = table.getSelectionIndices();
            OrdersTable ordersTable = (OrdersTable)table.getData();
            for (int i = 0; i < selection.length; i++)
                orders.add(ordersTable.list.get(selection[i]));
        }
        
        return (Order[])orders.toArray(new Order[orders.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals("ORDERSVIEW_COLUMNS"))
        {
            columns.clear();
            String value = TradingPlugin.getDefault().getPreferenceStore().getString(PREFS_ORDERS_COLUMNS);
            String[] id = value.split(";");
            for (int i = 0; i < id.length; i++)
            {
                IOrdersLabelProvider provider = TradingPlugin.createOrdersLabelProvider(id[i]);
                if (provider != null)
                    columns.add(provider);
            }

            all.updateTable();
            pending.updateTable();
            filled.updateTable();
            canceled.updateTable();
            rejected.updateTable();
        }
    }

    class OrdersTable implements ICollectionObserver, Observer
    {
        Table table;
        List filter = new ArrayList();
        List list = new ArrayList();
        Comparator comparator = new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Order)arg1).getDate().compareTo(((Order)arg0).getDate());
            }
        };

        public OrdersTable(Composite parent, List filter)
        {
            this.filter = filter;

            table = new Table(parent, SWT.MULTI|SWT.FULL_SELECTION);
            table.setHeaderVisible(true);
            table.setLinesVisible(false);
            table.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    CorePlugin.getRepository().allOrders().removeCollectionObserver(OrdersTable.this);
                    for (Iterator iter = list.iterator(); iter.hasNext(); )
                    {
                        Order order = (Order)iter.next();
                        order.deleteObserver(OrdersTable.this);
                    }
                }
            });
            table.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    updateActionsEnablement();
                }
            });
            table.setData(this);

            MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
            menuMgr.setRemoveAllWhenShown(true);
            menuMgr.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager menuManager)
                {
                    menuManager.add(new Separator("top")); //$NON-NLS-1$
                    menuManager.add(cancelRequest);
                    menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                    menuManager.add(new Separator("bottom")); //$NON-NLS-1$
                }
            });
            table.setMenu(menuMgr.createContextMenu(table));
            getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

            list = new ArrayList();
            for (Iterator iter = CorePlugin.getRepository().allOrders().iterator(); iter.hasNext(); )
            {
                Order order = (Order)iter.next();
                if (filter.size() == 0 || filter.contains(new Integer(order.getStatus())))
                    list.add(order);
            }
            Collections.sort(list, comparator);

            updateTable();
            CorePlugin.getRepository().allOrders().addCollectionObserver(this);
        }
        
        public Control getControl()
        {
            return table;
        }
        
        public void updateTable()
        {
            table.removeAll();
            for (int i = table.getColumnCount() - 1; i >= 0; i--)
                table.getColumn(i).dispose();
            
            for (Iterator iter = columns.iterator(); iter.hasNext(); )
            {
                IOrdersLabelProvider label = (IOrdersLabelProvider)iter.next(); 
                TableColumn tableColumn = new TableColumn(table, label.getStyle());
                tableColumn.setText(label.getHeaderText());
                tableColumn.addControlListener(columnControlListener);
            }
            
            String[] sizes = TradingPlugin.getDefault().getPreferenceStore().getString(PREFS_COLUMNS_SIZE).split(";");
            for (int i = 0; i < table.getColumnCount(); i++)
            {
                if (i < sizes.length && sizes[i].length() != 0)
                    table.getColumn(i).setWidth(Integer.parseInt(sizes[i]));
                else
                    table.getColumn(i).setWidth(75);
            }

            for (Iterator iter = list.iterator(); iter.hasNext(); )
            {
                Order order = (Order)iter.next();
                TableItem tableItem = new TableItem(table, SWT.NONE);
                update(tableItem, order);
                order.addObserver(OrdersTable.this);
            }
            if ("gtk".equals(SWT.getPlatform()))
                table.getColumn(table.getColumnCount() - 1).pack();
        }
        
        public int indexOf(Order order)
        {
            return list.indexOf(order);
        }
        
        void update(TableItem tableItem, Order order)
        {
            int index = 0;
            for (Iterator iter = columns.iterator(); iter.hasNext(); index++)
            {
                IOrdersLabelProvider label = (IOrdersLabelProvider)iter.next();
                tableItem.setText(index, label.getText(order));
            }

            if (filter.size() == 0)
            {
                switch(order.getStatus())
                {
                    case Order.STATUS_PARTIAL:
                        tableItem.setForeground(partialColor);
                        break;
                    case Order.STATUS_FILLED:
                        tableItem.setForeground(filledColor);
                        break;
                    case Order.STATUS_CANCELED:
                        tableItem.setForeground(canceledColor);
                        break;
                    case Order.STATUS_REJECTED:
                        tableItem.setForeground(rejectedColor);
                        break;
                    default:
                        tableItem.setForeground(null);
                        break;
                }
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            final Order order = (Order)o;
            table.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    int index = indexOf(order);
                    
                    if (index != -1 && !table.isDisposed())
                    {
                        TableItem tableItem = table.getItem(index);
                        if (filter.size() != 0 && !filter.contains(new Integer(order.getStatus())))
                        {
                            tableItem.dispose();
                            list.remove(order);
                        }
                        else
                            update(tableItem, order);
                    }
                    
                    if (filter.size() == 0)
                    {
                        switch(order.getStatus())
                        {
                            case Order.STATUS_PENDING_NEW:
                                if (pending.list.indexOf(order) == -1)
                                    pending.itemAdded(order);
                                break;
                            case Order.STATUS_FILLED:
                                if (filled.list.indexOf(order) == -1)
                                    filled.itemAdded(order);
                                break;
                            case Order.STATUS_CANCELED:
                                if (canceled.list.indexOf(order) == -1)
                                    canceled.itemAdded(order);
                                break;
                            case Order.STATUS_REJECTED:
                                if (rejected.list.indexOf(order) == -1)
                                    rejected.itemAdded(order);
                                break;
                        }
                    }

                    if ("gtk".equals(SWT.getPlatform()))
                        table.getColumn(table.getColumnCount() - 1).pack();
                }
            });
        }
        
        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            final Order order = (Order)o;

            if (filter.size() == 0 || filter.contains(new Integer(order.getStatus())))
            {
                list.add(order);
                Collections.sort(list, comparator);
                
                table.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!table.isDisposed())
                        {
                            TableItem tableItem = new TableItem(table, SWT.NONE, list.indexOf(order));
                            update(tableItem, order);
                            order.addObserver(OrdersTable.this);

                            if ("gtk".equals(SWT.getPlatform()))
                                table.getColumn(table.getColumnCount() - 1).pack();
                        }
                    }
                });
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            final Order order = (Order)o;
            order.deleteObserver(OrdersTable.this);
            if (list.indexOf(o) != -1)
            {
                table.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        int index = list.indexOf(order);
                        if (!table.isDisposed() && index != -1)
                        {
                            table.getItem(index).dispose();
                            list.remove(index);
                            if ("gtk".equals(SWT.getPlatform()))
                                table.getColumn(table.getColumnCount() - 1).pack();
                        }
                    }
                });
            }
        }
    }
}
