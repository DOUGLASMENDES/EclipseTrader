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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemePreview;

public class TableViewerThemePreview implements IThemePreview, IPropertyChangeListener
{
    private Table table;
    private TableItem item1;
    private TableItem item2;
    private TableItem item3;
    private TableItem item4;
    private TableItem totals;

    public TableViewerThemePreview()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.themes.ITheme)
     */
    public void createControl(Composite parent, ITheme currentTheme)
    {
        table = new Table(parent, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setBackground(parent.getBackground());
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                table.deselectAll();
                table.getParent().setFocus();
            }
        });
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);

        column = new TableColumn(table, SWT.NONE);
        column.setText("Code");
        column.setWidth(50);
        column = new TableColumn(table, SWT.NONE);
        column.setText("Description");
        column.setWidth(130);
        column = new TableColumn(table, SWT.NONE);
        column.setText("Last");
        column.setWidth(60);
        column = new TableColumn(table, SWT.NONE);
        column.setText("Change");
        column.setWidth(50);
        
        Color positiveForeground = currentTheme.getColorRegistry().get(WatchlistTableViewer.POSITIVE_FOREGROUND);
        Color negativeForeground = currentTheme.getColorRegistry().get(WatchlistTableViewer.NEGATIVE_FOREGROUND);
        Color evenForeground = currentTheme.getColorRegistry().get(WatchlistTableViewer.EVEN_ROWS_FOREGROUND);
        Color evenBackground = currentTheme.getColorRegistry().get(WatchlistTableViewer.EVEN_ROWS_BACKGROUND);
        Color oddForeground = currentTheme.getColorRegistry().get(WatchlistTableViewer.ODD_ROWS_FOREGROUND);
        Color oddBackground = currentTheme.getColorRegistry().get(WatchlistTableViewer.ODD_ROWS_BACKGROUND);
        Color totalsForeground = currentTheme.getColorRegistry().get(WatchlistTableViewer.TOTALS_ROWS_FOREGROUND);
        Color totalsBackground = currentTheme.getColorRegistry().get(WatchlistTableViewer.TOTALS_ROWS_BACKGROUND);
        Color tickBackground = currentTheme.getColorRegistry().get(WatchlistTableViewer.TICK_BACKGROUND);
        Color alertBackground = currentTheme.getColorRegistry().get(WatchlistTableViewer.ALERT_BACKGROUND);

        item1 = new TableItem(table, SWT.NONE); 
        item1.setBackground(evenBackground);
        item1.setForeground(evenForeground);
        item1.setText(1, "AAPL");
        item1.setText(2, "Apple");
        item1.setText(3, "61.6600");
        item1.setText(4, "-0.82%");
        item1.setForeground(4, negativeForeground);
        
        item2 = new TableItem(table, SWT.NONE); 
        item2.setBackground(oddBackground);
        item2.setForeground(oddForeground);
        item2.setText(1, "ERTS");
        item2.setText(2, "Electronic Arts");
        item2.setText(3, "42.4100");
        item2.setText(4, "+1.29%");
        item2.setForeground(4, positiveForeground);
        item2.setBackground(3, tickBackground);
        item2.setBackground(4, tickBackground);
        
        item3 = new TableItem(table, SWT.NONE); 
        item3.setBackground(evenBackground);
        item3.setForeground(evenForeground);
        item3.setText(1, "RHAT");
        item3.setText(2, "Red Hat");
        item3.setText(3, "28.3900");
        item3.setText(4, "-1.17%");
        item3.setForeground(4, negativeForeground);
        item3.setBackground(3, tickBackground);
        item3.setBackground(4, tickBackground);
        
        item4 = new TableItem(table, SWT.NONE); 
        item4.setBackground(alertBackground);
        item4.setForeground(oddForeground);
        item4.setText(1, "YHOO");
        item4.setText(2, "Yahoo!");
        item4.setText(3, "31.5200");
        item4.setText(4, "+1.47%");
        item4.setForeground(4, positiveForeground);
        
        totals = new TableItem(table, SWT.NONE); 
        totals.setBackground(totalsBackground);
        totals.setForeground(totalsForeground);
        totals.setText(2, "Totals");
        
        currentTheme.addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IThemePreview#dispose()
     */
    public void dispose()
    {
        table.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getProperty().equals(WatchlistTableViewer.EVEN_ROWS_BACKGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item1.setBackground(color);
            item3.setBackground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.EVEN_ROWS_FOREGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item1.setForeground(color);
            item3.setForeground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.ODD_ROWS_BACKGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item2.setBackground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.ODD_ROWS_FOREGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item2.setForeground(color);
            item4.setForeground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.TOTALS_ROWS_BACKGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            totals.setBackground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.TOTALS_ROWS_FOREGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            totals.setForeground(color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.NEGATIVE_FOREGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item1.setForeground(4, color);
            item3.setForeground(4, color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.POSITIVE_FOREGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item2.setForeground(4, color);
            item4.setForeground(4, color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.TICK_BACKGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item2.setBackground(3, color);
            item2.setBackground(4, color);
            item3.setBackground(1, color);
            item3.setBackground(2, color);
            color.dispose();
        }
        else if (event.getProperty().equals(WatchlistTableViewer.ALERT_BACKGROUND))
        {
            Color color = new Color(table.getDisplay(), (RGB) event.getNewValue());
            item4.setBackground(color);
            color.dispose();
        }
    }
}
