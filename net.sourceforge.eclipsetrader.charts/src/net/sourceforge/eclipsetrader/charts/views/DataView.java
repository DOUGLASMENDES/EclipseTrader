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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.charts.Indicator;
import net.sourceforge.eclipsetrader.charts.Plot;
import net.sourceforge.eclipsetrader.charts.PlotLine;
import net.sourceforge.eclipsetrader.charts.views.ChartView.ChartTabItem;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class DataView extends ViewPart implements IPartListener, Observer
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.chartData";
    private Table table;
    private ChartView chartView;
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private NumberFormat nf = NumberFormat.getInstance();
    
    public DataView()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
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
        
        table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
        
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();
        
        partActivated(getViewSite().getWorkbenchWindow().getPartService().getActivePart());
        getViewSite().getWorkbenchWindow().getPartService().addPartListener(this);
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
        if (chartView != null)
            chartView.getChart().deleteObserver(this);
        getViewSite().getWorkbenchWindow().getPartService().removePartListener(this);
        super.dispose();
    }

    private void updateView()
    {
        if (table.isDisposed())
            return;
        
        setContentDescription((chartView != null) ? chartView.getPartName() : "");

        table.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                int columnIndex = 6;
                int rowIndex = 0;
                TableItem tableItem = null;

                if (table.isDisposed())
                    return;

                if (chartView != null)
                {
                    for (Iterator tg = chartView.getTabGroups().iterator(); tg.hasNext(); )
                    {
                        CTabFolder folder = (CTabFolder)tg.next();
                        CTabItem[] items = folder.getItems();
                        for (int i = 0; i < items.length; i++)
                        {
                            Plot plot = ((ChartTabItem)items[i]).getPlot();
                            for (Iterator ind = plot.getIndicatorPlot().getIndicators().iterator(); ind.hasNext(); )
                            {
                                Indicator indicator = (Indicator) ind.next();
                                for (Iterator ln = indicator.getLines().iterator(); ln.hasNext(); )
                                {
                                    PlotLine line = (PlotLine) ln.next();
                                    if (line.getLabel() == null || line.getLabel().length() == 0)
                                        continue;

                                    TableColumn column = null;
                                    if (columnIndex < table.getColumnCount())
                                        column = table.getColumn(columnIndex);
                                    else
                                        column = new TableColumn(table, SWT.RIGHT);
                                    column.setText(line.getLabel());
                                    columnIndex++;
                                }
                            }
                        }
                    }
                }
                while (table.getColumnCount() > columnIndex)
                    table.getColumn(table.getColumnCount() - 1).dispose();

                if (chartView != null)
                {
                    BarData barData = chartView.getDatePlot().getBarData();
                    table.setItemCount(barData.size());

                    rowIndex = table.getItemCount() - 1;
                    for (Iterator iter = barData.iterator(); iter.hasNext(); )
                    {
                        Bar bar = (Bar) iter.next();
                        tableItem = table.getItem(rowIndex);
                        tableItem.setText(1, df.format(bar.getDate()));
                        tableItem.setText(2, nf.format(bar.getOpen()));
                        tableItem.setText(3, nf.format(bar.getHigh()));
                        tableItem.setText(4, nf.format(bar.getLow()));
                        tableItem.setText(5, nf.format(bar.getClose()));
                        
                        rowIndex--;
                    }
                    
                    columnIndex = 6;
                    for (Iterator tg = chartView.getTabGroups().iterator(); tg.hasNext(); )
                    {
                        CTabFolder folder = (CTabFolder)tg.next();
                        CTabItem[] items = folder.getItems();
                        for (int i = 0; i < items.length; i++)
                        {
                            Plot plot = ((ChartTabItem)items[i]).getPlot();
                            for (Iterator ind = plot.getIndicatorPlot().getIndicators().iterator(); ind.hasNext(); )
                            {
                                Indicator indicator = (Indicator) ind.next();
                                for (Iterator ln = indicator.getLines().iterator(); ln.hasNext(); )
                                {
                                    PlotLine line = (PlotLine) ln.next();
                                    if (line.getLabel() == null || line.getLabel().length() == 0)
                                        continue;
                                    
                                    rowIndex = table.getItemCount() - (table.getItemCount() - line.getSize()) - 1;
                                    while(rowIndex < table.getItemCount())
                                        table.getItem(rowIndex++).setText(columnIndex, "");
                                    
                                    rowIndex = table.getItemCount() - (table.getItemCount() - line.getSize()) - 1;
                                    for (Iterator iter = line.iterator(); iter.hasNext(); )
                                    {
                                        Object value = iter.next();
                                        if (value instanceof Double)
                                            table.getItem(rowIndex--).setText(columnIndex, nf.format(value));
                                    }
                                    
                                    columnIndex++;
                                }
                            }
                        }
                    }
                }
                else
                    table.setItemCount(0);
                
                for (int i = 1; i < table.getColumnCount(); i++)
                    table.getColumn(i).pack();
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part)
    {
        if (part instanceof ChartView && part != chartView)
        {
            if (chartView != null)
                chartView.getChart().deleteObserver(this);
            chartView = (ChartView) part;
            chartView.getChart().addObserver(this);
            updateView();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part)
    {
        if (part instanceof ChartView && part == chartView)
        {
            if (chartView != null)
                chartView.getChart().deleteObserver(this);
            chartView = null;
            updateView();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part)
    {
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        updateView();
    }
}
