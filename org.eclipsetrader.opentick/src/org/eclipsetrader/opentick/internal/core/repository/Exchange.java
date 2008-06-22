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

@XmlRootElement(name = "exchange")
@XmlType(name = "org.eclipsetrader.opentick.Exchange")
public class Exchange {
	@XmlAttribute(name = "code")
    private String code;

	@XmlAttribute(name = "available")
    private boolean available;

	@XmlValue
    private String description;

	public Exchange() {
	}

	public Exchange(String code, boolean available, String description) {
	    this.code = code;
	    this.available = available;
	    this.description = description;
    }

	@XmlTransient
	public String getCode() {
    	return code;
    }

	@XmlTransient
	public boolean isAvailable() {
    	return available;
    }

	@XmlTransient
	public String getDescription() {
    	return description;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return description + " (" + code + ")";
    }
}
