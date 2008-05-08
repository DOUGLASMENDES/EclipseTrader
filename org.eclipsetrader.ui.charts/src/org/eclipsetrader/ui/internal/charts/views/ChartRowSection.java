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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.DataSeries;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IIndicatorSection;
import org.eclipsetrader.core.charts.repository.IParameter;
import org.eclipsetrader.core.internal.charts.repository.ChartSection;
import org.eclipsetrader.ui.charts.AdaptableWrapper;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.IChartIndicator;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartRowSection implements IAdaptable {
	private String id;
	private String name;
	private List<ChartElement> elements = new ArrayList<ChartElement>();

	private IDataSeries root;
	private IDataSeries dataSeries;

	public ChartRowSection(String id, String name) {
	    this.id = id;
	    this.name = name;
		this.root = new DataSeries(name, new IAdaptable[0]);
		this.dataSeries = new DataSeries(name, new IAdaptable[0]);
    }

	public ChartRowSection(IDataSeries root, IChartSection section) {
		this.id = section.getId();
		this.name = section.getName();
		this.root = root;

		for (IIndicatorSection s : section.getIndicators())
			addIndicator(s);

		List<IDataSeries> l = new ArrayList<IDataSeries>();
		for (IAdaptable adaptableElement : elements) {
			IDataSeries data = (IDataSeries) adaptableElement.getAdapter(IDataSeries.class);
			if (data != null)
				l.add(data);
		}
		this.dataSeries = new DataSeries(section.getName(), new IAdaptable[0]);
		this.dataSeries.setChildren(l.toArray(new IDataSeries[l.size()]));
	}

	public List<IAdaptable> getElements() {
    	return new ArrayList<IAdaptable>(elements);
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public String getId() {
    	return id;
    }

	public void addIndicator(IIndicatorSection template) {
		IChartIndicator indicator = null;
		if (ChartsUIActivator.getDefault() != null)
			indicator = ChartsUIActivator.getDefault().getIndicator(template.getId());

		ChartParameters parameters = new ChartParameters();
		for (IParameter p : template.getParameters())
			parameters.setParameter(p.getName(), p.getValue());

		IAdaptable element = null;
		if (indicator != null)
			element = indicator.computeElement(new AdaptableWrapper(root), parameters);

		ChartElement chartElement = new ChartElement(template.getId(), parameters, indicator, element);
		elements.add(chartElement);
	}

	public void addIndicator(String id, ChartParameters parameters, IChartIndicator indicator, IAdaptable element) {
		addElement(new ChartElement(id, parameters, indicator, element));
	}

	public void addElement(ChartElement element) {
		elements.add(element);
		if (dataSeries != null) {
			List<IDataSeries> l = new ArrayList<IDataSeries>();
			for (IAdaptable adaptableElement : elements) {
				IDataSeries data = (IDataSeries) adaptableElement.getAdapter(IDataSeries.class);
				if (data != null)
					l.add(data);
			}
			dataSeries.setChildren(l.toArray(new IDataSeries[l.size()]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(IDataSeries.class))
    		return dataSeries;
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
		return null;
	}

	/**
	 * Creates and returns a template section object suitable for building the receiver.
	 *
	 * @return the template section object.
	 */
	public IChartSection getTemplate() {
		List<IIndicatorSection> indicators = new ArrayList<IIndicatorSection>();
		for (ChartElement element : elements) {
			if (element.getId() != null)
				indicators.add(element.getTemplate());
		}

		ChartSection section = new ChartSection(id, name);
		if (indicators.size() != 0)
			section.setIndicators(indicators.toArray(new IIndicatorSection[indicators.size()]));

		return section;
	}
}
