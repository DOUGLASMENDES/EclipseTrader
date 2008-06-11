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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipsetrader.core.views.IView;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;
import org.eclipsetrader.core.views.IViewVisitor;
import org.eclipsetrader.ui.charts.ChartViewItem;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;

public class ChartViewItemFinder implements IViewVisitor, IViewItemVisitor {
	private IChartObject target;
	private ChartViewItem viewItem;

	public ChartViewItemFinder() {
	}

	public ChartViewItemFinder(IChartObject target) {
	    this.target = target;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewVisitor#visit(org.eclipsetrader.core.views.IView)
     */
    public boolean visit(IView view) {
	    return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItemVisitor#visit(org.eclipsetrader.core.views.IViewItem)
	 */
	public boolean visit(final IViewItem viewItem) {
		if (!(viewItem instanceof ChartViewItem))
			return true;
		IChartObject object = (IChartObject) viewItem.getAdapter(IChartObject.class);
		object.accept(new IChartObjectVisitor() {
            public boolean visit(IChartObject object) {
        		if (object == target)
        			ChartViewItemFinder.this.viewItem = (ChartViewItem) viewItem;
	            return true;
            }
		});
		return true;
	}

	public ChartViewItem getViewItem() {
    	return viewItem;
    }
}
