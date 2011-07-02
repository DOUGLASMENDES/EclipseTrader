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

import org.eclipsetrader.core.feed.ISplit;
import org.eclipsetrader.core.feed.Split;

@XmlRootElement(name = "split")
public class SplitType implements Comparable<Object> {

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date date;

    @XmlAttribute(name = "old-quantity")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double oldQuantity;

    @XmlAttribute(name = "new-quantity")
    @XmlJavaTypeAdapter(DoubleValueAdapter.class)
    private Double newQuantity;

    public SplitType() {
    }

    public SplitType(ISplit split) {
        this.date = split.getDate();
        this.oldQuantity = split.getOldQuantity();
        this.newQuantity = split.getNewQuantity();
    }

    public Date getDate() {
        return date;
    }

    public ISplit getSplit() {
        return new Split(date, oldQuantity, newQuantity);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof DividendType) {
            return date.compareTo(((DividendType) o).getExDate());
        }
        if (o instanceof SplitType) {
            return date.compareTo(((SplitType) o).getDate());
        }
        return 0;
    }
}
