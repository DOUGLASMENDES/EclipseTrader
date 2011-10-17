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

package org.eclipsetrader.ui.internal.charts.views;

import java.text.NumberFormat;
import java.util.Observable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.Graphics;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IGraphics;

public class CurrentPriceLine extends Observable implements IChartObject, IAdaptable {

    private ITrade trade;

    private RGB foreground = Display.getDefault().getSystemColor(SWT.COLOR_INFO_FOREGROUND).getRGB();
    private RGB background = Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();

    private int width;
    private NumberFormat numberFormat = NumberFormat.getInstance();

    public CurrentPriceLine() {
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(4);
    }

    public void setTrade(ITrade trade) {
        this.trade = trade;
        setChanged();
        notifyObservers();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    @Override
    public void accept(IChartObjectVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
     */
    @Override
    public boolean containsPoint(int x, int y) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
     */
    @Override
    public IDataSeries getDataSeries() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
     */
    @Override
    public String getToolTip() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    @Override
    public String getToolTip(int x, int y) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusGained(ChartObjectFocusEvent event) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusLost(ChartObjectFocusEvent event) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
     */
    @Override
    public void invalidate() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    @Override
    public void paint(IGraphics graphics) {
        if (trade == null || trade.getPrice() == null) {
            return;
        }

        graphics.setForegroundColor(foreground);
        graphics.setBackgroundColor(background);

        int y = graphics.mapToVerticalAxis(trade.getPrice());
        graphics.drawLine(0, y, width, y);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
     */
    @Override
    public void paintScale(Graphics graphics) {
        if (trade == null || trade.getPrice() == null) {
            return;
        }

        graphics.setForegroundColor(foreground);
        graphics.setBackgroundColor(background);

        String text = numberFormat.format(trade.getPrice());
        Point extents = graphics.stringExtent(text);

        int y = graphics.mapToVerticalAxis(trade.getPrice()) - extents.y / 2;

        graphics.fillRectangle(0, y, extents.x, extents.y);
        graphics.drawString(text, 0, y);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
     */
    @Override
    public void setDataBounds(DataBounds bounds) {
        this.width = bounds.width;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
