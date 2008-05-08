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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.instruments.ISecurity;

@XmlRootElement(name = "history")
public class HistoryType {

	@XmlAttribute(name = "security")
	@XmlJavaTypeAdapter(SecurityAdapter.class)
	private ISecurity security;

	@XmlElementWrapper(name = "data")
	@XmlElementRef
	@XmlJavaTypeAdapter(OHLCAdapter.class)
	List<IOHLC> data = new ArrayList<IOHLC>();

	public HistoryType() {
	}

	public HistoryType(ISecurity security, IOHLC[] data) {
		this.security = security;
		if (data != null)
			this.data = new ArrayList<IOHLC>(Arrays.asList(data));
	}

	@XmlTransient
	public List<IOHLC> getData() {
    	return data;
    }

	public IOHLC[] toArray() {
		return data.toArray(new IOHLC[data.size()]);
	}

	public ISecurity getSecurity() {
    	return security;
    }
}
