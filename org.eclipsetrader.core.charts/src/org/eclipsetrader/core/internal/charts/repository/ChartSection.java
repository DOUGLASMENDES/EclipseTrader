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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.charts.repository.IChartSection;
import org.eclipsetrader.core.charts.repository.IChartVisitor;
import org.eclipsetrader.core.charts.repository.IIndicatorSection;
import org.eclipsetrader.core.charts.repository.ISecuritySection;

@XmlRootElement(name = "section")
public class ChartSection implements IChartSection {
	@XmlAttribute(name = "id")
	private String id;

	@XmlAttribute(name = "name")
	private String name;

	@XmlElement(name = "indicator")
	@XmlJavaTypeAdapter(IndicatorSectionAdapter.class)
	private IIndicatorSection[] indicators;

	@XmlElement(name = "security")
	@XmlJavaTypeAdapter(SecuritySectionAdapter.class)
	private ISecuritySection[] securities;

	public static class SecuritySectionAdapter extends XmlAdapter<SecuritySection, ISecuritySection> {

		public SecuritySectionAdapter() {
        }

		/* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        @Override
        public SecuritySection marshal(ISecuritySection v) throws Exception {
        	if (v instanceof SecuritySection)
        		return (SecuritySection) v;
	        return v != null ? new SecuritySection(v) : null;
        }

		/* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public ISecuritySection unmarshal(SecuritySection v) throws Exception {
	        return v;
        }
	}

	public ChartSection() {
	}

	public ChartSection(String id, String name) {
		this.id = id;
	    this.name = name;
    }

	public ChartSection(IChartSection section) {
	    this.id = section.getId();
	    this.name = section.getName();
    	this.indicators = section.getIndicators();
    	this.securities = section.getSecurities();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#getId()
     */
	@XmlTransient
	public String getId() {
    	return id;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#getName()
     */
	@XmlTransient
	public String getName() {
    	return name;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#setName(java.lang.String)
     */
	public void setName(String name) {
    	this.name = name;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#getIndicators()
     */
	@XmlTransient
	public IIndicatorSection[] getIndicators() {
    	return indicators != null ? indicators : new IIndicatorSection[0];
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#setIndicators(org.eclipsetrader.core.charts.repository.IIndicatorSection[])
     */
    public void setIndicators(IIndicatorSection[] indicators) {
    	this.indicators = indicators;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#getSecurities()
     */
	@XmlTransient
	public ISecuritySection[] getSecurities() {
    	return securities != null ? securities : new ISecuritySection[0];
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#setSecurities(org.eclipsetrader.core.charts.repository.ISecuritySection[])
     */
    public void setSecurities(ISecuritySection[] securities) {
    	this.securities = securities;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IChartSection#accept(org.eclipsetrader.core.charts.repository.IChartVisitor)
     */
	public void accept(IChartVisitor visitor) {
		if (visitor.visit(this)) {
			ISecuritySection[] s = getSecurities();
			for (int i = 0; i < s.length; i++)
				s[i].accept(visitor);

			IIndicatorSection[] d = getIndicators();
			for (int i = 0; i < d.length; i++)
				d[i].accept(visitor);
		}
	}
}
