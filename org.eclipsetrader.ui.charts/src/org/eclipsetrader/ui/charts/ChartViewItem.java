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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class ChartViewItem extends PlatformObject implements IViewItem {
	private IChartObjectFactory factory;
	private IChartObject object;

	public ChartViewItem() {
	}

	public ChartViewItem(IChartObjectFactory factory) {
	    this.factory = factory;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
	 */
	public int getItemCount() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getItems()
	 */
	public IViewItem[] getItems() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getParent()
	 */
	public IViewItem getParent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getValues()
	 */
	public IAdaptable[] getValues() {
		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(IChartObject.class)) {
    		if (object == null && factory != null)
    			object = factory.createObject(getSourceDataSeries());
    		return object;
    	}
		if (object != null && adapter.isAssignableFrom(object.getClass()))
			return object;

		if (factory != null && adapter.isAssignableFrom(factory.getClass()))
			return factory;

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
    }

    protected IDataSeries getSourceDataSeries() {
    	IViewItem parentItem = getParent();
    	while (parentItem != null) {
    		IDataSeries dataSeries = (IDataSeries) parentItem.getAdapter(IDataSeries.class);
    		if (dataSeries != null)
    			return dataSeries;
        	parentItem = parentItem.getParent();
    	}
    	return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#accept(org.eclipsetrader.core.views.IViewItemVisitor)
	 */
	public void accept(IViewItemVisitor visitor) {
		if (visitor.visit(this)) {
			IViewItem[] child = getItems();
			for (int i = 0; i < child.length; i++)
				child[i].accept(visitor);
		}
	}
}
