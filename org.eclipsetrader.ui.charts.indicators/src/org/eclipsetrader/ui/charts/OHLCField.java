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

public enum OHLCField {
	Open("open", "Open"),
	High("high", "High"),
	Low("low", "Low"),
	Close("close", "Close"),
	Volume("volume", "Volume");

	private String name;
	private String description;

	OHLCField(String name, String description) {
		this.name = name;
	    this.description = description;
	}

	public static OHLCField getFromName(String name) {
		OHLCField[] l = values();
		for (int i = 0; i < l.length; i++) {
			if (l[i].getName().equals(name))
				return l[i];
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
