/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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

public class LindhalBuy implements IChartObjectFactory, IGeneralPropertiesAdapter, ILineDecorator, IExecutableExtension {

    private String id;
    private String factoryName;
    private String name;

    private RenderStyle renderStyle = RenderStyle.Line;
    private RGB color;

    public LindhalBuy() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        id = config.getAttribute("id");
        factoryName = config.getAttribute("name");
        name = config.getAttribute("name");
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#getRenderStyle()
     */
    @Override
    public RenderStyle getRenderStyle() {
        return renderStyle;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.IGeneralPropertiesAdapter#setRenderStyle(org.eclipsetrader.ui.charts.RenderStyle)
     */
    @Override
    public void setRenderStyle(RenderStyle renderStyle) {
        this.renderStyle = renderStyle;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ILineDecorator#getColor()
     */
    @Override
    public RGB getColor() {
        return color;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.ILineDecorator#setColor(org.eclipse.swt.graphics.RGB)
     */
    @Override
    public void setColor(RGB color) {
        this.color = color;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public IChartObject createObject(IDataSeries source) {
        if (source == null) {
            return null;
        }

        IAdaptable[] values = source.getValues();
        if (values.length < 9) {
            return null;
        }

        GroupChartObject object = new GroupChartObject();

        for (int i = values.length - 9; i >= 0; i--) {
            IOHLC[] outBars = new IOHLC[] {
                    (IOHLC) values[i].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 1].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 2].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 3].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 4].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 5].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 6].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 7].getAdapter(IOHLC.class),
                    (IOHLC) values[i + 8].getAdapter(IOHLC.class),
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
        int a = 0, b = 1, d = 1, e = 1;

        // a must be the absolute low
        while (b < 9 && b < recs.length) {
            if (recs[a].getHigh() < recs[b].getHigh() && recs[b].getLow() > recs[a].getLow()) {
                break;
            }
            ++b;
        }

        // d must take out the low the preceeding bar
        d = b + 1; // start at b
        while (d < 9 && d < recs.length) {
            if (recs[d - 1].getLow() > recs[d].getLow() && recs[d].getLow() > recs[a].getLow() && recs[d - 1].getLow() > recs[a].getLow()) {
                break;
            }
            ++d;
        }

        // e must take out the high of the preceeding bar
        // and close above the previous bar's close
        // and close above its own open price

        e = d + 1;

        while (e < 9 && e < recs.length) {
            if (recs[e].getHigh() > recs[e - 1].getHigh() && recs[e].getClose() > recs[e - 1].getClose() && recs[e].getClose() > recs[e].getOpen() && recs[e].getLow() > recs[a].getLow() && recs[e - 1].getLow() > recs[a].getLow()) {
                return 1;
            }
            ++e;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    @Override
    public IChartParameters getParameters() {
        ChartParameters parameters = new ChartParameters();

        if (!factoryName.equals(name)) {
            parameters.setParameter("name", name);
        }

        parameters.setParameter("style", renderStyle.getName());
        if (color != null) {
            parameters.setParameter("color", color);
        }

        return parameters;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    @Override
    public void setParameters(IChartParameters parameters) {
        name = parameters.hasParameter("name") ? parameters.getString("name") : factoryName;

        renderStyle = parameters.hasParameter("style") ? RenderStyle.getStyleFromName(parameters.getString("style")) : RenderStyle.Line;
        color = parameters.getColor("color");
    }
}
