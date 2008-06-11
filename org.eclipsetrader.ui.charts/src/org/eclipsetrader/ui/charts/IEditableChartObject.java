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

import org.eclipsetrader.ui.charts.ChartToolEditor.ChartObjectEditorEvent;

public interface IEditableChartObject extends IChartObject {

	public boolean isOnEditHandle(int x, int y);

	public boolean isOnDragHandle(int x, int y);

	public void handleMouseDown(ChartObjectEditorEvent e);

	public void handleMouseUp(ChartObjectEditorEvent e);

	public void handleMouseMove(ChartObjectEditorEvent e);
}
