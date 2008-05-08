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

package org.eclipsetrader.core.internal.charts.repository;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.charts.repository.IChartVisitor;
import org.eclipsetrader.core.charts.repository.IIndicatorSection;
import org.eclipsetrader.core.charts.repository.IParameter;

@XmlRootElement(name = "indicator")
public class IndicatorSection implements IIndicatorSection {
	@XmlAttribute(name = "id")
	private String id;

    @XmlElement(name = "param")
    @XmlJavaTypeAdapter(ParameterAdapter.class)
	private List<IParameter> parameters;

    public static class ParameterAdapter extends XmlAdapter<Parameter, IParameter> {

    	public ParameterAdapter() {
    	}

    	/* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        @Override
        public Parameter marshal(IParameter v) throws Exception {
    	    return v != null ? new Parameter(v.getName(), v.getValue()) : null;
        }

    	/* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public IParameter unmarshal(Parameter v) throws Exception {
    	    return v;
        }
    }

	protected IndicatorSection() {
	}

	protected IndicatorSection(IIndicatorSection section) {
		this.id = section.getId();
    	this.parameters = section.getParameters() != null ? Arrays.asList(section.getParameters()) : null;
	}

	public IndicatorSection(String id) {
	    this.id = id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IIndicatorSection#getId()
     */
	@XmlTransient
	public String getId() {
    	return id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IIndicatorSection#getParameters()
     */
	@XmlTransient
    public IParameter[] getParameters() {
    	return parameters != null ? parameters.toArray(new IParameter[parameters.size()]) : new IParameter[0];
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IIndicatorSection#setParameters(org.eclipsetrader.core.charts.repository.IParameter[])
     */
    public void setParameters(IParameter[] parameters) {
    	this.parameters = parameters != null ? Arrays.asList(parameters) : null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IIndicatorSection#accept(org.eclipsetrader.core.charts.repository.IChartVisitor)
     */
	public void accept(IChartVisitor visitor) {
		visitor.visit(this);
	}
}
