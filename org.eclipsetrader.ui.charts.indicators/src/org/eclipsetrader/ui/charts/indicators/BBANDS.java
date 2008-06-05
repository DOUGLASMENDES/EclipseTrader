/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
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
import org.eclipsetrader.ui.charts.ChartObject;
import org.eclipsetrader.ui.charts.HistogramAreaChart;
import org.eclipsetrader.ui.charts.HistogramBarChart;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.LineChart;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class BBANDS implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String name;

    private OHLCField field = OHLCField.Close;
    private int period = 21;
    private double upperDeviation = 2.0;
    private double lowerDeviation = 2.0;
    private MAType maType = MAType.EMA;

    private RenderStyle upperLineStyle = RenderStyle.Line;
    private RGB upperLineColor;
    private RenderStyle middleLineStyle = RenderStyle.Dot;
    private RGB middleLineColor;
    private RenderStyle lowerLineStyle = RenderStyle.Line;
    private RGB lowerLineColor;

	public BBANDS() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
		if (source != null) {
			IAdaptable[] values = source.getValues();
			Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

	        int startIdx = 0;
	        int endIdx = values.length - 1;
			double[] inReal = Util.getValuesForField(values, field);

			MInteger outBegIdx = new MInteger();
	        MInteger outNbElement = new MInteger();
			int outputSize = values.length - core.bbandsLookback(period, upperDeviation, lowerDeviation, MAType.getTALib_MAType(maType));
			double[] outUpper = new double[outputSize];
			double[] outMiddle = new double[outputSize];
			double[] outLower = new double[outputSize];

			core.bbands(startIdx, endIdx, inReal, period, upperDeviation, lowerDeviation, MAType.getTALib_MAType(maType), outBegIdx, outNbElement, outUpper, outMiddle, outLower);

			ChartObject object = new ChartObject();
			object.add(createLineChartObject(new NumericDataSeries(getName(), outUpper, source), upperLineStyle, upperLineColor));
			object.add(createLineChartObject(new NumericDataSeries(getName(), outMiddle, source), middleLineStyle, middleLineColor));
			object.add(createLineChartObject(new NumericDataSeries(getName(), outLower, source), lowerLineStyle, lowerLineColor));
			return object;
		}
	    return null;
    }

    protected IChartObject createLineChartObject(IDataSeries result, RenderStyle renderStyle, RGB color) {
	    switch(renderStyle) {
	    	case Dash:
				return new LineChart(result, LineStyle.Dash, color);
	    	case Dot:
				return new LineChart(result, LineStyle.Dot, color);
	    	case HistogramBars:
				return new HistogramBarChart(result);
	    	case Histogram:
				return new HistogramAreaChart(result);
	    	case Invisible:
				return new LineChart(result, LineStyle.Invisible, color);
	    }

	    return new LineChart(result, LineStyle.Solid, color);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
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
