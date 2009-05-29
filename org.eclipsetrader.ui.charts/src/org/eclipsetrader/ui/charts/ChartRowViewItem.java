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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IElementSection;
import org.eclipsetrader.core.charts.repository.IParameter;
import org.eclipsetrader.core.internal.charts.repository.ChartSection;
import org.eclipsetrader.core.internal.charts.repository.ElementSection;
import org.eclipsetrader.core.internal.charts.repository.Parameter;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;
import org.eclipsetrader.core.views.ViewItemDelta;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartRowViewItem implements IViewItem {
	private ChartView parent;
	private String id;
	private String name;

	private IDataSeries rootDataSeries;
	private List<IChartObject> rootChart = new ArrayList<IChartObject>();

	private List<ChartViewItem> items = new ArrayList<ChartViewItem>();

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
				items.add(new ChartViewItem(this, factory, element[i].getId()));
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
    	refresh();
    }

	public void refresh() {
    	rootChart = new ArrayList<IChartObject>();
    	for (ChartViewItem value : items) {
    		IChartObject object = value.getFactory().createObject(rootDataSeries);
    		if (object != null)
    			rootChart.add(object);
    		value.setObject(object);
    	}
	}

	public void addChildItem(ChartViewItem viewItem) {
		items.add(viewItem);
		parent.fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.CHANGED, this),
				new ViewItemDelta(ViewItemDelta.ADDED, viewItem),
			});
	}

	public void removeChildItem(ChartViewItem viewItem) {
		items.remove(viewItem);
		parent.fireViewChangedEvent(new ViewItemDelta[] {
				new ViewItemDelta(ViewItemDelta.REMOVED, viewItem),
				new ViewItemDelta(ViewItemDelta.CHANGED, this),
			});
	}

	public void addFactory(IChartObjectFactory factory) {
		addChildItem(new ChartViewItem(this, factory));
	}

	public void removeFactory(IChartObjectFactory factory) {
		ChartViewItem removedItem = null;
		for (Iterator<ChartViewItem> iter = items.iterator(); iter.hasNext(); ) {
			ChartViewItem viewItem = iter.next();
			if (viewItem.getFactory() == factory) {
				iter.remove();
				removedItem = viewItem;
				break;
			}
		}

		if (removedItem != null) {
			parent.fireViewChangedEvent(new ViewItemDelta[] {
					new ViewItemDelta(ViewItemDelta.REMOVED, removedItem),
					new ViewItemDelta(ViewItemDelta.CHANGED, this),
				});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getItemCount()
	 */
	public int getItemCount() {
		return items.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IViewItem#getItems()
	 */
	public IViewItem[] getItems() {
		return items.toArray(new IViewItem[items.size()]);
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
		if (adapter.isAssignableFrom(IChartObject[].class))
			return rootChart.toArray(new IChartObject[rootChart.size()]);

		if (adapter.isAssignableFrom(IDataSeries.class))
			return rootDataSeries;

		if (adapter.isAssignableFrom(parent.getClass()))
			return parent;

		if (adapter.isAssignableFrom(getClass()))
			return this;

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

	public IChartSection getTemplate() {
		ChartSection template = new ChartSection(id, name);

		ElementSection[] element = new ElementSection[items.size()];
		for (int i = 0; i < element.length; i++) {
			IChartObjectFactory factory = items.get(i).getFactory();
			element[i] = new ElementSection(items.get(i).getId(), factory.getId());

			IChartParameters parameters = factory.getParameters();
			if (parameters != null) {
				String[] name = parameters.getParameterNames();

				IParameter[] elementParam = new IParameter[name.length];
				for (int ii = 0; ii < elementParam.length; ii++)
					elementParam[ii] = new Parameter(name[ii], parameters.getString(name[ii]));

				element[i].setParameters(elementParam);
			}
		}
		template.setElements(element);

		return template;
	}
}
