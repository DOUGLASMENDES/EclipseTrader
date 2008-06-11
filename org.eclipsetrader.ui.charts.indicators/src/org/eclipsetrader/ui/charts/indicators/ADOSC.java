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
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class ADOSC implements IChartObjectFactory, IExecutableExtension {
    private String id;
    private String name;

    private int fastPeriod = 3;
    private int slowPeriod = 10;

    private RenderStyle renderStyle = RenderStyle.Line;
    private RGB color;

	public ADOSC() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
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

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    public IChartObject createObject(IDataSeries source) {
    	if (source == null)
    		return null;

		IAdaptable[] values = source.getValues();
		Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		int lookback = core.adOscLookback(fastPeriod, slowPeriod);
		if (values.length < lookback)
			return null;

        int startIdx = 0;
        int endIdx = values.length - 1;
		double[] inHigh = Util.getValuesForField(values, OHLCField.High);
		double[] inLow = Util.getValuesForField(values, OHLCField.Low);
		double[] inClose = Util.getValuesForField(values, OHLCField.Close);
		double[] inVolume = Util.getValuesForField(values, OHLCField.Volume);

		MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();
        double[] outReal = new double[values.length - lookback];

        core.adOsc(startIdx, endIdx, inHigh, inLow, inClose, inVolume, fastPeriod, slowPeriod, outBegIdx, outNbElement, outReal);

		IDataSeries result = new NumericDataSeries(getName(), outReal, source);
		return Util.createLineChartObject(result, renderStyle, color);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    public void setParameters(IChartParameters parameters) {
    }
}
