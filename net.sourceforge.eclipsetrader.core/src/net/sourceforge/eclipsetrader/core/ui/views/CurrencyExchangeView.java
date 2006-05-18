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

package net.sourceforge.eclipsetrader.core.ui.views;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CurrencyConverter;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class CurrencyExchangeView extends ViewPart implements Observer
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.currencies";
    private Color evenForeground = new Color(null, 0, 0, 0);
    private Color evenBackground = new Color(null, 255, 255, 255);
    private Color oddForeground = new Color(null, 0, 0, 0);
    private Color oddBackground = new Color(null, 210, 240, 210);
    private Table table;
    private NumberFormat nf = NumberFormat.getInstance();
    
    public CurrencyExchangeView()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
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
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        table = new Table(content, SWT.MULTI|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                table.deselectAll();
            }
        });
        new TableColumn(table, SWT.NONE);

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateView();
                CurrencyConverter.getInstance().addObserver(CurrencyExchangeView.this);
            }
        });
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
        CurrencyConverter.getInstance().deleteObserver(this);
        evenForeground.dispose();
        evenBackground.dispose();
        oddForeground.dispose();
        oddBackground.dispose();
        super.dispose();
    }

    public void updateView()
    {
        table.setRedraw(false);
        
        List currencies = CurrencyConverter.getInstance().getCurrencies();

        int index = 1;
        for (Iterator iter = currencies.iterator(); iter.hasNext(); )
        {
            TableColumn column = null;
            if (index < table.getColumnCount())
                column = table.getColumn(index);
            else
                column = new TableColumn(table, SWT.RIGHT);
            column.setText((String)iter.next());
            index++;
        }
        while(table.getColumnCount() > index)
            table.getColumn(index).dispose();
        
        index = 0;
        for (Iterator iter = currencies.iterator(); iter.hasNext(); )
        {
            String symbol = (String)iter.next();
            TableItem tableItem = null;
            if (index < table.getItemCount())
                tableItem = table.getItem(index);
            else
                tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, symbol);
            for (int i = 0; i < currencies.size(); i++)
            {
                if (i != index)
                    tableItem.setText(i + 1, nf.format(CurrencyConverter.getInstance().convert(1, symbol, (String)currencies.get(i))));
            }
            tableItem.setBackground(0, tableItem.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            tableItem.setBackground(((index & 1) == 1) ? oddBackground : evenBackground);
            tableItem.setForeground(((index & 1) == 1) ? oddForeground : evenForeground);
            index++;
        }
        table.setItemCount(index);

        table.setRedraw(true);
        
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
        table.getColumn(0).setWidth(table.getColumn(0).getWidth() + 5);
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
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
}
