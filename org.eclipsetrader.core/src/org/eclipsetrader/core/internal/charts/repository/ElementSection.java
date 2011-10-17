/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.charts.repository.IChartVisitor;
import org.eclipsetrader.core.charts.repository.IElementSection;
import org.eclipsetrader.core.charts.repository.IParameter;

@XmlRootElement(name = "element")
public class ElementSection implements IElementSection {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "plugin-id")
    private String pluginId;

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

    protected ElementSection() {
    }

    protected ElementSection(IElementSection section) {
        this.id = section.getId();
        this.pluginId = section.getPluginId();
        this.parameters = section.getParameters() != null ? Arrays.asList(section.getParameters()) : null;
    }

    public ElementSection(String pluginId) {
        this.id = UUID.randomUUID().toString();
        this.pluginId = pluginId;
    }

    public ElementSection(String id, String pluginId) {
        this.id = id;
        this.pluginId = pluginId;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IElementSection#getId()
     */
    @Override
    @XmlTransient
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IElementSection#getPluginId()
     */
    @Override
    @XmlTransient
    public String getPluginId() {
        return pluginId;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IElementSection#getParameters()
     */
    @Override
    @XmlTransient
    public IParameter[] getParameters() {
        return parameters != null ? parameters.toArray(new IParameter[parameters.size()]) : new IParameter[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IElementSection#setParameters(org.eclipsetrader.core.charts.repository.IParameter[])
     */
    @Override
    public void setParameters(IParameter[] parameters) {
        this.parameters = parameters != null ? Arrays.asList(parameters) : null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IElementSection#accept(org.eclipsetrader.core.charts.repository.IChartVisitor)
     */
    @Override
    public void accept(IChartVisitor visitor) {
        visitor.visit(this);
    }
}
