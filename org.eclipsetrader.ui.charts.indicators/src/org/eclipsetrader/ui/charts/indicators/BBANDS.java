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
import org.eclipsetrader.ui.charts.IChartIndicator;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.eclipsetrader.ui.internal.charts.indicators.MultiLineElement;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class BBANDS implements IChartIndicator, IExecutableExtension {
    private String id;
    private String name;

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
     * @see org.eclipsetrader.ui.charts.model.IChartIndicator#computeElement(org.eclipse.core.runtime.IAdaptable, org.eclipsetrader.ui.charts.model.IChartParameters)
     */
    public IAdaptable computeElement(IAdaptable source, IChartParameters parameters) {
		IDataSeries dataSeries = (IDataSeries) source.getAdapter(IDataSeries.class);
		if (dataSeries != null) {
		    OHLCField field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
		    int period = parameters.getInteger("period");
			double upperDeviation = parameters.hasParameter("upper-deviation") ? parameters.getDouble("upper-deviation") : 2.0;
			double lowerDeviation = parameters.hasParameter("lower-deviation") ? parameters.getDouble("lower-deviation") : 2.0;
		    MAType maType = MAType.getFromName(parameters.getString("ma-type"));

		    RenderStyle upperLineStyle = parameters.hasParameter("upper-line-style") ? RenderStyle.getStyleFromName(parameters.getString("upper-line-style")) : RenderStyle.Line;
		    RGB upperLineColor = parameters.getColor("upper-line-color");
		    RenderStyle middleLineStyle = parameters.hasParameter("middle-line-style") ? RenderStyle.getStyleFromName(parameters.getString("middle-line-style")) : RenderStyle.Dot;
		    RGB middleLineColor = parameters.getColor("middle-line-color");
		    RenderStyle lowerLineStyle = parameters.hasParameter("lower-line-style") ? RenderStyle.getStyleFromName(parameters.getString("lower-line-style")) : RenderStyle.Line;
		    RGB lowerLineColor = parameters.getColor("lower-line-color");

			IAdaptable[] values = dataSeries.getValues();
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

			MultiLineElement result = new MultiLineElement();
			result.addElement(new NumericDataSeries(getName(), outUpper, dataSeries), upperLineStyle, upperLineColor);
			result.addElement(new NumericDataSeries(getName(), outMiddle, dataSeries), middleLineStyle, middleLineColor);
			result.addElement(new NumericDataSeries(getName(), outLower, dataSeries), lowerLineStyle, lowerLineColor);
			return result;
		}

		return null;
    }
}
