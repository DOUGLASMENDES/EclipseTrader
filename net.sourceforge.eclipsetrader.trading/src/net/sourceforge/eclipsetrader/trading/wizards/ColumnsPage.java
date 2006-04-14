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

package net.sourceforge.eclipsetrader.trading.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.columns.Column;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ColumnsPage extends CommonPreferencePage
{
    private Table available;
    private Table shown;

    public ColumnsPage()
    {
        setTitle("Columns");
        setDescription("Select the watchlist columns to show");
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Available columns");

        label = new Label(content, SWT.NONE);
        label.setText("Shown columns");

        Composite column = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        column.setLayout(gridLayout);
        column.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        available = new Table(column, SWT.MULTI|SWT.FULL_SELECTION);
        GridData gridData = new GridData();
        gridData.widthHint = 150;
        gridData.heightHint = available.getItemHeight() * 15;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        available.setLayoutData(gridData);
        available.setHeaderVisible(false);
        available.setLinesVisible(false);
        new TableColumn(available, SWT.NONE);

        Composite buttons = new Composite(column, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        Button button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/right.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveSelectedItems(available, shown, false);
            }
        });
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/all-right.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                available.setSelection(available.getItems());
                moveSelectedItems(available, shown, false);
            }
        });
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/all-left.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                shown.setSelection(shown.getItems());
                moveSelectedItems(shown, available, true);
            }
        });
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/left.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveSelectedItems(shown, available, true);
            }
        });

        column = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        column.setLayout(gridLayout);
        column.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        shown = new Table(column, SWT.MULTI|SWT.FULL_SELECTION);
        gridData = new GridData();
        gridData.widthHint = 150;
        gridData.heightHint = available.getItemHeight() * 15;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        shown.setLayoutData(gridData);
        shown.setHeaderVisible(false);
        shown.setLinesVisible(false);
        new TableColumn(shown, SWT.NONE);
        shown.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                int width = shown.getSize().x;
                shown.getColumn(0).setWidth(width);
            }
        });

        buttons = new Composite(column, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/up.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveUp(shown);
            }
        });
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/down.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveDown(shown);
            }
        });
        
        for (Iterator iter = Column.allColumns().iterator(); iter.hasNext(); )
        {
            Column c = (Column)iter.next();
            try
            {
                TableItem item = new TableItem(available, SWT.NONE);
                item.setText(0, c.getLabel());
                item.setData(c.clone());
            }
            catch (CloneNotSupportedException e) {
                CorePlugin.logException(e);
            }
        }
        available.getColumn(0).pack();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
    }
    
    public List getColumns()
    {
        List list = new ArrayList();

        TableItem[] items = shown.getItems();
        for (int i = 0; i < items.length; i++)
            list.add(items[i].getData());
        
        return list;
    }
    
    public void setColumns(List list)
    {
        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Column column = (Column)iter.next();
            
            TableItem item = new TableItem(shown, SWT.NONE);
            item.setText(0, column.getLabel());
            item.setData(column);

            TableItem[] items = available.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (column.equals(items[i].getData()))
                    items[i].dispose();
            }
        }
    }
    
    private void moveSelectedItems(Table from, Table to, boolean ordered)
    {
        if (from.getSelectionCount() != 0)
        {
            TableItem[] selection = from.getSelection();
            for (int i = 0; i < selection.length; i++)
            {
                TableItem item = null;
                if (ordered)
                {
                    int index = 0;
                    TableItem[] items = to.getItems();
                    for (index = 0; index < items.length; index++)
                    {
                        if (selection[i].getText(0).compareToIgnoreCase(items[index].getText(0)) <= 0)
                            break;
                    }
                    item = new TableItem(to, SWT.NONE, index);
                }
                else
                    item = new TableItem(to, SWT.NONE);

                item.setText(0, selection[i].getText(0));
                item.setData(selection[i].getData());

                selection[i].dispose();
            }
        }
    }
    
    private void moveUp(Table table)
    {
        if (table.getSelectionCount() == 1)
        {
            TableItem[] selection = table.getSelection();
            for (int i = 0; i < selection.length; i++)
            {
                int index = table.indexOf(selection[i]);
                if (index == 0)
                    continue;
                
                TableItem item = new TableItem(table, SWT.NONE, index - 1);
                item.setText(0, selection[i].getText(0));
                item.setData(selection[i].getData());
                table.select(index - 1);

                selection[i].dispose();
            }
        }
    }
    
    private void moveDown(Table table)
    {
        if (table.getSelectionCount() == 1)
        {
            TableItem[] selection = table.getSelection();
            for (int i = 0; i < selection.length; i++)
            {
                int index = table.indexOf(selection[i]);
                if (index >= (table.getItemCount() - 1))
                    continue;
                
                TableItem item = new TableItem(table, SWT.NONE, index + 2);
                item.setText(0, selection[i].getText(0));
                item.setData(selection[i].getData());
                table.select(index + 2);

                selection[i].dispose();
            }
        }
    }
}
