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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.charts.repository.IChartVisitor;

@XmlRootElement(name = "chart")
public class ChartTemplate implements IChartTemplate {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "section")
    @XmlJavaTypeAdapter(ChartSectionAdapter.class)
    private List<IChartSection> sections;

    public static class ChartSectionAdapter extends XmlAdapter<ChartSection, IChartSection> {

        public ChartSectionAdapter() {
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        @Override
        public ChartSection marshal(IChartSection v) throws Exception {
            return v != null ? new ChartSection(v) : null;
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public IChartSection unmarshal(ChartSection v) throws Exception {
            return v;
        }
    }

    public ChartTemplate() {
    }

    public ChartTemplate(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#getName()
     */
    @Override
    @XmlTransient
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#getSections()
     */
    @Override
    @XmlTransient
    public IChartSection[] getSections() {
        return sections != null ? sections.toArray(new ChartSection[sections.size()]) : new ChartSection[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#setSections(org.eclipsetrader.core.charts.repository.IChartSection[])
     */
    @Override
    public void setSections(IChartSection[] sections) {
        this.sections = Arrays.asList(sections);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#getSectionWithName(java.lang.String)
     */
    @Override
    public IChartSection getSectionWithName(String name) {
        for (IChartSection s : sections) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#getSectionWithId(java.lang.String)
     */
    @Override
    public IChartSection getSectionWithId(String id) {
        for (IChartSection s : sections) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartTemplate#accept(org.eclipsetrader.core.charts.repository.IChartVisitor)
     */
    @Override
    public void accept(IChartVisitor visitor) {
        if (visitor.visit(this)) {
            IChartSection[] s = getSections();
            for (int i = 0; i < s.length; i++) {
                s[i].accept(visitor);
            }
        }
    }
}
