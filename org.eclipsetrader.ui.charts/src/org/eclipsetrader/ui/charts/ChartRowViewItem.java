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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IElementSection;
import org.eclipsetrader.core.charts.repository.IParameter;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartRowViewItem implements IViewItem {
	private ChartView parent;
	private String id;
	private String name;

	private IDataSeries rootDataSeries;
	private List<IChartObjectFactory> factories = new ArrayList<IChartObjectFactory>();
	private IChartObject rootChart = new ChartObject();

	public ChartRowViewItem(ChartView parent, String name) {
		this.parent = parent;
		this.name = name;
		this.id = UUID.randomUUID().toString();
	}

	public ChartRowViewItem(ChartView parent, IChartSection template) {
		this.parent = parent;
		this.name = template.getName();
		this.id = template.getId();

		IElementSection[] element = template.getElements();
		for (int i = 0; i < element.length; i++) {
			IChartObjectFactory factory = null;
			if (ChartsUIActivator.getDefault() != null)
				factory = ChartsUIActivator.getDefault().getChartObjectFactory(element[i].getPluginId());

			if (factory != null) {
				ChartParameters parameters = new ChartParameters();
				for (IParameter p : element[i].getParameters())
					parameters.setParameter(p.getName(), p.getValue());

				factory.setParameters(parameters);
				factories.add(factory);
			}
		}
	}

	public String getId() {
    	return id;
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public IDataSeries getRootDataSeries() {
    	return rootDataSeries;
    }

	public void setRootDataSeries(IDataSeries rootDataSeries) {
    	this.rootDataSeries = rootDataSeries;

    	rootChart = new ChartObject();
    	for (IChartObjectFactory factory : factories) {
    		IChartObject object = factory.createObject(rootDataSeries);
    		if (object != null)
    			rootChart.add(object);
    	}
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

	public ChartView getParentView() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getValues()
	 */
	public IAdaptable[] getValues() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IChartObject.class))
			return rootChart;

		if (adapter.isAssignableFrom(IDataSeries.class))
			return rootDataSeries;

		if (adapter.isAssignableFrom(parent.getClass()))
			return parent;

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
