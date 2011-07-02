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

package org.eclipsetrader.core.internal.markets;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "list")
@XmlType(name = "org.eclipsetrader.core.markets.List")
public class MarketList {

    @XmlElementRef
    private List<Market> list = new ArrayList<Market>();

    public MarketList() {
    }

    public MarketList(List<Market> list) {
        this.list = list;
    }

    public List<Market> getList() {
        return list;
    }
}
