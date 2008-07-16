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

package org.eclipsetrader.opentick.internal.core.repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "instrument")
@XmlType(name = "org.eclipsetrader.opentick.Instrument")
public class Instrument {
	@XmlAttribute(name = "code")
    private String code;

	@XmlValue
    private String company;

	@XmlAttribute(name = "currency")
    private String currency;

	@XmlAttribute(name = "type")
    private int type;

	public Instrument() {
	}

	public Instrument(String code, String company, String currency, int type) {
	    this.code = code;
	    this.company = company;
	    this.currency = currency;
	    this.type = type;
    }

	@XmlTransient
	public String getCode() {
    	return code;
    }

	@XmlTransient
	public String getCompany() {
    	return company;
    }

	@XmlTransient
	public String getCurrency() {
    	return currency;
    }

	@XmlTransient
	public int getType() {
    	return type;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	if ("".equals(company))
    		return code;
	    return company + " (" + code +")";
    }
}
