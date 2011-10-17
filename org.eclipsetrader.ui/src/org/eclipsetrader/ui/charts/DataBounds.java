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

package org.eclipsetrader.ui.charts;

import java.util.Date;

import org.eclipse.swt.graphics.Rectangle;

public class DataBounds {

    public Date first;
    public Date last;
    public Date[] dates;

    public Double highest;
    public Double lowest;

    public int x;
    public int y;
    public int width;
    public int height;

    public int horizontalSpacing;

    public DataBounds(Date[] dates, Double highest, Double lowest, Rectangle bounds, int horizontalSpacing) {
        this.first = dates.length != 0 ? dates[0] : null;
        this.last = dates.length != 0 ? dates[dates.length - 1] : null;
        this.dates = dates;
        this.highest = highest;
        this.lowest = lowest;

        this.x = bounds.x;
        this.y = bounds.y;
        this.width = bounds.width;
        this.height = bounds.height;

        this.horizontalSpacing = horizontalSpacing;
    }
}
