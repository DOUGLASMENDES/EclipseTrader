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

package net.sourceforge.eclipsetrader.charts.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class DataView extends ViewPart
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.chartData";
    private Table table;

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);

        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Date");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Open");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("High");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Low");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Close");
        
        table.pack();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
    }
}
