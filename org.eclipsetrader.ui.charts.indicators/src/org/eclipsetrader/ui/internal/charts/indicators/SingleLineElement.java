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

package org.eclipsetrader.ui.internal.charts.indicators;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.IObjectRenderer;
import org.eclipsetrader.ui.charts.LineRenderers;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.charts.RenderTarget;

public class SingleLineElement implements IAdaptable, IObjectRenderer {
	private IDataSeries dataSeries;
	private RenderStyle style;
	private RGB color = new RGB(0, 0, 0);

	public SingleLineElement() {
	}

	public SingleLineElement(IDataSeries dataSeries, RenderStyle style, RGB color) {
	    this.dataSeries = dataSeries;
	    this.style = style;
	    this.color = color;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IObjectRenderer#renderObject(org.eclipsetrader.ui.charts.RenderTarget, org.eclipsetrader.core.charts.IDataSeries)
     */
    public void renderObject(RenderTarget target, IDataSeries dataSeries) {
    	Color color = target.registry.getColor(this.color);
    	switch(style) {
    		case Dot:
    			LineRenderers.renderDotLine(target, dataSeries.getValues(), color);
    			break;
    		case Dash:
    			LineRenderers.renderDashLine(target, dataSeries.getValues(), color);
    			break;
    		case HistogramBars:
    			LineRenderers.renderHistogramBars(target, dataSeries.getValues(), 3, color);
    			break;
    		default:
    			LineRenderers.renderLine(target, dataSeries.getValues(), color);
    			break;
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(IDataSeries.class))
    		return dataSeries;
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
		return null;
	}
}
