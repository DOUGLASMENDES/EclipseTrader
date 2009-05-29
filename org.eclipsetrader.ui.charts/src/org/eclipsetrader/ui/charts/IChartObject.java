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

package org.eclipsetrader.ui.charts;

import org.eclipsetrader.core.charts.IDataSeries;

/**
 * A lightweight chart object.
 * Charts are rendered to an <code>IGraphics</code> object.
 *
 * @since 1.0
 */
public interface IChartObject {

	/**
	 * Gets the data series associated with the receiver.
	 *
	 * @return the data series.
	 */
	public IDataSeries getDataSeries();

	public void setDataBounds(DataBounds bounds);

	/**
	 * Paints the received using the given graphics object.
	 *
	 * @param graphics the graphics object to paint on.
	 */
	public void paint(IGraphics graphics);

	/**
	 * Gets the tooltip string which describes the receiver's content.
	 *
	 * @return the tooltip text, or <code>null</code>
	 */
	public String getToolTip();

	/**
	 * Gets the tooltip string for the element at the given location.
	 * <p>A value of <code>SWT.DEFAULT</code> for either x or y means that that value
	 * should not be considered.</p>
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @return the tooltip text, or <code>null</code>.
	 */
	public String getToolTip(int x, int y);

	/**
	 * Returns true if the point (x, y) is contained within this object's bounds.
	 * <p>A value of <code>SWT.DEFAULT</code> for either x or y means that that value
	 * should not be considered.</p>
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @return <code>true</code> if the point (x, y) is contained in this IChartObject's bounds.
	 */
	public boolean containsPoint(int x, int y);

	/**
	 * Called when this object has gained focus.
	 *
	 * @param event the focus event.
	 */
	public void handleFocusGained(ChartObjectFocusEvent event);

	/**
	 * Called when this object has lost focus.
	 *
	 * @param event the focus event.
	 */
	public void handleFocusLost(ChartObjectFocusEvent event);

	/**
	 * Invalidate any cached content.
	 */
	public void invalidate();

	public void accept(IChartObjectVisitor visitor);
}
