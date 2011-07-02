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
 * Draws a line chart.
 *
 * @since 1.0
 */
public class OHLCLineChart implements IChartObject, ISummaryBarDecorator, IAdaptable {

    private IDataSeries dataSeries;

    private LineStyle style;
    private RGB color;
    private int width = 5;

    private IAdaptable[] values;
    private Point[] pointArray;
    private boolean valid;
    private boolean hasFocus;

    private SummaryDateItem dateItem;
    private SummaryOHLCItem ohlcItem;

    private DateFormat dateFormat = DateFormat.getDateInstance();
    private NumberFormat numberFormat = NumberFormat.getInstance();

    public static enum LineStyle {
        Solid, Dot, Dash, Invisible
    }

    public OHLCLineChart(IDataSeries dataSeries, LineStyle style, RGB color) {
        this.dataSeries = dataSeries;
        this.style = style;
        this.color = color;

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
        this.width = dataBounds.horizontalSpacing;
        this.valid = false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusGained(ChartObjectFocusEvent event) {
        hasFocus = true;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusLost(ChartObjectFocusEvent event) {
        hasFocus = false;
    }

    protected boolean hasFocus() {
        return hasFocus;
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
        if ((!valid || pointArray == null) && values != null && style != LineStyle.Invisible) {
            pointArray = new Point[values.length];
            for (int i = 0; i < values.length; i++) {
                Date date = (Date) values[i].getAdapter(Date.class);
                Number value = (Number) values[i].getAdapter(Number.class);
                pointArray[i] = graphics.mapToPoint(date, value);
            }
            valid = true;
        }

        if (pointArray != null && style != LineStyle.Invisible) {
            switch (style) {
                case Dash:
                    graphics.setLineStyle(SWT.LINE_DASH);
                    break;
                case Dot:
                    graphics.setLineStyle(SWT.LINE_DOT);
                    break;
                default:
                    graphics.setLineStyle(SWT.LINE_SOLID);
                    break;
            }

            graphics.pushState();
            graphics.setForegroundColor(color);
            graphics.setLineWidth(hasFocus() ? 2 : 1);
            graphics.drawPolyline(pointArray);
            graphics.popState();
        }
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
        if (pointArray != null) {
            if (y == SWT.DEFAULT) {
                return true;
            }
            return PixelTools.isPointOnLine(x, y, pointArray);
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
            return dateFormat.format(ohlc.getDate()) + " O:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
            " H:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
            " L:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
            " C:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
        }
        return dataSeries.getName();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    @Override
    public String getToolTip(int x, int y) {
        if (pointArray != null) {
            if (y == SWT.DEFAULT) {
                for (int i = 0; i < pointArray.length; i++) {
                    if (x >= pointArray[i].x - width / 2 && x <= pointArray[i].x + width / 2) {
                        return getTooltipAt(i);
                    }
                }
            }
            else {
                for (int i = 1; i < pointArray.length; i++) {
                    if (PixelTools.isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y)) {
                        return getTooltipAt(i - 1);
                    }
                }
            }
        }
        return null;
    }

    String getTooltipAt(int index) {
        IOHLC ohlc = (IOHLC) values[index].getAdapter(IOHLC.class);
        return dataSeries.getName() + "\r\nD:" + dateFormat.format(ohlc.getDate()) + //$NON-NLS-1$
        "\r\nO:" + numberFormat.format(ohlc.getOpen()) + //$NON-NLS-1$
        "\r\nH:" + numberFormat.format(ohlc.getHigh()) + //$NON-NLS-1$
        "\r\nL:" + numberFormat.format(ohlc.getLow()) + //$NON-NLS-1$
        "\r\nC:" + numberFormat.format(ohlc.getHigh()); //$NON-NLS-1$
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

        IOHLC ohlc = (IOHLC) (values.length > 0 ? values[values.length - 1].getAdapter(IOHLC.class) : null);
        IOHLC previousOhlc = (IOHLC) (values.length > 1 ? values[values.length - 2].getAdapter(IOHLC.class) : null);

        dateItem = new SummaryDateItem(parent, SWT.DATE);
        dateItem.setDate(ohlc != null ? ohlc.getDate() : null);

        ohlcItem = new SummaryOHLCItem(parent, SWT.NONE);
        ohlcItem.setOHLC(ohlc, previousOhlc);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ISummaryBarDecorator#updateDecorator(int, int)
     */
    @Override
    public void updateDecorator(int x, int y) {
        if (pointArray != null) {
            IOHLC ohlc = null;
            IOHLC previousOhlc = null;
            if (y == SWT.DEFAULT) {
                for (int i = 0; i < pointArray.length; i++) {
                    if (x >= pointArray[i].x - width / 2 && x <= pointArray[i].x + width / 2) {
                        ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                        if (i > 0) {
                            previousOhlc = (IOHLC) values[i - 1].getAdapter(IOHLC.class);
                        }
                    }
                }
            }
            else {
                for (int i = 1; i < pointArray.length; i++) {
                    if (PixelTools.isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y)) {
                        ohlc = (IOHLC) values[i - 1].getAdapter(IOHLC.class);
                        if (i > 1) {
                            previousOhlc = (IOHLC) values[i - 2].getAdapter(IOHLC.class);
                        }
                    }
                }
            }
            if (ohlc != null) {
                dateItem.setDate(ohlc.getDate());
                ohlcItem.setOHLC(ohlc, previousOhlc);
            }
        }
    }
}
