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

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ItemsPage extends CommonPreferencePage
{
    private Table table;

    public ItemsPage()
    {
        setTitle("Items");
        setDescription("Manage the items shown in the watchlist");
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

        table = new Table(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Code");
        column = new TableColumn(table, SWT.NONE);
        column.setText("Description");

        Composite buttons = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        
        Button button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/up.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveUp();
            }
        });
        
        button = new Button(buttons, SWT.PUSH);
        button.setImage(TradingPlugin.getImageDescriptor("icons/buttons16/down.gif").createImage());
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                moveDown();
            }
        });

        buttons = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        buttons.setLayout(gridLayout);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        
        button = new Button(buttons, SWT.PUSH);
        button.setText("Delete");
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                deleteSelected();
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
    }
    
    public void setItems(List items)
    {
        for (Iterator iter = items.iterator(); iter.hasNext(); )
        {
            WatchlistItem item = (WatchlistItem)iter.next();
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, item.getSecurity().getCode());
            tableItem.setText(1, item.getSecurity().getDescription());
            tableItem.setData(item);
        }
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
    }
    
    public List getItems()
    {
        List list = new ArrayList();
        
        for (int i = 0; i < table.getItemCount(); i++)
            list.add(table.getItem(i).getData());
        
        return list;
    }
    
    private void moveUp()
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
                item.setText(1, selection[i].getText(1));
                item.setData(selection[i].getData());
                table.select(index - 1);

                selection[i].dispose();
            }
        }
    }
    
    private void moveDown()
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
                item.setText(1, selection[i].getText(1));
                item.setData(selection[i].getData());
                table.select(index + 2);

                selection[i].dispose();
            }
        }
    }
    
    private void deleteSelected()
    {
        TableItem[] selection = table.getSelection();
        for (int i = 0; i < selection.length; i++)
            selection[i].dispose();
    }
}
