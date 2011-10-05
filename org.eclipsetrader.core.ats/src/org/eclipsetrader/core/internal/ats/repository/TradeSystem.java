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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.ats.IStrategy;

@XmlRootElement(name = "system")
public class TradeSystem {

    @XmlAttribute(name = "active")
    private boolean active;

    @XmlAttribute(name = "strategy")
    @XmlJavaTypeAdapter(StrategyAdapter.class)
    private IStrategy strategy;

    public TradeSystem() {
    }

    public TradeSystem(boolean active, IStrategy strategy) {
        this.active = active;
        this.strategy = strategy;
    }

    @XmlTransient
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlTransient
    public IStrategy getStrategy() {
        return strategy;
    }
}
