/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Widget;

public class RenderTarget {
	public Device display;

	public Widget widget;

	public GC gc;

	public int x;

	public int y;

	public int width;

	public int height;

	public IAxis verticalAxis;

	public IAxis horizontalAxis;

	public Object input;

	public Object firstValue;

	public Object lastValue;

	public IColorRegistry registry;

	public RenderTarget() {
	}
}
