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

package org.eclipsetrader.repository.local.internal.types;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "property")
public class PropertyType {
	@XmlAttribute(name = "name")
	private String name;

	@XmlValue
	private String value;

	public PropertyType() {
	}

	public PropertyType(String name, String value) {
	    this.name = name;
	    this.value = value;
    }

	@XmlTransient
	public String getName() {
    	return name;
    }

	@XmlTransient
	public String getValue() {
    	return value;
    }
}
