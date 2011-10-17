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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipsetrader.core.charts.repository.IParameter;

@XmlRootElement(name = "param")
public class Parameter implements IParameter {

    @XmlAttribute(name = "value")
    private String value;

    @XmlAttribute(name = "name")
    private String name;

    protected Parameter() {
    }

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IParameter#getName()
     */
    @Override
    @XmlTransient
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IParameter#getValue()
     */
    @Override
    @XmlTransient
    public String getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IParameter#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
