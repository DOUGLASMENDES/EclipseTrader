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

package org.eclipsetrader.borsaitalia.internal.ui.wizards;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "instrument")
@XmlType(name = "org.eclipsetrader.borsaitalia.Instrument")
public class Instrument {

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "isin")
    private String isin;

    @XmlValue
    private String company;

    @XmlAttribute(name = "currency")
    private String currency;

    public Instrument() {
    }

    @XmlTransient
    public String getCode() {
        return code;
    }

    @XmlTransient
    public String getIsin() {
        return isin;
    }

    @XmlTransient
    public String getCompany() {
        return company;
    }

    @XmlTransient
    public String getCurrency() {
        return currency;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if ("".equals(company)) { //$NON-NLS-1$
            return code;
        }
        return company + " (" + code + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
