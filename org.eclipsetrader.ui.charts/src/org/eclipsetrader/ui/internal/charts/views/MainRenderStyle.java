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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipsetrader.ui.charts.Messages;

public enum MainRenderStyle {
	Line("line", Messages.RenderStyle_LineText), //$NON-NLS-1$
	Bars("bars", Messages.RenderStyle_BarsText), //$NON-NLS-1$
	Candles("candles", Messages.RenderStyle_CandlesText), //$NON-NLS-1$ 
	Histogram("histogram", Messages.RenderStyle_HistogramText); //$NON-NLS-1$

	private String name;
	private String description;

	private MainRenderStyle(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public static MainRenderStyle getStyleFromName(String name) {
		MainRenderStyle[] s = values();
		for (int i = 0; i < s.length; i++) {
			if (s[i].getName().equals(name))
				return s[i];
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return description;
	}
}
