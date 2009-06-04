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

package org.eclipsetrader.ui.charts.patterns;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.GroupChartObject;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.ILineDecorator;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.PatternBox;
import org.eclipsetrader.ui.internal.charts.indicators.IGeneralPropertiesAdapter;

public class DoubleReversal implements IChartObjectFactory, IGeneralPropertiesAdapter, ILineDecorator, IExecutableExtension {
	private String id;
	private String factoryName;
	private String name;

	private RenderStyle renderStyle = RenderStyle.Line;
	private RGB color;

	public DoubleReversal() {
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
		if (values.length < 6)
			return null;

		GroupChartObject object = new GroupChartObject();

		for (int i = values.length - 6; i >= 0; i--) {
			IOHLC[] outBars = new IOHLC[] {
			    (IOHLC) values[i].getAdapter(IOHLC.class),
			    (IOHLC) values[i + 1].getAdapter(IOHLC.class),
			    (IOHLC) values[i + 2].getAdapter(IOHLC.class),
			    (IOHLC) values[i + 3].getAdapter(IOHLC.class),
			    (IOHLC) values[i + 4].getAdapter(IOHLC.class),
			    (IOHLC) values[i + 5].getAdapter(IOHLC.class),
			};

			int sentiment = getSentiment(outBars);
			if (sentiment > 0) {
				object.add(new PatternBox(outBars, color, getName(), "Bullish"));
				i -= outBars.length;
			}
			else if (sentiment < 0) {
				object.add(new PatternBox(outBars, color, getName(), "Bearish"));
				i -= outBars.length;
			}
		}

		return object;
	}

	int getSentiment(IOHLC[] recs) {
		boolean reversals[] = {
		    false, false, false, false, false, false
		};
		int complete[] = {
		    0, 0
		};

		for (int n = 0; n < recs.length - 1; n++) {
			IOHLC rn = recs[n];
			IOHLC rn1 = recs[n + 1]; // save memory accesses
			double high[] = {
			    rn.getHigh(), rn1.getHigh()
			};
			double low[] = {
			    rn.getLow(), rn1.getLow()
			};
			double close[] = {
			    rn.getClose(), rn1.getClose()
			};

			// high-low reversal
			if (close[1] >= high[1] - 0.05 && close[0] <= low[0] + 0.05) {
				reversals[0] = true;
				if (n + 1 > complete[0])
					complete[0] = n + 1;
			}
			else if (close[0] >= high[0] - 0.05 && close[1] <= low[0] + 0.05) {
				reversals[3] = true;
				if (n + 1 > complete[1])
					complete[1] = n + 1;
			}

			// closing price reversal
			if (low[1] < low[0] // lower low
			        && close[1] > close[0]) //higher close
			{
				reversals[1] = true;
				if (n + 1 > complete[0])
					complete[0] = n + 1;
			}
			else if (high[1] >= high[0] // higher high
			        && close[1] <= close[0]) // lower close
			{
				reversals[4] = true;
				if (n + 1 > complete[1])
					complete[1] = n + 1;
			}

			// key reversal
			if (high[0] < high[1] && low[0] > low[1]) {
				if (close[1] >= high[0]) {
					reversals[2] = true;
					if (n + 1 > complete[0])
						complete[0] = n + 1;
				}
			}
			else if (high[0] < high[1] && low[0] > low[1]) {
				if (close[1] <= low[0]) {
					reversals[5] = true;
					if (n + 1 > complete[1])
						complete[1] = n + 1;
				}
			}
		}

		if ((reversals[0] && (reversals[1] || reversals[2])) || (reversals[1] && (reversals[0] || reversals[2])) || (reversals[2] && (reversals[0] || reversals[1])))
			return 1;
		else if ((reversals[3] && (reversals[4] || reversals[5])) || (reversals[4] && (reversals[3] || reversals[5])) || (reversals[5] && (reversals[3] || reversals[4])))
			return -1;

		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
	 */
	public IChartParameters getParameters() {
		ChartParameters parameters = new ChartParameters();

		if (!factoryName.equals(name))
			parameters.setParameter("name", name);

		parameters.setParameter("style", renderStyle.getName());
		if (color != null)
			parameters.setParameter("color", color);

		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
	 */
	public void setParameters(IChartParameters parameters) {
		name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

		renderStyle = parameters.hasParameter("style") ? RenderStyle.getStyleFromName(parameters.getString("style")) : RenderStyle.Line;
		color = parameters.getColor("color");
	}
}
