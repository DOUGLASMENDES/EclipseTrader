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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.FeedMonitor;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.columns.Column;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.transfers.WatchlistItemTransfer;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTable;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTableColumn;
import net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.WatchlistItemSelection;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;
import net.sourceforge.eclipsetrader.trading.wizards.WatchlistItemPropertiesDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 */
public class WatchlistTableViewer extends AbstractLayout
{
    public static final String EVEN_ROWS_BACKGROUND = "TABLE_EVEN_ROWS_BACKGROUND";
    public static final String EVEN_ROWS_FOREGROUND = "TABLE_EVEN_ROWS_FOREGROUND";
    public static final String ODD_ROWS_BACKGROUND = "TABLE_ODD_ROWS_BACKGROUND";
    public static final String ODD_ROWS_FOREGROUND = "TABLE_ODD_ROWS_FOREGROUND";
    public static final String TOTALS_ROWS_BACKGROUND = "TABLE_TOTALS_ROWS_BACKGROUND";
    public static final String TOTALS_ROWS_FOREGROUND = "TABLE_TOTALS_ROWS_FOREGROUND";
    public static final String TICK_BACKGROUND = "TABLE_TICK_BACKGROUND";
    public static final String POSITIVE_FOREGROUND = "TABLE_POSITIVE_FOREGROUND";
    public static final String NEGATIVE_FOREGROUND = "TABLE_NEGATIVE_FOREGROUND";
    public static final String ALERT_BACKGROUND = "TABLE_ALERT_BACKGROUND";
    private Composite content;
    private EditableTable table;
    private Color evenForeground;
    private Color evenBackground;
    private Color oddForeground;
    private Color oddBackground;
    private Color totalsForeground;
    private Color totalsBackground;
    private Color tickBackground;
    private Color negativeForeground;
    private Color positiveForeground;
    private Color alertHilightBackground;
    private boolean showTotals = false;
    private int sortColumn = -1;
    private int sortDirection = 0;
    private Action propertiesAction;
    private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(IThemeManager.CHANGE_CURRENT_THEME))
            {
                ((ITheme) event.getOldValue()).removePropertyChangeListener(this);
                setTheme((ITheme) event.getNewValue());
            }
            else
            {
                IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
                ITheme theme = themeManager.getCurrentTheme();

                if (event.getProperty().equals(EVEN_ROWS_BACKGROUND))
                    evenBackground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(EVEN_ROWS_FOREGROUND))
                    evenForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(ODD_ROWS_BACKGROUND))
                    oddBackground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(ODD_ROWS_FOREGROUND))
                    oddForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(TOTALS_ROWS_FOREGROUND))
                    totalsForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(TOTALS_ROWS_FOREGROUND))
                    totalsForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(POSITIVE_FOREGROUND))
                    positiveForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(NEGATIVE_FOREGROUND))
                    negativeForeground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(TICK_BACKGROUND))
                    tickBackground = theme.getColorRegistry().get(event.getProperty());
                else if (event.getProperty().equals(ALERT_BACKGROUND))
                    alertHilightBackground = theme.getColorRegistry().get(event.getProperty());
            }
            updateView();
        }
    };
    private Comparator comparator = new Comparator() {
        public int compare(Object arg0, Object arg1)
        {
            Comparator c = ((Column) table.getColumn(sortColumn).getData());
            if (sortDirection == 0)
                return c.compare(arg0, arg1);
            else
                return c.compare(arg1, arg0);
        }
    };
    private SelectionListener columnSelectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
            int index = table.indexOf((TableColumn) e.widget);
            if (index == sortColumn)
                sortDirection = sortDirection == 0 ? 1 : 0;
            else
            {
                sortColumn = index;
                sortDirection = 0;
            }

            Collections.sort(getView().getWatchlist().getItems(), comparator);
            updateView();
            
            String s = String.valueOf(sortColumn) + ";" + String.valueOf(sortDirection);
            TradingPlugin.getDefault().getPreferenceStore().setValue(WatchlistView.PREFS_SORTING + getViewSite().getSecondaryId(), s);
        }
    };
    
    public WatchlistTableViewer(WatchlistView view)
    {
        super(view);
        
        propertiesAction = new Action() {
            public void run()
            {
                TableItem[] selection = table.getSelection();
                if (selection.length != 0 && selection[0] instanceof WatchlistTableItem)
                {
                    WatchlistItem item = ((WatchlistTableItem) selection[0]).getWatchlistItem();
                    WatchlistItemPropertiesDialog dlg = new WatchlistItemPropertiesDialog(item, getViewSite().getShell());
                    dlg.open();
                }
                
            }
        };
        propertiesAction.setText("Properties");
        propertiesAction.setEnabled(false);

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("properties", propertiesAction);
        actionBars.updateActionBars();
    }
    
    public Composite createPartControl(Composite parent)
    {
        content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        themeManager.addPropertyChangeListener(themeChangeListener);
        ITheme theme = themeManager.getCurrentTheme();
        setTheme(theme);

        table = new EditableTable(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setBackground(parent.getBackground());
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
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);
        
        // Drag and drop support
        DragSource dragSource = new DragSource(table, DND.DROP_COPY|DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[] { SecurityTransfer.getInstance(), WatchlistItemTransfer.getInstance(), TextTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {
            public void dragStart(DragSourceEvent event)
            {
                if (table.getSelectionCount() == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event)
            {
                if (SecurityTransfer.getInstance().isSupportedType(event.dataType))
                {
                    TableItem selection[] = table.getSelection();
                    Security[] securities = new Security[selection.length];
                    for (int i = 0; i < selection.length; i++)
                    {
                        WatchlistTableItem item = (WatchlistTableItem)selection[i];
                        securities[i] = item.getWatchlistItem().getSecurity();
                    }
                    event.data = securities;
                }
                else if (WatchlistItemTransfer.getInstance().isSupportedType(event.dataType))
                {
                    TableItem selection[] = table.getSelection();
                    WatchlistItem[] items = new WatchlistItem[selection.length];
                    for (int i = 0; i < selection.length; i++)
                    {
                        WatchlistTableItem item = (WatchlistTableItem)selection[i];
                        items[i] = item.getWatchlistItem();
                    }
                    event.data = items;
                }
                else if (TextTransfer.getInstance().isSupportedType(event.dataType))
                {
                    TableItem selection[] = table.getSelection();
                    
                    StringBuffer data = new StringBuffer();
                    for (int i = 0; i < selection.length; i++)
                    {
                        for (int c = 1; c < table.getColumnCount(); c++)
                        {
                            if (c > 1)
                                data.append(";");
                            data.append(selection[i].getText(c));
                        }
                        data.append("\r\n");
                    }

                    event.data = data.toString();
                }
            }

            public void dragFinished(DragSourceEvent event)
            {
                if (event.doit && event.detail == DND.DROP_MOVE)
                {
                    TableItem selection[] = table.getSelection();
                    for (int i = 0; i < selection.length; i++)
                    {
                        WatchlistTableItem item = (WatchlistTableItem)selection[i];
                        getView().getWatchlist().getItems().remove(item.getWatchlistItem());
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
                menuManager.add(new Separator("search")); //$NON-NLS-1$
                getView().fillMenuBars(menuManager);
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
                menuManager.add(propertiesAction);
            }
        });
        table.setMenu(menuMgr.createContextMenu(table));
        getView().getSite().registerContextMenu(menuMgr, getView().getSite().getSelectionProvider());
        
        showTotals = TradingPlugin.getDefault().getPreferenceStore().getBoolean(WatchlistView.PREFS_SHOW_TOTALS + getViewSite().getSecondaryId());
        String[] items = TradingPlugin.getDefault().getPreferenceStore().getString(WatchlistView.PREFS_SORTING + getViewSite().getSecondaryId()).split(";");
        if (items.length == 2)
        {
            sortColumn = new Integer(items[0]).intValue();
            sortDirection = new Integer(items[1]).intValue();
        }

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    void setFocus()
    {
        table.getParent().setFocus();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        themeManager.removePropertyChangeListener(themeChangeListener);
        ITheme theme = themeManager.getCurrentTheme();
        theme.removePropertyChangeListener(themeChangeListener);

        if (content != null)
            content.dispose();
        
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("properties", null);
    }

    public void updateView()
    {
        int index;
        TableColumn tableColumn;
        WatchlistTableItem tableItem;

        index = 1;
        for (Iterator iter = getView().getWatchlist().getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column)iter.next();
            int style = SWT.LEFT;
            if (column.getStyle() == Column.CENTER)
                style = SWT.CENTER;
            else if (column.getStyle() == Column.RIGHT)
                style = SWT.RIGHT;
            
            if (index < table.getColumnCount())
            {
                tableColumn = table.getColumn(index);
                tableColumn.setAlignment(style);
            }
            else
            {
                tableColumn = new EditableTableColumn(table, style) {
                    public boolean isEditable()
                    {
                        Column c = (Column)getData();
                        return (c == null || !c.isEditable()) ? false : true; 
                    }
                };
                tableColumn.addControlListener(new ControlAdapter() {
                    public void controlResized(ControlEvent e)
                    {
                        Column c = (Column)e.widget.getData();
                        if (c != null)
                            c.setWidth(((TableColumn)e.widget).getWidth());
                    }
                });
                tableColumn.addSelectionListener(columnSelectionListener);
            }
            tableColumn.setText(column.getLabel());
            tableColumn.setData(column);
            index++;
        }
        
        while(index < table.getColumnCount())
            table.getColumn(index).dispose();

        index = 0;
        for (Iterator iter = getView().getWatchlist().getItems().iterator(); iter.hasNext(); )
        {
            WatchlistItem watchlistItem = (WatchlistItem)iter.next();
            if (index < table.getItemCount())
            {
                tableItem = (WatchlistTableItem)table.getItem(index);
                tableItem.setWatchlistItem(watchlistItem);
            }
            else
                tableItem = new WatchlistTableItem(table, SWT.NONE, watchlistItem);
            tableItem.setBackground(((index & 1) == 1) ? oddBackground : evenBackground);
            tableItem.setForeground(((index & 1) == 1) ? oddForeground : evenForeground);

            Security security = watchlistItem.getSecurity();
            if (security != null && security.getQuoteFeed() != null)
                FeedMonitor.monitor(security);

            index++;
        }
        
        if (showTotals)
        {
            WatchlistTotalsTableItem item = null;
            if (index < table.getItemCount())
            {
                if (table.getItem(index) instanceof WatchlistTotalsTableItem)
                {
                    item = (WatchlistTotalsTableItem) table.getItem(index);
                    item.setWatchlistItem(getView().getWatchlist().getTotals());
                }
            }
            if (item == null)
                item = new WatchlistTotalsTableItem(table, SWT.NONE, getView().getWatchlist().getTotals());
            item.setBackground(totalsBackground);
            item.setForeground(totalsForeground);
            index++;
        }

        table.setItemCount(index);

        for (int i = 1; i < table.getColumnCount(); i++)
        {
            Column c = (Column)table.getColumn(i).getData();
            if (c.getWidth() == 0)
                table.getColumn(i).pack();
            else
                table.getColumn(i).setWidth(c.getWidth());
        }
        if ("gtk".equals(SWT.getPlatform()))
            table.getColumn(table.getColumnCount() - 1).pack();
    }

    public void update(Observable o, Object arg)
    {
        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!table.isDisposed())
                    updateView();
            }
        });
    }
    
    private void updateSelection()
    {
        boolean enable = false;
        if (table.getSelectionCount() != 0 && table.getSelection()[0] instanceof WatchlistTableItem)
        {
            WatchlistTableItem tableItem = (WatchlistTableItem)table.getSelection()[0];
            getView().getSite().getSelectionProvider().setSelection(new WatchlistItemSelection(tableItem.getWatchlistItem()));
            enable = true;
        }
        else
            getView().getSite().getSelectionProvider().setSelection(new NullSelection());

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.getGlobalActionHandler("cut").setEnabled(enable);
        actionBars.getGlobalActionHandler("copy").setEnabled(enable);
        actionBars.getGlobalActionHandler("delete").setEnabled(enable);
        actionBars.getGlobalActionHandler("properties").setEnabled(enable);
    }

    public void itemAdded(Object o)
    {
        if (o instanceof WatchlistItem)
        {
            if (sortColumn >= 0)
                Collections.sort(getView().getWatchlist().getItems(), comparator);
            updateView();
        }
/*        {
            WatchlistItem watchlistItem = (WatchlistItem)o;
            int index = table.getItemCount();
            if (showTotals)
                index--;
            WatchlistTableItem tableItem = new WatchlistTableItem(table, SWT.NONE, index, watchlistItem);
            tableItem.setBackground(((index & 1) == 1) ? oddBackground : evenBackground);
        }*/
    }

    public void itemRemoved(Object o)
    {
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            WatchlistTableItem tableItem = (WatchlistTableItem)items[i];
            if (tableItem.getWatchlistItem().equals(o))
            {
                tableItem.dispose();
                for (; i < table.getItemCount() - 1; i++)
                    table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
                break;
            }
        }
        
        if (table.getSelectionCount() != 0)
        {
            WatchlistTableItem tableItem = (WatchlistTableItem)table.getSelection()[0];
            getView().getSite().getSelectionProvider().setSelection(new WatchlistItemSelection(tableItem.getWatchlistItem()));
        }
        else
            getView().getSite().getSelectionProvider().setSelection(new NullSelection());
    }
    
    public boolean isShowTotals()
    {
        return showTotals;
    }

    public void setShowTotals(boolean showTotals)
    {
        this.showTotals = showTotals;
        TradingPlugin.getDefault().getPreferenceStore().setValue(WatchlistView.PREFS_SHOW_TOTALS + getViewSite().getSecondaryId(), showTotals);
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.internal.AbstractLayout#getSelection()
     */
    public WatchlistItem[] getSelection()
    {
        TableItem selection[] = table.getSelection();
        WatchlistItem[] items = new WatchlistItem[selection.length];
        for (int i = 0; i < selection.length; i++)
        {
            WatchlistTableItem item = (WatchlistTableItem)selection[i];
            items[i] = item.getWatchlistItem();
        }
        return items;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.internal.AbstractLayout#tickAlert(net.sourceforge.eclipsetrader.core.db.WatchlistItem, org.eclipse.swt.graphics.RGB, org.eclipse.swt.graphics.RGB)
     */
    public void tickAlert(WatchlistItem watchlistItem, RGB foreground, RGB background)
    {
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            WatchlistTableItem tableItem = (WatchlistTableItem)items[i];
            if (tableItem.getWatchlistItem().equals(watchlistItem))
            {
                Color fg = foreground != null ? new Color(null, foreground) : null;
                Color bg = background != null ? new Color(null, background) : new Color(null, alertHilightBackground.getRGB());
                tableItem.ticker.tick(bg, fg);
                if (bg != null)
                    bg.dispose();
                if (fg != null)
                    fg.dispose();
                break;
            }
        }
    }
    
    protected void setTheme(ITheme theme)
    {
        positiveForeground = theme.getColorRegistry().get(POSITIVE_FOREGROUND);
        negativeForeground = theme.getColorRegistry().get(NEGATIVE_FOREGROUND);
        evenForeground = theme.getColorRegistry().get(EVEN_ROWS_FOREGROUND);
        evenBackground = theme.getColorRegistry().get(EVEN_ROWS_BACKGROUND);
        oddForeground = theme.getColorRegistry().get(ODD_ROWS_FOREGROUND);
        oddBackground = theme.getColorRegistry().get(ODD_ROWS_BACKGROUND);
        totalsForeground = theme.getColorRegistry().get(TOTALS_ROWS_FOREGROUND);
        totalsBackground = theme.getColorRegistry().get(TOTALS_ROWS_BACKGROUND);
        tickBackground = theme.getColorRegistry().get(TICK_BACKGROUND);
        alertHilightBackground = theme.getColorRegistry().get(ALERT_BACKGROUND);
        theme.addPropertyChangeListener(themeChangeListener);
    }

    public Table getTable()
    {
        return table;
    }
    
    private class WatchlistTableItem extends TableItem implements DisposeListener, Observer, IEditableItem
    {
        private WatchlistItem watchlistItem;
        private CellTicker ticker;

        WatchlistTableItem(Table parent, int style, int index, WatchlistItem watchlistItem)
        {
            super(parent, style, index);
            addDisposeListener(this);
            ticker = new CellTicker(this, CellTicker.BACKGROUND|CellTicker.FOREGROUND);
            setWatchlistItem(watchlistItem);
        }

        WatchlistTableItem(Table parent, int style, WatchlistItem watchlistItem)
        {
            super(parent, style);
            addDisposeListener(this);
            ticker = new CellTicker(this, CellTicker.BACKGROUND|CellTicker.FOREGROUND);
            setWatchlistItem(watchlistItem);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        void setWatchlistItem(WatchlistItem watchlistItem)
        {
            if (this.watchlistItem != null)
            {
                this.watchlistItem.deleteObserver(this);
                Security security = this.watchlistItem.getSecurity();
                if (security != null)
                    FeedMonitor.cancelMonitor(security);
            }
            this.watchlistItem = watchlistItem;
            
            int column = 1;
            for (Iterator iter2 = watchlistItem.getValues().iterator(); iter2.hasNext(); )
            {
                String value = (String)iter2.next();
                setText(column, value);
                if (value.startsWith("+"))
                    setForeground(column, positiveForeground);
                else if (value.startsWith("-"))
                    setForeground(column, negativeForeground);
                else
                    setForeground(column, null);
                column++;
            }
            
            this.watchlistItem.addObserver(this);
        }
        
        WatchlistItem getWatchlistItem()
        {
            return watchlistItem;
        }
        
        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (isDisposed())
                        return;
                    int column = 1;
                    for (Iterator iter2 = watchlistItem.getValues().iterator(); iter2.hasNext(); )
                    {
                        String value = (String)iter2.next();
                        if (!value.equals(getText(column)))
                        {
                            setText(column, value);
                            if (value.startsWith("+"))
                                setForeground(column, positiveForeground);
                            else if (value.startsWith("-"))
                                setForeground(column, negativeForeground);
                            else
                                setForeground(column, null);
                            
                            if (!((Column)table.getColumn(column).getData()).isEditable())
                                ticker.tick(column);
                        }
                        column++;
                    }
                }
            });
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#canEdit(int)
         */
        public boolean canEdit(int index)
        {
            return ((Column)table.getColumn(index).getData()).isEditable();
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#isEditable()
         */
        public boolean isEditable()
        {
            return true;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#itemEdited(int, java.lang.String)
         */
        public void itemEdited(int index, String text)
        {
            Column c = (Column)table.getColumn(index).getData();
            if (c != null)
                c.setText(watchlistItem, text);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e)
        {
            if (watchlistItem != null)
                watchlistItem.deleteObserver(this);
            ticker.dispose();
        }
    }
    
    private class WatchlistTotalsTableItem extends TableItem implements DisposeListener, Observer, IEditableItem
    {
        private WatchlistItem watchlistItem;
        private CellTicker ticker;

        WatchlistTotalsTableItem(Table parent, int style, int index, WatchlistItem watchlistItem)
        {
            super(parent, style, index);
            addDisposeListener(this);
            ticker = new CellTicker(this, CellTicker.BACKGROUND);
            setWatchlistItem(watchlistItem);
        }

        WatchlistTotalsTableItem(Table parent, int style, WatchlistItem watchlistItem)
        {
            super(parent, style);
            addDisposeListener(this);
            ticker = new CellTicker(this, CellTicker.BACKGROUND);
            setWatchlistItem(watchlistItem);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        void setWatchlistItem(WatchlistItem watchlistItem)
        {
            if (this.watchlistItem != null)
                this.watchlistItem.deleteObserver(this);
            this.watchlistItem = watchlistItem;
            
            int column = 1;
            for (Iterator iter2 = watchlistItem.getValues().iterator(); iter2.hasNext(); )
            {
                String value = (String)iter2.next();
                setText(column, value);
                column++;
            }
            
            this.watchlistItem.addObserver(this);

            ticker.setBackground(tickBackground);
        }
        
        WatchlistItem getWatchlistItem()
        {
            return watchlistItem;
        }
        
        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (isDisposed())
                        return;
                    int column = 1;
                    for (Iterator iter2 = watchlistItem.getValues().iterator(); iter2.hasNext(); )
                    {
                        String value = (String)iter2.next();
                        if (!value.equals(getText(column)))
                            setText(column, value);
                        column++;
                    }
                }
            });
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#canEdit(int)
         */
        public boolean canEdit(int index)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#isEditable()
         */
        public boolean isEditable()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem#itemEdited(int, java.lang.String)
         */
        public void itemEdited(int index, String text)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e)
        {
            if (watchlistItem != null)
                watchlistItem.deleteObserver(this);
            ticker.dispose();
        }
    }
}
