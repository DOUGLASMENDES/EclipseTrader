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

public enum RenderStyle {
	Line("line", Messages.RenderStyle_LineText), //$NON-NLS-1$
	Dot("dot", Messages.RenderStyle_DotText), //$NON-NLS-1$
	Dash("dash", Messages.RenderStyle_DashText), //$NON-NLS-1$
	Histogram("histogram", Messages.RenderStyle_HistogramText), //$NON-NLS-1$
	HistogramBars("histogram-bars", Messages.RenderStyle_HistogramBarsText), //$NON-NLS-1$
	Invisible("invisible", Messages.RenderStyle_Invisible); //$NON-NLS-1$

	private String name;
	private String description;

	private RenderStyle(String name, String description) {
	    this.name = name;
	    this.description = description;
    }

	public static RenderStyle getStyleFromName(String name) {
		RenderStyle[] s = values();
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
