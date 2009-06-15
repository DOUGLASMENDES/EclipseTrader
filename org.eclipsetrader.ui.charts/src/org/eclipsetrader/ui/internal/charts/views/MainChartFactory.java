/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.ui.charts.BarChart;
import org.eclipsetrader.ui.charts.CandleStickChart;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.HistogramAreaChart;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.LineChart;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.LineChart.LineStyle;

public class MainChartFactory implements IChartObjectFactory {
	public static final String FACTORY_ID = "org.eclipsetrader.ui.charts.main";

	private MainRenderStyle style = MainRenderStyle.Candles;
	private OHLCField lineField = OHLCField.Close;

	private RGB lineColor;

	private RGB barPositiveColor;
	private RGB barNegativeColor;

	private RGB candlePositiveColor;
	private RGB candleNegativeColor;
	private RGB candleOutlineColor;

	public MainChartFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
	 */
	public String getId() {
		return FACTORY_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
	 */
	public String getName() {
		return "MAIN";
	}

	public OHLCField getLineField() {
		return lineField;
	}

	public void setLineField(OHLCField field) {
		this.lineField = field;
	}

	public MainRenderStyle getStyle() {
		return style;
	}

	public void setStyle(MainRenderStyle style) {
		this.style = style;
	}

	public RGB getLineColor() {
		return lineColor;
	}

	public void setLineColor(RGB lineColor) {
		this.lineColor = lineColor;
	}

	public RGB getBarPositiveColor() {
		return barPositiveColor;
	}

	public void setBarPositiveColor(RGB barPositiveColor) {
		this.barPositiveColor = barPositiveColor;
	}

	public RGB getBarNegativeColor() {
		return barNegativeColor;
	}

	public void setBarNegativeColor(RGB barNegativeColor) {
		this.barNegativeColor = barNegativeColor;
	}

	public RGB getCandlePositiveColor() {
		return candlePositiveColor;
	}

	public void setCandlePositiveColor(RGB candlePositiveColor) {
		this.candlePositiveColor = candlePositiveColor;
	}

	public RGB getCandleNegativeColor() {
		return candleNegativeColor;
	}

	public void setCandleNegativeColor(RGB candleNegativeColor) {
		this.candleNegativeColor = candleNegativeColor;
	}

	public RGB getCandleOutlineColor() {
		return candleOutlineColor;
	}

	public void setCandleOutlineColor(RGB candleBorderColor) {
		this.candleOutlineColor = candleBorderColor;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
	 */
	public IChartObject createObject(IDataSeries source) {
		if (source == null || !(source instanceof OHLCDataSeries))
			return null;

		if (style == MainRenderStyle.Bars)
			return new BarChart(source, barPositiveColor, barNegativeColor);
		if (style == MainRenderStyle.Candles)
			return new CandleStickChart(source, candleOutlineColor, candlePositiveColor, candleNegativeColor);
		if (style == MainRenderStyle.Histogram)
			return new HistogramAreaChart(source, lineColor);

		return new LineChart(source, LineStyle.Solid, lineColor);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
	 */
	public IChartParameters getParameters() {
		ChartParameters parameters = new ChartParameters();

		parameters.setParameter("style", style.getName());

		if (lineColor != null)
			parameters.setParameter("line-color", lineColor);

		if (barPositiveColor != null)
			parameters.setParameter("bar-positive-color", barPositiveColor);
		if (barNegativeColor != null)
			parameters.setParameter("bar-negative-color", barNegativeColor);

		if (candlePositiveColor != null)
			parameters.setParameter("candle-positive-color", candlePositiveColor);
		if (candleNegativeColor != null)
			parameters.setParameter("candle-negative-color", candleNegativeColor);
		if (candleOutlineColor != null)
			parameters.setParameter("candle-outline-color", candleOutlineColor);

		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
	 */
	public void setParameters(IChartParameters parameters) {
		style = parameters.hasParameter("style") ? MainRenderStyle.getStyleFromName(parameters.getString("style")) : MainRenderStyle.Bars;

		lineColor = parameters.getColor("line-color");

		barPositiveColor = parameters.getColor("bar-positive-color");
		barNegativeColor = parameters.getColor("bar-negative-color");

		candlePositiveColor = parameters.getColor("candle-positive-color");
		candleNegativeColor = parameters.getColor("candle-negative-color");
		candleOutlineColor = parameters.getColor("candle-outline-color");
	}
}
