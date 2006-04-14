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

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.FeedMonitor;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.db.columns.Column;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTable;
import net.sourceforge.eclipsetrader.core.ui.widgets.EditableTableColumn;
import net.sourceforge.eclipsetrader.core.ui.widgets.IEditableItem;
import net.sourceforge.eclipsetrader.trading.WatchlistItemSelection;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 */
public class TableLayout extends AbstractLayout
{
    public static final String TABLE_EVEN_ROWS_BACKGROUND = "TABLE_EVEN_ROWS_BACKGROUND";
    public static final String TABLE_EVEN_ROWS_FOREGROUND = "TABLE_EVEN_ROWS_FOREGROUND";
    public static final String TABLE_ODD_ROWS_BACKGROUND = "TABLE_ODD_ROWS_BACKGROUND";
    public static final String TABLE_ODD_ROWS_FOREGROUND = "TABLE_ODD_ROWS_FOREGROUND";
    private Composite content;
    private EditableTable table;
    private Color evenForeground;
    private Color evenBackground;
    private Color oddForeground;
    private Color oddBackground;
    private Color negativeForeground = new Color(null, 240, 0, 0);
    private Color positiveForeground = new Color(null, 0, 192, 0);
    private ITheme theme;
    private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(TABLE_EVEN_ROWS_BACKGROUND))
                evenBackground = theme.getColorRegistry().get(TABLE_EVEN_ROWS_BACKGROUND);
            else if (event.getProperty().equals(TABLE_EVEN_ROWS_FOREGROUND))
                evenForeground = theme.getColorRegistry().get(TABLE_EVEN_ROWS_FOREGROUND);
            else if (event.getProperty().equals(TABLE_ODD_ROWS_BACKGROUND))
                oddBackground = theme.getColorRegistry().get(TABLE_ODD_ROWS_BACKGROUND);
            else if (event.getProperty().equals(TABLE_ODD_ROWS_FOREGROUND))
                oddForeground = theme.getColorRegistry().get(TABLE_ODD_ROWS_FOREGROUND);
            updateView();
        }
    };
    
    public TableLayout(WatchlistView view)
    {
        super(view);
    }
    
    public Composite createPartControl(Composite parent)
    {
        content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        theme = themeManager.getCurrentTheme();
        evenForeground = theme.getColorRegistry().get(TABLE_EVEN_ROWS_FOREGROUND);
        evenBackground = theme.getColorRegistry().get(TABLE_EVEN_ROWS_BACKGROUND);
        oddForeground = theme.getColorRegistry().get(TABLE_ODD_ROWS_FOREGROUND);
        oddBackground = theme.getColorRegistry().get(TABLE_ODD_ROWS_BACKGROUND);
        theme.addPropertyChangeListener(themeChangeListener);

        table = new EditableTable(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setBackground(parent.getBackground());
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (table.getSelectionCount() != 0)
                {
                    WatchlistTableItem tableItem = (WatchlistTableItem)table.getSelection()[0];
                    getView().getSite().getSelectionProvider().setSelection(new WatchlistItemSelection(tableItem.getWatchlistItem()));
                }
                else
                    getView().getSite().getSelectionProvider().setSelection(new NullSelection());
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e)
            {
                if (table.getItem(new Point(e.x, e.y)) == null)
                {
                    table.deselectAll();
                    getView().getSite().getSelectionProvider().setSelection(new NullSelection());
                }
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
                menuManager.add(new Separator("top")); //$NON-NLS-1$
                menuManager.add(new Separator("group1")); //$NON-NLS-1$
                menuManager.add(new Separator("group2")); //$NON-NLS-1$
                menuManager.add(new Separator("group3")); //$NON-NLS-1$
                menuManager.add(new Separator("group4")); //$NON-NLS-1$
                menuManager.add(new Separator("group5")); //$NON-NLS-1$
                menuManager.add(new Separator("group6")); //$NON-NLS-1$
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                menuManager.add(new Separator("bottom")); //$NON-NLS-1$
            }
        });
        table.setMenu(menuMgr.createContextMenu(table));
        getView().getSite().registerContextMenu(menuMgr, getView().getSite().getSelectionProvider());

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
        if (content != null)
            content.dispose();
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
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

        table.setItemCount(index);

        for (int i = 1; i < table.getColumnCount(); i++)
        {
            Column c = (Column)table.getColumn(i).getData();
            if (c.getWidth() == 0)
                table.getColumn(i).pack();
            else
                table.getColumn(i).setWidth(c.getWidth());
        }
    }

    public void update(Observable o, Object arg)
    {
        table.getDisplay().syncExec(new Runnable() {
            public void run()
            {
                updateView();
            }
        });
    }

    public void itemAdded(Object o)
    {
        if (o instanceof WatchlistItem)
        {
            WatchlistItem watchlistItem = (WatchlistItem)o;
            int index = table.getItemCount();
            WatchlistTableItem tableItem = new WatchlistTableItem(table, SWT.NONE, index, watchlistItem);
            tableItem.setBackground(((index & 1) == 1) ? oddBackground : evenBackground);
        }
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
}
