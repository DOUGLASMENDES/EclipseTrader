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

package org.eclipsetrader.ui.internal.charts.indicators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.IObjectRenderer;
import org.eclipsetrader.ui.charts.LineRenderers;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.charts.RenderTarget;

public class MultiLineElement implements IAdaptable, IObjectRenderer {

    private IDataSeries dataSeries;

    private class Line {

        public IDataSeries dataSeries;
        public RenderStyle style;
        public RGB color;

        public Line(IDataSeries dataSeries, RenderStyle style, RGB color) {
            this.dataSeries = dataSeries;
            this.style = style;
            this.color = color;
        }
    }

    private List<Line> lines = new ArrayList<Line>();

    public MultiLineElement() {
    }

    public void addElement(IDataSeries dataSeries, RenderStyle style, RGB color) {
        lines.add(new Line(dataSeries, style, color));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IObjectRenderer#renderObject(org.eclipsetrader.ui.charts.RenderTarget, org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public void renderObject(RenderTarget target, IDataSeries dataSeries) {
        for (Line l : lines) {
            Color color = target.registry.getColor(l.color);
            switch (l.style) {
                case Dot:
                    LineRenderers.renderDotLine(target, l.dataSeries.getValues(), color);
                    break;
                case Dash:
                    LineRenderers.renderDashLine(target, l.dataSeries.getValues(), color);
                    break;
                case Histogram:
                    LineRenderers.renderHistogram(target, l.dataSeries.getValues(), color, color);
                    break;
                case HistogramBars:
                    LineRenderers.renderHistogramBars(target, l.dataSeries.getValues(), 3, color);
                    break;
                default:
                    LineRenderers.renderLine(target, l.dataSeries.getValues(), color);
                    break;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IDataSeries.class)) {
            if (dataSeries == null) {
                dataSeries = new DataSeries("", new IAdaptable[0]);
                IDataSeries[] series = new IDataSeries[lines.size()];
                for (int i = 0; i < series.length; i++) {
                    series[i] = lines.get(i).dataSeries;
                }
                dataSeries.setChildren(series);
            }
            return dataSeries;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
