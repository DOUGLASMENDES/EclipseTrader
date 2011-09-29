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

package org.eclipsetrader.core.internal.trading;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.instruments.ISecurity;

@XmlRootElement(name = "instrument")
public class InstrumentElement {

    @XmlAttribute(name = "instrument")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity instrument;

    @XmlElementRef
    private List<AlertElement> alerts = new ArrayList<AlertElement>();

    public InstrumentElement() {
    }

    public InstrumentElement(ISecurity instrument, List<AlertElement> alerts) {
        this.instrument = instrument;
        this.alerts = alerts;
    }

    @XmlTransient
    public ISecurity getInstrument() {
        return instrument;
    }

    @XmlTransient
    public AlertElement[] getAlerts() {
        return alerts.toArray(new AlertElement[alerts.size()]);
    }
}
