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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.IBarDecorator;

public class SecurityElement implements IAdaptable, IBarDecorator {

    private IDataSeries dataSeries;

    private RGB positiveBarColor = new RGB(0, 255, 0);
    private RGB negavitBarColor = new RGB(255, 0, 0);

    public SecurityElement(IDataSeries dataSeries) {
        this.dataSeries = dataSeries;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.model.IBarDecorator#getPositiveBarColor()
     */
    @Override
    public RGB getPositiveBarColor() {
        return positiveBarColor;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.internal.charts.model.IBarDecorator#getNegativeBarColor()
     */
    @Override
    public RGB getNegativeBarColor() {
        return negavitBarColor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IDataSeries.class)) {
            return dataSeries;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
