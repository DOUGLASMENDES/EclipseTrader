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

package org.eclipsetrader.ui.charts.indicators;

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.NumberValue;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.Graphics;
import org.eclipsetrader.ui.charts.GroupChartObject;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.IGraphics;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.PixelTools;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.Util;

public class TREND implements IChartObjectFactory, IExecutableExtension {

    private String id;
    private String factoryName;
    private String name;

    private int period = 30;

    private RenderStyle upperLineStyle = RenderStyle.Line;
    private RGB upperLineColor;
    private RenderStyle middleLineStyle = RenderStyle.Dot;
    private RGB middleLineColor;
    private RenderStyle lowerLineStyle = RenderStyle.Line;
    private RGB lowerLineColor;

    private class LineToolObject implements IChartObject {
    
        private RenderStyle style;
        private RGB color;
    
        private Date d1;
        private Date d2;
        private Double v1;
        private Double v2;
    
        private Point p1;
        private Point p2;
        private boolean hasFocus;
    
        public LineToolObject(RenderStyle style, RGB color, Date d1, Double v1, Date d2, Double v2) {
            this.style = style;
            this.color = color;
            this.d1 = d1;
            this.v1 = v1;
            this.d2 = d2;
            this.v2 = v2;
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
         */
        @Override
        public void setDataBounds(DataBounds bounds) {
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
         */
        @Override
        public IDataSeries getDataSeries() {
            return new DataSeries(name, new IAdaptable[] {
                new NumberValue(d1, v1), new NumberValue(d2, v2)
            });
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
         */
        @Override
        public boolean containsPoint(int x, int y) {
            if (p1 != null && p2 != null) {
                return PixelTools.isPointOnLine(x, y, p1.x, p1.y, p2.x, p2.y);
            }
            return false;
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
         */
        @Override
        public String getToolTip() {
            return name;
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
            hasFocus = true;
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
         */
        @Override
        public void handleFocusLost(ChartObjectFocusEvent event) {
            hasFocus = false;
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
            p1 = new Point(graphics.mapToHorizontalAxis(d1), graphics.mapToVerticalAxis(v1));
            p2 = new Point(graphics.mapToHorizontalAxis(d2), graphics.mapToVerticalAxis(v2));
    
            graphics.pushState();
    
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
    
            graphics.setForegroundColor(color);
            graphics.setLineWidth(hasFocus ? 2 : 0);
            graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
    
            graphics.popState();
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
         */
        @Override
        public void paintScale(Graphics graphics) {
        }
    
        /* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
         */
        @Override
        public void accept(IChartObjectVisitor visitor) {
        }
    }

    public TREND() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        factoryName = config.getAttribute("name");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public RenderStyle getUpperLineStyle() {
        return upperLineStyle;
    }

    public void setUpperLineStyle(RenderStyle upperLineStyle) {
        this.upperLineStyle = upperLineStyle;
    }

    public RGB getUpperLineColor() {
        return upperLineColor;
    }

    public void setUpperLineColor(RGB upperLineColor) {
        this.upperLineColor = upperLineColor;
    }

    public RenderStyle getMiddleLineStyle() {
        return middleLineStyle;
    }

    public void setMiddleLineStyle(RenderStyle middleLineStyle) {
        this.middleLineStyle = middleLineStyle;
    }

    public RGB getMiddleLineColor() {
        return middleLineColor;
    }

    public void setMiddleLineColor(RGB middleLineColor) {
        this.middleLineColor = middleLineColor;
    }

    public RenderStyle getLowerLineStyle() {
        return lowerLineStyle;
    }

    public void setLowerLineStyle(RenderStyle lowerLineStyle) {
        this.lowerLineStyle = lowerLineStyle;
    }

    public RGB getLowerLineColor() {
        return lowerLineColor;
    }

    public void setLowerLineColor(RGB lowerLineColor) {
        this.lowerLineColor = lowerLineColor;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public IChartObject createObject(IDataSeries source) {
        if (source == null) {
            return null;
        }

        IAdaptable[] values = source.getValues();
        if (values.length < period) {
            return null;
        }

        double[] inReal = Util.getValuesForField(values, OHLCField.Close);

        int numberPlotPoints = 0;
        double sumxx = 0;
        double sumxy = 0;
        double sumx = 0;
        double sumy = 0;
        for (int i = inReal.length - period; i < inReal.length; i++, numberPlotPoints++) {
            double x = numberPlotPoints;
            double y = inReal[i];
            sumx += x;
            sumy += y;
            sumxx += x * x;
            sumxy += x * y;
        }
        double n = numberPlotPoints;
        double Sxx = sumxx - sumx * sumx / n;
        double Sxy = sumxy - sumx * sumy / n;
        double b = Sxy / Sxx;
        double a = (sumy - b * sumx) / n;

        double average = 0;
        numberPlotPoints = 0;
        for (int i = inReal.length - period; i < inReal.length; i++, numberPlotPoints++) {
            average += Math.pow(inReal[i] - (a + numberPlotPoints * b), 2);
        }
        average /= numberPlotPoints;
        average /= Math.sqrt(average);

        Date d1 = (Date) values[values.length - period].getAdapter(Date.class);
        Date d2 = (Date) values[values.length - 1].getAdapter(Date.class);

        GroupChartObject object = new GroupChartObject();
        object.add(new LineToolObject(middleLineStyle, middleLineColor, d1, a, d2, a + period * b));
        object.add(new LineToolObject(upperLineStyle, upperLineColor, d1, a + average, d2, a + average + period * b));
        object.add(new LineToolObject(lowerLineStyle, lowerLineColor, d1, a - average, d2, a - average + period * b));
        return object;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    @Override
    public IChartParameters getParameters() {
        ChartParameters parameters = new ChartParameters();

        if (!factoryName.equals(name)) {
            parameters.setParameter("name", name);
        }

        parameters.setParameter("period", period);

        parameters.setParameter("upper-line-style", upperLineStyle.getName());
        if (upperLineColor != null) {
            parameters.setParameter("upper-line-color", upperLineColor);
        }
        parameters.setParameter("middle-line-style", middleLineStyle.getName());
        if (middleLineColor != null) {
            parameters.setParameter("middle-line-color", middleLineColor);
        }
        parameters.setParameter("lower-line-style", lowerLineStyle.getName());
        if (lowerLineColor != null) {
            parameters.setParameter("lower-line-color", lowerLineColor);
        }

        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    @Override
    public void setParameters(IChartParameters parameters) {
        name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

        period = parameters.getInteger("period");

        upperLineStyle = parameters.hasParameter("upper-line-style") ? RenderStyle.getStyleFromName(parameters.getString("upper-line-style")) : RenderStyle.Line;
        upperLineColor = parameters.getColor("upper-line-color");
        middleLineStyle = parameters.hasParameter("middle-line-style") ? RenderStyle.getStyleFromName(parameters.getString("middle-line-style")) : RenderStyle.Dot;
        middleLineColor = parameters.getColor("middle-line-color");
        lowerLineStyle = parameters.hasParameter("lower-line-style") ? RenderStyle.getStyleFromName(parameters.getString("lower-line-style")) : RenderStyle.Line;
        lowerLineColor = parameters.getColor("lower-line-color");
    }
}
