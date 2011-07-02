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

import java.util.ArrayList;
import java.util.List;

import org.eclipsetrader.core.charts.IDataSeries;

/**
 * Default implementation of on <code>IChartObject</code> that is composed
 * of other child objects.
 *
 * @since 1.0
 */
public class GroupChartObject implements IChartObject {

    private List<IChartObject> objects = new ArrayList<IChartObject>();

    public GroupChartObject() {
    }

    public void add(IChartObject object) {
        objects.add(object);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
     */
    @Override
    public boolean containsPoint(int x, int y) {
        for (IChartObject o : objects) {
            if (o.containsPoint(x, y)) {
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
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
     */
    @Override
    public String getToolTip() {
        for (IChartObject o : objects) {
            if (o.getToolTip() != null) {
                return o.getToolTip();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    @Override
    public String getToolTip(int x, int y) {
        for (IChartObject o : objects) {
            if (o.getToolTip(x, y) != null) {
                return o.getToolTip(x, y);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    @Override
    public void paint(IGraphics graphics) {
        for (IChartObject o : objects) {
            o.paint(graphics);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
     */
    @Override
    public void paintScale(Graphics graphics) {
        for (IChartObject o : objects) {
            o.paintScale(graphics);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
     */
    @Override
    public void invalidate() {
        for (IChartObject o : objects) {
            o.invalidate();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
     */
    @Override
    public void setDataBounds(DataBounds bounds) {
        for (IChartObject o : objects) {
            o.setDataBounds(bounds);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusGained(ChartObjectFocusEvent event) {
        for (IChartObject o : objects) {
            o.handleFocusGained(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
     */
    @Override
    public void handleFocusLost(ChartObjectFocusEvent event) {
        for (IChartObject o : objects) {
            o.handleFocusLost(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    @Override
    public void accept(IChartObjectVisitor visitor) {
        if (visitor.visit(this)) {
            for (IChartObject o : objects) {
                o.accept(visitor);
            }
        }
    }
}
