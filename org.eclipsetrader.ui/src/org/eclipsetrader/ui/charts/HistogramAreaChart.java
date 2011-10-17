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

package org.eclipsetrader.ui.charts;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IOHLC;

/**
 * Draws an histogram area chart.
 *
 * @since 1.0
 */
public class HistogramAreaChart implements IChartObject, ISummaryBarDecorator, IAdaptable {

    private IDataSeries dataSeries;
    private OHLCField field;

    private IAdaptable[] values;
    private List<Polygon> pointArray = new ArrayList<Polygon>(2048);
    private boolean valid;
    private boolean focus;

    private RGB color = new RGB(0, 0, 0);
    private RGB fillColor;

    private SummaryDateItem dateItem;
    private SummaryOHLCItem ohlcItem;
    private SummaryNumberItem numberItem;

    private DateFormat dateFormat = DateFormat.getDateInstance();
    private NumberFormat numberFormat = NumberFormat.getInstance();

    public HistogramAreaChart(IDataSeries dataSeries, RGB color) {
        this(dataSeries, OHLCField.Close, color);
    }

    public HistogramAreaChart(IDataSeries dataSeries, OHLCField field, RGB color) {
        this.dataSeries = dataSeries;
        this.field = field;

        if (color != null) {
            this.color = color;
        }

        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(4);
    }

    public RGB getColor() {
        return color;
    }

    public void setColor(RGB color) {
        this.color = color;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
     */
    @Override
    public void setDataBounds(DataBounds dataBounds) {
        List<IAdaptable> l = new ArrayList<IAdaptable>(2048);
        for (IAdaptable value : dataSeries.getValues()) {
            Date date = (Date) value.getAdapter(Date.class);
            if ((dataBounds.first == null || !date.before(dataBounds.first)) && (dataBounds.last == null || !date.after(dataBounds.last))) {
                l.add(value);
            }
        }
        this.values = l.toArray(new IAdaptable[l.size()]);
        this.valid = false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
     */
    @Override
    public void invalidate() {
        this.valid = false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    @Override
    public void paint(IGraphics graphics) {
        if (!valid && values != null) {
            int zero = graphics.mapToVerticalAxis(0.0);

            double[] inReal = Util.getValuesForField(values, field);

            for (int i = 0; i < values.length - 1; i++) {
                Date date1 = (Date) values[i].getAdapter(Date.class);
                Date date2 = (Date) values[i + 1].getAdapter(Date.class);

                int x1 = graphics.mapToHorizontalAxis(date1);
                int y1 = graphics.mapToVerticalAxis(inReal[i]);
                int x2 = graphics.mapToHorizontalAxis(date2);
                int y2 = graphics.mapToVerticalAxis(inReal[i + 1]);

                Polygon candle;
                if (i < pointArray.size()) {
                    candle = pointArray.get(i);
                }
                else {
                    candle = new Polygon();
                    pointArray.add(candle);
                }
                candle.setBounds(zero, x1, y1, x2, y2);
                candle.setValue(values[i]);
            }
            while (pointArray.size() > values.length - 1) {
                pointArray.remove(pointArray.size() - 1);
            }
        }

        if (fillColor == null) {
            fillColor = Util.blend(color, graphics.getBackgroundColor(), 25);
        }

        graphics.pushState();
        graphics.setLineWidth(hasFocus() ? 2 : 1);
        for (Polygon c : pointArray) {
            c.paint(graphics);
        }
        graphics.popState();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
     */
    @Override
    public void paintScale(Graphics graphics) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
     */
    @Override
    public boolean containsPoint(int x, int y) {
        for (Polygon c : pointArray) {
            if (c.containsPoint(x, y)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
     */
    @Override
    public IDataSeries getDataSeries() {
        return dataSeries;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
     */
    @Override
    public String getToolTip() {
        if (dataSeries.getLast() != null) {
            IOHLC ohlc = (IOHLC) dataSeries.getLast().getAdapter(IOHLC.class);
            if (ohlc != null) {
                return dateFormat.format(ohlc.getDate()) + " O:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
                " H:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
                " L:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
                " C:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
            }
            return dataSeries.getName() + ": " + numberFormat.format(dataSeries.getLast().getAdapter(Number.class)); //$NON-NLS-1$
        }
        return dataSeries.getName();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    @Override
    public String getToolTip(int x, int y) {
        for (Polygon c : pointArray) {
            if (c.containsPoint(x, y)) {
                return c.getToolTip();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusGained(ChartObjectFocusEvent event) {
        focus = true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusLost(ChartObjectFocusEvent event) {
        focus = false;
    }

    protected boolean hasFocus() {
        return focus;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    @Override
    public void accept(IChartObjectVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ISummaryBarDecorator.class)) {
            return this;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#createDecorator(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createDecorator(Composite parent) {
        IAdaptable[] values = dataSeries.getValues();
        if (values.length == 0) {
            return;
        }

        IOHLC ohlc = (IOHLC) (values.length > 0 ? values[values.length - 1].getAdapter(IOHLC.class) : null);
        if (ohlc != null) {
            IOHLC previousOhlc = (IOHLC) (values.length > 1 ? values[values.length - 2].getAdapter(IOHLC.class) : null);

            dateItem = new SummaryDateItem(parent, SWT.DATE);
            dateItem.setDate(ohlc != null ? ohlc.getDate() : null);

            ohlcItem = new SummaryOHLCItem(parent, SWT.NONE);
            ohlcItem.setOHLC(ohlc, previousOhlc);
        }
        else {
            Number value = (Number) (values.length > 0 ? values[values.length - 1].getAdapter(Number.class) : null);

            numberItem = new SummaryNumberItem(parent, SWT.NONE);
            numberItem.setValue(dataSeries.getName() + ": ", value); //$NON-NLS-1$
            if (color != null) {
                numberItem.setForeground(color);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#updateDecorator(int, int)
     */
    @Override
    public void updateDecorator(int x, int y) {
        if (pointArray != null) {
            for (int i = 0; i < pointArray.size(); i++) {
                if (pointArray.get(i).containsPoint(x, y)) {
                    if (numberItem != null) {
                        Number value = (Number) values[i].getAdapter(Number.class);
                        if (value != null) {
                            numberItem.setValue(dataSeries.getName() + ": ", value); //$NON-NLS-1$
                        }
                    }
                    if (dateItem != null && ohlcItem != null) {
                        IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                        IOHLC previousOhlc = (IOHLC) (i >= 1 ? values[i - 1].getAdapter(IOHLC.class) : null);
                        dateItem.setDate(ohlc.getDate());
                        ohlcItem.setOHLC(ohlc, previousOhlc);
                    }
                }
            }
        }
    }

    public class Polygon {

        int x1;
        int x2;
        int y1;
        int y2;
        int yZero;
        int[] polygon = new int[8];
        Point[] points = new Point[5];
        IAdaptable value;

        public Polygon() {
        }

        public void setBounds(int yZero, int x1, int y1, int x2, int y2) {
            this.yZero = yZero;
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;

            points[0] = new Point(x1, yZero);
            points[1] = new Point(x1, y1);
            points[2] = new Point(x2, y2);
            points[3] = new Point(x2, yZero);
            points[4] = points[0];

            polygon[0] = x1;
            polygon[1] = yZero;
            polygon[2] = x1;
            polygon[3] = y1;
            polygon[4] = x2;
            polygon[5] = y2;
            polygon[6] = x2;
            polygon[7] = yZero;
        }

        public void setValue(IAdaptable value) {
            this.value = value;
        }

        public void paint(IGraphics graphics) {
            graphics.setBackgroundColor(fillColor);
            graphics.fillPolygon(polygon);
            graphics.setForegroundColor(color);
            graphics.drawLine(x1, y1, x2, y2);
            graphics.drawLine(x1, yZero, x2, yZero);
        }

        public boolean containsPoint(int x, int y) {
            if (y == SWT.DEFAULT) {
                return x >= x1 && x < x2;
            }

            int crossings = 0;
            for (int i = 0; i < points.length - 1; i++) {
                int div = points[i + 1].y - points[i].y;
                if (div == 0) {
                    div = 1;
                }
                double slope = (points[i + 1].x - points[i].x) / div;
                boolean cond1 = points[i].y <= y && y < points[i + 1].y;
                boolean cond2 = points[i + 1].y <= y && y < points[i].y;
                boolean cond3 = x < slope * (y - points[i].y) + points[i].x;
                if ((cond1 || cond2) && cond3) {
                    crossings++;
                }
            }
            return crossings % 2 != 0;
        }

        public String getToolTip() {
            IOHLC ohlc = (IOHLC) value.getAdapter(IOHLC.class);
            if (ohlc != null) {
                return dataSeries.getName() + "\r\nD:" + dateFormat.format(ohlc.getDate()) + //$NON-NLS-1$
                "\r\nO:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
                "\r\nH:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
                "\r\nL:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
                "\r\nC:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
            }
            return dataSeries.getName() + ": " + numberFormat.format(value.getAdapter(Number.class)); //$NON-NLS-1$
        }
    }
}
