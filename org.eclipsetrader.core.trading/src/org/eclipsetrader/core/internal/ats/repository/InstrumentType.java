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

package org.eclipsetrader.core.internal.ats.repository;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

@XmlRootElement(name = "instrument")
public class InstrumentType {
	@XmlAttribute(name = "security")
	@XmlJavaTypeAdapter(SecurityAdapter.class)
	private ISecurity instrument;

	@XmlAttribute(name = "time-span")
	@XmlJavaTypeAdapter(TimeSpanAdapter.class)
	private TimeSpan timeSpan;

	public InstrumentType() {
	}

	public InstrumentType(ISecurity instrument, TimeSpan timeSpan) {
	    this.instrument = instrument;
	    this.timeSpan = timeSpan;
    }

	public ISecurity getInstrument() {
    	return instrument;
    }

	public TimeSpan getTimeSpan() {
    	return timeSpan;
    }
}
