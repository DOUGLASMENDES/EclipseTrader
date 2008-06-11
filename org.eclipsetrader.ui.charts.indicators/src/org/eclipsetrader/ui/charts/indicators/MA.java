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
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.ILineDecorator;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

@SuppressWarnings("restriction")
public class MA implements IChartObjectFactory, IGeneralPropertiesAdapter, ILineDecorator, IExecutableExtension {
    private String id;
    private String name;

    private OHLCField field = OHLCField.Close;
    private int period = 7;
    private MAType type = MAType.EMA;

    private RenderStyle renderStyle = RenderStyle.Line;
    private RGB color;

	public MA() {
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

	public int getPeriod() {
    	return period;
    }

	public void setPeriod(int period) {
    	this.period = period;
    }

	public MAType getType() {
    	return type;
    }

	public void setType(MAType type) {
    	this.type = type;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#getRenderStyle()
     */
    public RenderStyle getRenderStyle() {
    	return renderStyle;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#setRenderStyle(org.eclipsetrader.ui.charts.RenderStyle)
     */
    public void setRenderStyle(RenderStyle renderStyle) {
    	this.renderStyle = renderStyle;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ILineDecorator#getColor()
     */
    public RGB getColor() {
    	return color;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ILineDecorator#setColor(org.eclipse.swt.graphics.RGB)
     */
    public void setColor(RGB color) {
    	this.color = color;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
    	if (source == null)
    		return null;

    	IAdaptable[] values = source.getValues();
		Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		int lookback = core.movingAverageLookback(period, MAType.getTALib_MAType(type));
		if (values.length < lookback)
			return null;

	    int startIdx = 0;
        int endIdx = values.length - 1;
		double[] inReal = Util.getValuesForField(values, field);

		MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outReal = new double[values.length - lookback];

        core.movingAverage(startIdx, endIdx, inReal, period, MAType.getTALib_MAType(type), outBegIdx, outNbElement, outReal);

		IDataSeries result = new NumericDataSeries(getName(), outReal, source);
		return Util.createLineChartObject(result, renderStyle, color);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
	    name = parameters.hasParameter("name") ? parameters.getString("name") : name;

	    field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
	    period = parameters.getInteger("period");
	    type = parameters.hasParameter("type") ? MAType.getFromName(parameters.getString("type")) : MAType.EMA;

	    renderStyle = parameters.hasParameter("style") ? RenderStyle.getStyleFromName(parameters.getString("style")) : RenderStyle.Line;
	    color = parameters.getColor("color");
    }
}
