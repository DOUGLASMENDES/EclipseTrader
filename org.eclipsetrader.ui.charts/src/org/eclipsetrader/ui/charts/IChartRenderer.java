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

package org.eclipsetrader.ui.charts;

public interface IChartRenderer {

    /**
     * Disposes of this chart renderer.
     */
	public void dispose();

	/**
	 * Renders the background graphics.
	 *
	 * @param graphics element to render.
	 */
	public void renderBackground(RenderTarget target);

	/**
	 * Render the graphics.
	 *
	 * @param graphics element to render.
	 */
	public void renderElement(RenderTarget target, Object element);
}
