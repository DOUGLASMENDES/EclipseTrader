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

package org.eclipsetrader.repository.local.internal.types;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.Dividend;
import org.eclipsetrader.core.feed.IDividend;

@XmlRootElement(name = "dividend")
public class DividendType implements Comparable<Object> {

    @XmlAttribute(name = "ex-date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date exDate;

    @XmlAttribute(name = "value")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double value;

    public DividendType() {
    }

    public DividendType(IDividend dividend) {
        this.exDate = dividend.getExDate();
        this.value = dividend.getValue();
    }

    public IDividend getDividend() {
        return new Dividend(exDate, value);
    }

    public Date getExDate() {
        return exDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof DividendType) {
            return exDate.compareTo(((DividendType) o).getExDate());
        }
        if (o instanceof SplitType) {
            return exDate.compareTo(((SplitType) o).getDate());
        }
        return 0;
    }
}
