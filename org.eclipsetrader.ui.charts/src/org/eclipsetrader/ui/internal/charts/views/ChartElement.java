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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.repository.IElementSection;
import org.eclipsetrader.core.internal.charts.repository.ElementSection;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.IChartIndicator;

public class ChartElement implements IAdaptable {
	private String id;
	private ChartParameters parameters;

	private IChartIndicator indicator;
	private IAdaptable element;

	public ChartElement(String id, ChartParameters parameters, IChartIndicator indicator, IAdaptable element) {
        this.id = id;
        this.parameters = parameters;
        this.indicator = indicator;
        this.element = element;
	}

	public String getId() {
    	return id;
    }

	public ChartParameters getParameters() {
    	return parameters;
    }

	public void setParams(ChartParameters parameters) {
        this.parameters = parameters;
    }

	public IChartIndicator getIndicator() {
    	return indicator;
    }

	public void setElement(IAdaptable element) {
    	this.element = element;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
        return element != null ? element.getAdapter(adapter) : null;
	}

	/**
	 * Creates and returns a template object suitable for building the receiver.
	 *
	 * @return the template object.
	 */
	public IElementSection getTemplate() {
		IElementSection section = new ElementSection(id, indicator.getId());
		section.setParameters(parameters.toParametersArray());
		return section;
	}
}
