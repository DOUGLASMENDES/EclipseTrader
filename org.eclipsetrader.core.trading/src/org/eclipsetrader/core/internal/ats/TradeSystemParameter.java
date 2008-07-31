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

package org.eclipsetrader.core.internal.ats;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.eclipsetrader.core.ats.ITradeSystemParameter;

@XmlRootElement(name = "param")
public class TradeSystemParameter implements ITradeSystemParameter {
	@XmlAttribute(name = "id")
	private String name;

	@XmlValue
	private String value;

	protected TradeSystemParameter() {
	}

	public TradeSystemParameter(String name, String value) {
	    this.name = name;
	    this.value = value;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IParameter#getName()
     */
	@XmlTransient
    public String getName() {
    	return name;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.IParameter#getValue()
     */
	@XmlTransient
    public String getValue() {
    	return value;
    }
}
