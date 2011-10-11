/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.ats;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipsetrader.core.ats.Report;
import org.eclipsetrader.core.ats.simulation.SimulationReport;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.OHLC;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IStockTransaction;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.ui.Util;
import org.eclipsetrader.ui.charts.BarChart;
import org.eclipsetrader.ui.charts.BaseChartViewer;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.LineChart;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;
import org.eclipsetrader.ui.charts.indicators.VOLUME;

public class ReportViewPart extends ViewPart {

    public static final String VIEW_ID = "org.eclipsetrader.ui.ats.views.report";

    private BaseChartViewer equityChartViewer;
    private TableViewer tradesViewer;

    public ReportViewPart() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        createSummaryPage(parent);
    }

    private Control createSummaryPage(Composite parent) {
        SashForm content = new SashForm(parent, SWT.VERTICAL | SWT.NO_FOCUS);

        GC gc = new GC(content);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        Composite composite = new Composite(content, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Performance");

        equityChartViewer = new BaseChartViewer(composite, SWT.BORDER);
        equityChartViewer.setFillAvailableSpace(true);
        equityChartViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        composite = new Composite(content, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        label = new Label(composite, SWT.NONE);
        label.setText("Trades");

        tradesViewer = new TableViewer(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        tradesViewer.getTable().setHeaderVisible(true);
        tradesViewer.getTable().setLinesVisible(false);
        tradesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        content.setWeights(new int[] {
            75, 25
        });

        final DateFormat dateFormat = Util.getDateFormat();
        final NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        final NumberFormat priceNumberFormat = NumberFormat.getInstance();
        priceNumberFormat.setGroupingUsed(false);
        priceNumberFormat.setMinimumFractionDigits(1);
        priceNumberFormat.setMaximumFractionDigits(4);

        TableViewerColumn vewerColumn = new TableViewerColumn(tradesViewer, SWT.NONE);
        vewerColumn.getColumn().setText("Date / Time");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 60));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                ITransaction transaction = (ITransaction) cell.getElement();
                cell.setText(dateFormat.format(transaction.getOrder().getDate()));
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.NONE);
        vewerColumn.getColumn().setText("Instrument");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 150));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                IOrder order = ((ITransaction) cell.getElement()).getOrder();
                if (order != null) {
                    cell.setText(order.getSecurity().getName());
                }
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.NONE);
        vewerColumn.getColumn().setText("Side");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 50));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                IOrder order = ((ITransaction) cell.getElement()).getOrder();
                if (order != null) {
                    cell.setText(order.getSide().toString());
                }
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.RIGHT);
        vewerColumn.getColumn().setText("Q.ty");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 60));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                ITransaction transaction = (ITransaction) cell.getElement();
                if (transaction instanceof IStockTransaction) {
                    cell.setText(String.valueOf(((IStockTransaction) transaction).getFilledQuantity()));
                }
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.RIGHT);
        vewerColumn.getColumn().setText("Avg. Price");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 60));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                ITransaction transaction = (ITransaction) cell.getElement();
                if (transaction instanceof IStockTransaction) {
                    cell.setText(priceNumberFormat.format(((IStockTransaction) transaction).getAveragePrice()));
                }
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.RIGHT);
        vewerColumn.getColumn().setText("Amount");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 60));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                ITransaction transaction = (ITransaction) cell.getElement();
                IOrder order = transaction.getOrder();
                double amount = transaction.getAmount().getAmount();
                if (order != null) {
                    cell.setText(numberFormat.format(order.getSide() == IOrderSide.Buy ? -amount : amount));
                }
                else {
                    cell.setText(numberFormat.format(amount));
                }
            }
        });

        vewerColumn = new TableViewerColumn(tradesViewer, SWT.NONE);
        vewerColumn.getColumn().setText("Message");
        vewerColumn.getColumn().setWidth(Dialog.convertHorizontalDLUsToPixels(fontMetrics, 100));
        vewerColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                IOrder order = ((ITransaction) cell.getElement()).getOrder();
                if (order != null && order.getReference() != null) {
                    cell.setText(order.getReference());
                }
            }
        });

        tradesViewer.setContentProvider(new ArrayContentProvider());

        return content;
    }

    private Control createSecurityPage(Composite parent, ISecurity security, List<IBar> bars) {
        BaseChartViewer chartViewer = new BaseChartViewer(parent, SWT.NONE);
        chartViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        List<IOHLC> list = new ArrayList<IOHLC>();
        for (IBar bar : bars) {
            list.add(new OHLC(bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume()));
        }

        OHLCDataSeries dataSeries = new OHLCDataSeries(security.getName(), list.toArray(new IOHLC[list.size()]), null);

        chartViewer.setInput(new IChartObject[][] {
            new IChartObject[] {
                new BarChart(dataSeries),
            },
            new IChartObject[] {
                new VOLUME().createObject(dataSeries)
            }
        });

        return chartViewer.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        equityChartViewer.getControl().setFocus();
    }

    public void setReport(Report report) {
        String title = NLS.bind("Report: {0}", new Object[] {
            report.getStrategy().getName()
        });
        if (report instanceof SimulationReport) {
            SimulationReport simulationReport = (SimulationReport) report;
            DateFormat dateFormat = Util.getDateFormat();
            title = NLS.bind("Report: {0} [{1}-{2}]", new Object[] {
                report.getStrategy().getName(),
                dateFormat.format(simulationReport.getBegin()),
                dateFormat.format(simulationReport.getEnd())
            });
        }
        setPartName(title);

        LineChart lineChart = new LineChart(report.getEquityData(), LineStyle.Solid, new RGB(0, 0, 224));
        equityChartViewer.setInput(new IChartObject[][] {
            new IChartObject[] {
                lineChart
            },
        });

        equityChartViewer.setSummaryVisible(false);

        tradesViewer.setInput(report.getTradesData());
    }
}
