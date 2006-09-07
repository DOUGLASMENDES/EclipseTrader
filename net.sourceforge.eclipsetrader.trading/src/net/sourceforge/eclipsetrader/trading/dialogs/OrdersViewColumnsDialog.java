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

package net.sourceforge.eclipsetrader.trading.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.core.ui.LabelProvidersRegistry;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.views.OrdersView;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class OrdersViewColumnsDialog extends Dialog
{
    Table available;
    Table shown;
    
    private class Element
    {
        String label = "";
        String id = "";
        String width = "";
        
        Element(String label, String id)
        {
            this.label = label;
            this.id = id;
            this.width = "75";
        }

        public boolean equals(Object obj)
        {
            if (!(obj instanceof Element))
                return false;
            Element that = (Element)obj;
            return this.id.equals(that.id);
        }
    }

    public OrdersViewColumnsDialog(Shell parentShell)
    {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText("Orders View Columns");
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = (Composite)super.createDialogArea(parent);
        ((GridLayout)content.getLayout()).numColumns = 2;
        ((GridLayout)content.getLayout()).makeColumnsEqualWidth = true;

        Label label = new Label(content, SWT.NONE);
        label.setText("Available columns");

        label = new Label(content, SWT.NONE);
        label.setText("Shown columns");

        Composite column = new Composite(content, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
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

        List columns = new ArrayList();
        IConfigurationElement[] members = new LabelProvidersRegistry(OrdersView.VIEW_ID).getProviders();
        for (int i = 0; i < members.length; i++)
            columns.add(new Element(members[i].getAttribute("name"), members[i].getAttribute("id")));
        Collections.sort(columns, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Element)arg0).label.compareTo(((Element)arg1).label);
            }
        });
        for (Iterator iter = columns.iterator(); iter.hasNext(); )
        {
            Element c = (Element)iter.next();
            TableItem item = new TableItem(available, SWT.NONE);
            item.setText(0, c.label);
            item.setData(c);
        }
        available.getColumn(0).pack();

        String[] id = TradingPlugin.getDefault().getPreferenceStore().getString(OrdersView.PREFS_ORDERS_COLUMNS).split(";");
        String[] sizes = TradingPlugin.getDefault().getPreferenceStore().getString(OrdersView.PREFS_COLUMNS_SIZE).split(";");
        for (int i = 0; i < id.length; i++)
        {
            int index = columns.indexOf(new Element("", id[i]));
            if (index != -1)
            {
                Element c = (Element)columns.get(index);
                if (i < sizes.length && sizes[i].length() != 0)
                    c.width = sizes[i];

                TableItem item = new TableItem(shown, SWT.NONE);
                item.setText(0, c.label);
                item.setData(c);

                available.getItem(index).dispose();
                columns.remove(index);
            }
        }
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        StringBuffer columns = new StringBuffer();
        StringBuffer sizes = new StringBuffer();

        TableItem[] items = shown.getItems();
        for (int i = 0; i < items.length; i++)
        {
            Element c = (Element)items[i].getData();
            if (i > 0)
            {
                columns.append(";");
                sizes.append(";");
            }
            columns.append(c.id);
            sizes.append(c.width);
        }
        
        TradingPlugin.getDefault().getPreferenceStore().setValue(OrdersView.PREFS_COLUMNS_SIZE, sizes.toString());
        TradingPlugin.getDefault().getPreferenceStore().setValue(OrdersView.PREFS_ORDERS_COLUMNS, columns.toString());
        
        super.okPressed();
    }
    
    void moveSelectedItems(Table from, Table to, boolean ordered)
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
    
    void moveUp(Table table)
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
    
    void moveDown(Table table)
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
