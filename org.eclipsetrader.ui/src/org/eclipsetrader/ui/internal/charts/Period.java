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

package org.eclipsetrader.ui.internal.charts;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.TimeSpan;

@XmlRootElement(name = "period")
public class Period {

    @XmlElement(name = "description")
    private String description;

    @XmlAttribute(name = "period")
    @XmlJavaTypeAdapter(TimeSpanAdapter.class)
    private TimeSpan period;

    @XmlAttribute(name = "resolution")
    @XmlJavaTypeAdapter(TimeSpanAdapter.class)
    private TimeSpan resolution;

    public static class TimeSpanAdapter extends XmlAdapter<String, TimeSpan> {

        public TimeSpanAdapter() {
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        @Override
        public String marshal(TimeSpan v) throws Exception {
            return v != null ? v.toString() : null;
        }

        /* (non-Javadoc)
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public TimeSpan unmarshal(String v) throws Exception {
            return v != null ? TimeSpan.fromString(v) : null;
        }
    }

    public Period() {
    }

    public Period(String description, TimeSpan period, TimeSpan barSize) {
        this.description = description;
        this.period = period;
        this.resolution = barSize;
    }

    @XmlTransient
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public TimeSpan getPeriod() {
        return period;
    }

    public void setPeriod(TimeSpan period) {
        this.period = period;
    }

    @XmlTransient
    public TimeSpan getResolution() {
        return resolution;
    }

    public void setResolution(TimeSpan resolution) {
        this.resolution = resolution;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 3 * description.hashCode() + 7 * period.hashCode() + 11 * resolution.hashCode();
    }

    public boolean equalsTo(TimeSpan period, TimeSpan resolution) {
        if (!this.period.equals(period)) {
            return false;
        }
        if (!this.resolution.equals(resolution)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Period)) {
            return false;
        }
        Period other = (Period) obj;
        if (!description.equals(other.description)) {
            return false;
        }
        if (!period.equals(other.period)) {
            return false;
        }
        if (!resolution.equals(other.resolution)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Period [" + period + ", resolution=" + resolution + "]";
    }
}
