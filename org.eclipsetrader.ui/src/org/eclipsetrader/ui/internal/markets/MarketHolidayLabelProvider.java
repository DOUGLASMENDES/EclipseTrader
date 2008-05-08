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

package org.eclipsetrader.ui.internal.markets;

import java.text.DateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.ui.Util;

public class MarketHolidayLabelProvider extends LabelProvider implements ITableLabelProvider {
	protected DateFormat dateFormat = Util.getDateFormat();

	public MarketHolidayLabelProvider() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object object, int columnIndex) {
    	MarketHolidayElement element = (MarketHolidayElement) object;
    	switch(columnIndex) {
    		case 0:
    			return dateFormat.format(element.getDate());
    		case 1:
    			return element.getDescription();
    	}
        return "";
    }
}
