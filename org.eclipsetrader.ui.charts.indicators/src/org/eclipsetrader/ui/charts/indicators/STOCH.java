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
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class STOCH implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String factoryName;
    private String name;

    private int kFastPeriod = 7;
    private int kSlowPeriod = 21;
    private MAType kMaType = MAType.EMA;
    private int dPeriod = 14;
    private MAType dMaType = MAType.EMA;

    private RenderStyle kLineStyle = RenderStyle.Line;
    private RGB kLineColor;
    private RenderStyle dLineStyle = RenderStyle.Dot;
    private RGB dLineColor;

	public STOCH() {
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
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#setName(java.lang.String)
     */
    public void setName(String name) {
    	this.name = name;
    }

	public int getKFastPeriod() {
    	return kFastPeriod;
    }

	public void setKFastPeriod(int fastPeriod) {
    	kFastPeriod = fastPeriod;
    }

	public int getKSlowPeriod() {
    	return kSlowPeriod;
    }

	public void setKSlowPeriod(int slowPeriod) {
    	kSlowPeriod = slowPeriod;
    }

	public MAType getKMaType() {
    	return kMaType;
    }

	public void setKMaType(MAType maType) {
    	kMaType = maType;
    }

	public int getDPeriod() {
    	return dPeriod;
    }

	public void setDPeriod(int period) {
    	dPeriod = period;
    }

	public MAType getDMaType() {
    	return dMaType;
    }

	public void setDMaType(MAType maType) {
    	dMaType = maType;
    }

	public RenderStyle getKLineStyle() {
    	return kLineStyle;
    }

	public void setKLineStyle(RenderStyle lineStyle) {
    	kLineStyle = lineStyle;
    }

	public RGB getKLineColor() {
    	return kLineColor;
    }

	public void setKLineColor(RGB lineColor) {
    	kLineColor = lineColor;
    }

	public RenderStyle getDLineStyle() {
    	return dLineStyle;
    }

	public void setDLineStyle(RenderStyle lineStyle) {
    	dLineStyle = lineStyle;
    }

	public RGB getDLineColor() {
    	return dLineColor;
    }

	public void setDLineColor(RGB lineColor) {
    	dLineColor = lineColor;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
    	if (source == null)
    		return null;

		IAdaptable[] values = source.getValues();
		Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		int lookback = core.stochLookback(kFastPeriod, kSlowPeriod, MAType.getTALib_MAType(kMaType), dPeriod, MAType.getTALib_MAType(dMaType));
		if (values.length < lookback)
			return null;

        int startIdx = 0;
        int endIdx = values.length - 1;
		double[] inHigh = Util.getValuesForField(values, OHLCField.High);
		double[] inLow = Util.getValuesForField(values, OHLCField.Low);
		double[] inClose = Util.getValuesForField(values, OHLCField.Close);

		MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outK = new double[values.length - lookback];
        double[] outD = new double[values.length - lookback];

        core.stoch(startIdx, endIdx, inHigh, inLow, inClose, kFastPeriod, kSlowPeriod, MAType.getTALib_MAType(kMaType), dPeriod, MAType.getTALib_MAType(dMaType), outBegIdx, outNbElement, outK, outD);

		GroupChartObject object = new GroupChartObject();
		object.add(createLineChartObject(new NumericDataSeries("STOCH-K", outK, source), kLineStyle, kLineColor));
		object.add(createLineChartObject(new NumericDataSeries("STOCH-D", outD, source), dLineStyle, dLineColor));
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

    	parameters.setParameter("k-fast-period", kFastPeriod);
    	parameters.setParameter("k-slow-period", kSlowPeriod);
    	parameters.setParameter("k-ma-type", kMaType.getName());
    	parameters.setParameter("d-period", dPeriod);
    	parameters.setParameter("d-ma-type", dMaType.getName());

    	parameters.setParameter("k-style", kLineStyle.getName());
    	if (kLineColor != null)
        	parameters.setParameter("k-color", kLineColor);
    	parameters.setParameter("d-style", dLineStyle.getName());
    	if (dLineColor != null)
        	parameters.setParameter("d-color", dLineColor);

    	return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

	    kFastPeriod = parameters.getInteger("k-fast-period");
	    kSlowPeriod = parameters.getInteger("k-slow-period");
	    kMaType = MAType.getFromName(parameters.getString("k-ma-type"));
	    dPeriod = parameters.getInteger("d-period");
	    dMaType = MAType.getFromName(parameters.getString("d-ma-type"));

	    kLineStyle = parameters.hasParameter("k-style") ? RenderStyle.getStyleFromName(parameters.getString("k-style")) : RenderStyle.Line;
	    kLineColor = parameters.getColor("k-color");
	    dLineStyle = parameters.hasParameter("d-style") ? RenderStyle.getStyleFromName(parameters.getString("d-style")) : RenderStyle.Dot;
	    dLineColor = parameters.getColor("d-color");
    }
}
