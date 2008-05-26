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
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplate;

/**
 * Charts UI model root.
 *
 * @since 1.0
 */
public class ChartDocument implements IAdaptable {
	private String name;
	private List<ChartRowSection> sections = new ArrayList<ChartRowSection>();

	private IDataSeries root;

	public ChartDocument(String name, IDataSeries root) {
		this.name = name;
		this.root = root;
	}

	public ChartDocument(String name, IHistory history) {
		this(name, new OHLCDataSeries(history.getSecurity() != null ? history.getSecurity().getName() : "MAIN", history.getOHLC()));
	}

	public ChartDocument(IChartTemplate template, IHistory history) {
		this.name = template.getName();
		this.root = new OHLCDataSeries(history.getSecurity() != null ? history.getSecurity().getName() : "MAIN", history.getOHLC());

		for (IChartSection templateSection : template.getSections()) {
    		ChartRowSection row = new ChartRowSection(this.root, templateSection);
        	if (sections.size() == 0)
        		row.addIndicator(null, null, null, new SecurityElement(root));
        	sections.add(row);
		}

		if (sections.size() == 0) {
    		ChartRowSection row = new ChartRowSection(UUID.randomUUID().toString(), "MAIN");
    		row.getElements().add(new SecurityElement(this.root));
    		sections.add(row);
    	}
	}

	/**
	 * Returns the chart's name.
	 *
	 * @return the name
	 */
	public String getName() {
    	return name;
    }

	/**
	 * Sets the chart name.
	 *
	 * @param name the new name to set.
	 */
	public void setName(String name) {
    	this.name = name;
    }

	/**
	 * Creates a new section at the end of the chart.
	 *
	 * @param name the name of the new section.
	 * @return the new section object.
	 */
	public ChartRowSection createSection(String name) {
		ChartRowSection s = new ChartRowSection(UUID.randomUUID().toString(), name);
		sections.add(s);
		return s;
	}

	/**
	 * Returns a possibly empty array of the sections defined on the receiver.
	 *
	 * @return the sections array.
	 */
	public ChartRowSection[] getSections() {
    	return sections.toArray(new ChartRowSection[sections.size()]);
    }

	/**
	 * Returns the section with the given id.
	 *
	 * @param id the id to search.
	 * @return the section object, or <code>null</code> if no section with the given id is found.
	 */
	public ChartRowSection getSectionWithId(String id) {
		for (ChartRowSection row : sections) {
			if (row.getId().equals(id))
				return row;
		}
		return null;
	}

	/**
	 * Returns the section with the given name.
	 *
	 * @param name the name to search.
	 * @return the section object, or <code>null</code> if no section with the given name is found.
	 */
	public ChartRowSection getSectionWithName(String name) {
		for (ChartRowSection row : sections) {
			if (row.getName().equals(name))
				return row;
		}
		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(IDataSeries.class))
    		return root;
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

	/**
	 * Creates and returns a template object suitable for building the receiver.
	 *
	 * @return the template object.
	 */
    public ChartTemplate getTemplate() {
		IChartSection[] chartSections = new IChartSection[sections.size()];
		for (int i = 0; i < chartSections.length; i++)
			chartSections[i] = sections.get(i).getTemplate();

		ChartTemplate template = new ChartTemplate(name);
		template.setSections(chartSections);

		return template;
    }

	public IDataSeries getRoot() {
    	return root;
    }
}
