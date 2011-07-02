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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.NumericDataSeries;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.GroupChartObject;
import org.eclipsetrader.ui.charts.HistogramAreaChart;
import org.eclipsetrader.ui.charts.HistogramBarChart;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.LineChart;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class BBANDS implements IChartObjectFactory, IExecutableExtension {

    private String id;
    private String factoryName;
    private String name;

    private OHLCField field = OHLCField.Close;
    private int period = 21;
    private double upperDeviation = 2.0;
    private double lowerDeviation = 2.0;
    private MAType maType = MAType.EMA;

    private RenderStyle upperLineStyle = RenderStyle.Line;
    private RGB upperLineColor;
    private RenderStyle middleLineStyle = RenderStyle.Invisible;
    private RGB middleLineColor;
    private RenderStyle lowerLineStyle = RenderStyle.Line;
    private RGB lowerLineColor;

    public BBANDS() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        name = config.getAttribute("name");
        factoryName = config.getAttribute("name");
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

    public OHLCField getField() {
        return field;
    }

    public void setField(OHLCField field) {
        this.field = field;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public double getUpperDeviation() {
        return upperDeviation;
    }

    public void setUpperDeviation(double upperDeviation) {
        this.upperDeviation = upperDeviation;
    }

    public double getLowerDeviation() {
        return lowerDeviation;
    }

    public void setLowerDeviation(double lowerDeviation) {
        this.lowerDeviation = lowerDeviation;
    }

    public MAType getMaType() {
        return maType;
    }

    public void setMaType(MAType maType) {
        this.maType = maType;
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
        Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

        int lookback = core.bbandsLookback(period, upperDeviation, lowerDeviation, MAType.getTALib_MAType(maType));
        if (values.length < lookback) {
            return null;
        }

        int startIdx = 0;
        int endIdx = values.length - 1;
        double[] inReal = Util.getValuesForField(values, field);

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outUpper = new double[values.length - lookback];
        double[] outMiddle = new double[values.length - lookback];
        double[] outLower = new double[values.length - lookback];

        core.bbands(startIdx, endIdx, inReal, period, upperDeviation, lowerDeviation, MAType.getTALib_MAType(maType), outBegIdx, outNbElement, outUpper, outMiddle, outLower);

        GroupChartObject object = new GroupChartObject();
        object.add(createLineChartObject(new NumericDataSeries("BBU", outUpper, source), upperLineStyle, upperLineColor));
        object.add(createLineChartObject(new NumericDataSeries("BBM", outMiddle, source), middleLineStyle, middleLineColor));
        object.add(createLineChartObject(new NumericDataSeries("BBL", outLower, source), lowerLineStyle, lowerLineColor));
        return object;
    }

    protected IChartObject createLineChartObject(IDataSeries result, RenderStyle renderStyle, RGB color) {
        LineStyle lineStyle = LineStyle.Solid;
        switch (renderStyle) {
            case Dash:
                lineStyle = LineStyle.Dash;
                break;
            case Dot:
                lineStyle = LineStyle.Dot;
                break;
            case HistogramBars:
                return new HistogramBarChart(result);
            case Histogram:
                return new HistogramAreaChart(result, color);
            case Invisible:
                lineStyle = LineStyle.Invisible;
                break;
        }

        return new LineChart(result, lineStyle, color);
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

        parameters.setParameter("field", field.getName());
        parameters.setParameter("period", period);
        parameters.setParameter("upper-deviation", upperDeviation);
        parameters.setParameter("lower-deviation", lowerDeviation);
        parameters.setParameter("ma-type", maType.getName());

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

        field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
        period = parameters.getInteger("period");
        upperDeviation = parameters.hasParameter("upper-deviation") ? parameters.getDouble("upper-deviation") : 2.0;
        lowerDeviation = parameters.hasParameter("lower-deviation") ? parameters.getDouble("lower-deviation") : 2.0;
        maType = MAType.getFromName(parameters.getString("ma-type"));

        upperLineStyle = parameters.hasParameter("upper-line-style") ? RenderStyle.getStyleFromName(parameters.getString("upper-line-style")) : RenderStyle.Line;
        upperLineColor = parameters.getColor("upper-line-color");
        middleLineStyle = parameters.hasParameter("middle-line-style") ? RenderStyle.getStyleFromName(parameters.getString("middle-line-style")) : RenderStyle.Dot;
        middleLineColor = parameters.getColor("middle-line-color");
        lowerLineStyle = parameters.hasParameter("lower-line-style") ? RenderStyle.getStyleFromName(parameters.getString("lower-line-style")) : RenderStyle.Line;
        lowerLineColor = parameters.getColor("lower-line-color");
    }
}
