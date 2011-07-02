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
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.trading.IAlert;

@XmlRootElement(name = "alert")
public class AlertElement {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(AlertTypeAdapter.class)
    private IAlert alert;

    @XmlElementRef
    private List<ParameterElement> parameters = new ArrayList<ParameterElement>();

    public AlertElement() {
    }

    public AlertElement(IAlert alert) {
        this.alert = alert;

        this.parameters = new ArrayList<ParameterElement>();
        for (Entry<String, Object> entry : alert.getParameters().entrySet()) {
            this.parameters.add(ParameterElement.create(entry.getKey(), entry.getValue()));
        }
    }

    public IAlert getAlert() {
        return alert;
    }

    public ParameterElement[] getParameters() {
        return parameters.toArray(new ParameterElement[parameters.size()]);
    }
}
