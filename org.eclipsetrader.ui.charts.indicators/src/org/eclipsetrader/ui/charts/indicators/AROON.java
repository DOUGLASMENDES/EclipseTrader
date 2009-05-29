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
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.GroupChartObject;
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

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class AROON implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String factoryName;
    private String name;

    private int period = 7;

    private RenderStyle upLineStyle = RenderStyle.Line;
    private RGB upLineColor;
    private RenderStyle downLineStyle = RenderStyle.Line;
    private RGB downLineColor;

	public AROON() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	factoryName = config.getAttribute("name");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
     */
    public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
	 */
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
    	return upLineStyle;
    }

	public void setUpperLineStyle(RenderStyle upperLineStyle) {
    	this.upLineStyle = upperLineStyle;
    }

	public RGB getUpperLineColor() {
    	return upLineColor;
    }

	public void setUpperLineColor(RGB upperLineColor) {
    	this.upLineColor = upperLineColor;
    }

	public RenderStyle getMiddleLineStyle() {
    	return downLineStyle;
    }

	public void setMiddleLineStyle(RenderStyle middleLineStyle) {
    	this.downLineStyle = middleLineStyle;
    }

	public RGB getMiddleLineColor() {
    	return downLineColor;
    }

	public void setMiddleLineColor(RGB middleLineColor) {
    	this.downLineColor = middleLineColor;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
    	if (source == null)
    		return null;

		IAdaptable[] values = source.getValues();
		Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		int lookback = core.aroonLookback(period);
		if (values.length < lookback)
			return null;

        int startIdx = 0;
        int endIdx = values.length - 1;
		double[] inHigh = Util.getValuesForField(values, OHLCField.High);
		double[] inLow = Util.getValuesForField(values, OHLCField.Low);

		MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
		double[] outUp = new double[values.length - lookback];
		double[] outDown = new double[values.length - lookback];

        core.aroon(startIdx, endIdx, inHigh, inLow, period, outBegIdx, outNbElement, outUp, outDown);

		GroupChartObject object = new GroupChartObject();
		object.add(createLineChartObject(new NumericDataSeries("AROON-U", outUp, source), upLineStyle, upLineColor));
		object.add(createLineChartObject(new NumericDataSeries("AROON-D", outDown, source), downLineStyle, downLineColor));
		return object;
    }

    protected IChartObject createLineChartObject(IDataSeries result, RenderStyle renderStyle, RGB color) {
		LineStyle lineStyle = LineStyle.Solid;
	    switch(renderStyle) {
	    	case Dash:
	    		lineStyle = LineStyle.Dash;
	    		break;
	    	case Dot:
	    		lineStyle = LineStyle.Dot;
	    		break;
	    	case HistogramBars:
				return new HistogramBarChart(result) {
		            @Override
		            protected boolean hasFocus() {
			            return ((GroupChartObject) getParent()).hasFocus();
		            }
				};
	    	case Histogram:
				return new HistogramAreaChart(result, color) {
		            @Override
		            protected boolean hasFocus() {
			            return ((GroupChartObject) getParent()).hasFocus();
		            }
				};
	    	case Invisible:
	    		lineStyle = LineStyle.Invisible;
	    		break;
	    }

	    return new LineChart(result, lineStyle, color) {
            @Override
            protected boolean hasFocus() {
	            return ((GroupChartObject) getParent()).hasFocus();
            }
	    };
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    public IChartParameters getParameters() {
    	ChartParameters parameters = new ChartParameters();

    	if (!factoryName.equals(name))
    		parameters.setParameter("name", name);

    	parameters.setParameter("period", period);

    	parameters.setParameter("up-line-style", upLineStyle.getName());
    	if (upLineColor != null)
        	parameters.setParameter("up-line-color", upLineColor);
    	parameters.setParameter("down-line-style", downLineStyle.getName());
    	if (downLineColor != null)
        	parameters.setParameter("down-line-color", downLineColor);

    	return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

	    period = parameters.getInteger("period");

	    upLineStyle = parameters.hasParameter("up-line-style") ? RenderStyle.getStyleFromName(parameters.getString("up-line-style")) : RenderStyle.Line;
	    upLineColor = parameters.getColor("up-line-color");
	    downLineStyle = parameters.hasParameter("down-line-style") ? RenderStyle.getStyleFromName(parameters.getString("down-line-style")) : RenderStyle.Line;
	    downLineColor = parameters.getColor("down-line-color");
    }
}
