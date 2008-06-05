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
import org.eclipsetrader.ui.charts.HistogramAreaChart;
import org.eclipsetrader.ui.charts.HistogramBarChart;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.LineChart;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.eclipsetrader.ui.internal.charts.indicators.AdaptableWrapper;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class RSI implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String name;

    private OHLCField field = OHLCField.Close;
    private int period = 7;

    private RenderStyle style = RenderStyle.Line;
    private RGB color;

	public RSI() {
	}

	public RSI(int period, OHLCField field) {
	    this.period = period;
	    this.field = field;
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
	        double[] outReal = new double[values.length - core.rsiLookback(period)];

	        core.rsi(startIdx, endIdx, inReal, period, outBegIdx, outNbElement, outReal);

	        NumericDataSeries result = new NumericDataSeries(getName(), outReal, source);
	        result.setHighest(new AdaptableWrapper(100));
	        result.setLowest(new AdaptableWrapper(0));

	        switch(style) {
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
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
	    field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
	    period = parameters.getInteger("period");

	    style = parameters.hasParameter("style") ? RenderStyle.getStyleFromName(parameters.getString("style")) : RenderStyle.Line;
	    color = parameters.getColor("color");
    }
}
