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

package org.eclipsetrader.core.internal.ats.repository;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement(name = "systems")
public class TradeSystemRepository {

    @XmlElementRef
    private List<TradeSystem> systems = new ArrayList<TradeSystem>();

    public TradeSystemRepository() {
    }

    @XmlTransient
    public TradeSystem[] getSystems() {
        return systems.toArray(new TradeSystem[systems.size()]);
    }

    public void add(TradeSystem system) {
        systems.add(system);
    }

    public void remove(TradeSystem system) {
        systems.remove(system);
    }
}
