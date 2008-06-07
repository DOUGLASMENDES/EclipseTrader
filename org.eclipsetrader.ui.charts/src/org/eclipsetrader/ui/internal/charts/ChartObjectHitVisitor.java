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

package org.eclipsetrader.ui.internal.charts;

import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;

/**
 * Visits an <code>IChartObject</code> tree to find the object
 * at a given screen location.
 *
 * @since 1.0
 */
public class ChartObjectHitVisitor implements IChartObjectVisitor {
	private int x;
	private int y;
	private IChartObject chartObject;

	public ChartObjectHitVisitor() {
    }

	public ChartObjectHitVisitor(int x, int y) {
	    this.x = x;
	    this.y = y;
    }

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
		chartObject = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectVisitor#visit(org.eclipsetrader.ui.charts.IChartObject)
	 */
	public boolean visit(IChartObject object) {
    	if (chartObject == null && object.containsPoint(x, y))
    		chartObject = object;
		return chartObject == null;
	}

	public IChartObject getChartObject() {
    	return chartObject;
    }
}
