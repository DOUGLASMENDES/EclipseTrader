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

import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class ChartViewItem extends PlatformObject implements IViewItem, IWorkbenchAdapter {
	private ChartRowViewItem parent;
	private String id;
	private IChartObjectFactory factory;
	private IChartObject object;

	public ChartViewItem(ChartRowViewItem parent, IChartObjectFactory factory, String id) {
		this.parent = parent;
	    this.factory = factory;
	    this.id = id;
    }

	public ChartViewItem(ChartRowViewItem parent, IChartObjectFactory factory) {
		this.parent = parent;
	    this.factory = factory;
	    this.id = UUID.randomUUID().toString();
    }

	public String getId() {
    	return id;
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
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getValues()
	 */
	public IAdaptable[] getValues() {
		return null;
	}

	public IChartObjectFactory getFactory() {
    	return factory;
    }

	public IChartObject getObject() {
		if (object == null && factory != null)
			object = factory.createObject(getSourceDataSeries());
    	return object;
    }

	public void setObject(IChartObject object) {
    	this.object = object;
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

		if (adapter.isAssignableFrom(IChartObjectFactory.class))
			return factory;
		if (factory != null) {
			if (adapter.isAssignableFrom(factory.getClass()))
				return factory;
			if (factory instanceof IAdaptable) {
				Object o = ((IAdaptable) factory).getAdapter(adapter);
				if (o != null)
					return o;
			}
		}

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return super.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
	    return factory.getName();
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
	    return null;
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
			if (child != null) {
				for (int i = 0; i < child.length; i++)
					child[i].accept(visitor);
			}
		}
	}
}
