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

public class MACD implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String factoryName;
    private String name;

    private OHLCField field = OHLCField.Close;
    private int fastPeriod = 7;
    private MAType fastMaType = MAType.EMA;
    private int slowPeriod = 21;
    private MAType slowMaType = MAType.EMA;
    private int signalPeriod = 14;
    private MAType signalMaType = MAType.EMA;

    private RenderStyle macdLineStyle = RenderStyle.Line;
    private RGB macdLineColor;
    private RenderStyle signalLineStyle = RenderStyle.Dot;
    private RGB signalLineColor;
    private RenderStyle histLineStyle = RenderStyle.Invisible;
    private RGB histLineColor;

	public MACD() {
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

	public OHLCField getField() {
    	return field;
    }

	public void setField(OHLCField field) {
    	this.field = field;
    }

	public int getFastPeriod() {
    	return fastPeriod;
    }

	public void setFastPeriod(int fastPeriod) {
    	this.fastPeriod = fastPeriod;
    }

	public int getSlowPeriod() {
    	return slowPeriod;
    }

	public void setSlowPeriod(int slowPeriod) {
    	this.slowPeriod = slowPeriod;
    }

	public int getSignalPeriod() {
    	return signalPeriod;
    }

	public void setSignalPeriod(int signalPeriod) {
    	this.signalPeriod = signalPeriod;
    }

	public MAType getFastMaType() {
    	return fastMaType;
    }

	public void setFastMaType(MAType fastMaType) {
    	this.fastMaType = fastMaType;
    }

	public MAType getSlowMaType() {
    	return slowMaType;
    }

	public void setSlowMaType(MAType slowMaType) {
    	this.slowMaType = slowMaType;
    }

	public MAType getSignalMaType() {
    	return signalMaType;
    }

	public void setSignalMaType(MAType signalMaType) {
    	this.signalMaType = signalMaType;
    }

	public RenderStyle getMacdLineStyle() {
    	return macdLineStyle;
    }

	public void setMacdLineStyle(RenderStyle macdLineStyle) {
    	this.macdLineStyle = macdLineStyle;
    }

	public RGB getMacdLineColor() {
    	return macdLineColor;
    }

	public void setMacdLineColor(RGB macdLineColor) {
    	this.macdLineColor = macdLineColor;
    }

	public RenderStyle getSignalLineStyle() {
    	return signalLineStyle;
    }

	public void setSignalLineStyle(RenderStyle signalLineStyle) {
    	this.signalLineStyle = signalLineStyle;
    }

	public RGB getSignalLineColor() {
    	return signalLineColor;
    }

	public void setSignalLineColor(RGB signalLineColor) {
    	this.signalLineColor = signalLineColor;
    }

	public RenderStyle getHistLineStyle() {
    	return histLineStyle;
    }

	public void setHistLineStyle(RenderStyle histLineStyle) {
    	this.histLineStyle = histLineStyle;
    }

	public RGB getHistLineColor() {
    	return histLineColor;
    }

	public void setHistLineColor(RGB histLineColor) {
    	this.histLineColor = histLineColor;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
    	if (source == null)
    		return null;

		IAdaptable[] values = source.getValues();
		Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		int lookback = core.macdExtLookback(fastPeriod, MAType.getTALib_MAType(fastMaType), slowPeriod, MAType.getTALib_MAType(slowMaType), signalPeriod, MAType.getTALib_MAType(signalMaType));
		if (values.length < lookback)
			return null;

        int startIdx = 0;
        int endIdx = values.length - 1;
		double[] inReal = Util.getValuesForField(values, field);

		MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outMACD = new double[values.length - lookback];
        double[] outSignal = new double[values.length - lookback];
        double[] outMACDHist = new double[values.length - lookback];

        core.macdExt(startIdx, endIdx, inReal, fastPeriod, MAType.getTALib_MAType(fastMaType), slowPeriod, MAType.getTALib_MAType(slowMaType), signalPeriod, MAType.getTALib_MAType(signalMaType), outBegIdx, outNbElement, outMACD, outSignal, outMACDHist);

		GroupChartObject object = new GroupChartObject();
		object.add(createLineChartObject(new NumericDataSeries("MACD", outMACD, source), macdLineStyle, macdLineColor));
		object.add(createLineChartObject(new NumericDataSeries("MACD-S", outSignal, source), signalLineStyle, signalLineColor));
		object.add(createLineChartObject(new NumericDataSeries("MACD-H", outMACDHist, source), histLineStyle, histLineColor));
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
    public IChartParameters getParameters() {
    	ChartParameters parameters = new ChartParameters();

    	if (!factoryName.equals(name))
    		parameters.setParameter("name", name);

    	parameters.setParameter("field", field.getName());
    	parameters.setParameter("fast-period", fastPeriod);
    	parameters.setParameter("fast-ma-type", fastMaType.getName());
    	parameters.setParameter("slow-period", slowPeriod);
    	parameters.setParameter("slow-ma-type", slowMaType.getName());
    	parameters.setParameter("signal-period", signalPeriod);
    	parameters.setParameter("signal-ma-type", signalMaType.getName());

    	parameters.setParameter("macd-style", macdLineStyle.getName());
    	if (macdLineColor != null)
        	parameters.setParameter("macd-color", macdLineColor);
    	parameters.setParameter("signal-style", signalLineStyle.getName());
    	if (signalLineColor != null)
        	parameters.setParameter("signal-color", signalLineColor);
    	parameters.setParameter("hist-style", histLineStyle.getName());
    	if (histLineColor != null)
        	parameters.setParameter("hist-color", histLineColor);

    	return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

	    field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
	    fastPeriod = parameters.getInteger("fast-period");
	    fastMaType = MAType.getFromName(parameters.getString("fast-ma-type"));
	    slowPeriod = parameters.getInteger("slow-period");
	    slowMaType = MAType.getFromName(parameters.getString("slow-ma-type"));
	    signalPeriod = parameters.getInteger("signal-period");
	    signalMaType = MAType.getFromName(parameters.getString("signal-ma-type"));

	    macdLineStyle = parameters.hasParameter("macd-style") ? RenderStyle.getStyleFromName(parameters.getString("macd-style")) : RenderStyle.Line;
	    macdLineColor = parameters.getColor("macd-color");
	    signalLineStyle = parameters.hasParameter("signal-style") ? RenderStyle.getStyleFromName(parameters.getString("signal-style")) : RenderStyle.Dot;
	    signalLineColor = parameters.getColor("signal-color");
	    histLineStyle = parameters.hasParameter("hist-style") ? RenderStyle.getStyleFromName(parameters.getString("hist-style")) : RenderStyle.Invisible;
	    histLineColor = parameters.getColor("hist-color");
    }
}
