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

package org.eclipsetrader.repository.local.internal.types;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IHolding;

@XmlRootElement(name = "holding")
public class HoldingType {

    @XmlAttribute(name = "security")
    @XmlJavaTypeAdapter(SecurityAdapter.class)
    private ISecurity security;

    @XmlAttribute(name = "position")
    private Long position;

    @XmlAttribute(name = "price")
    private Double purchasePrice;

    @XmlAttribute(name = "date")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date date;

    public HoldingType() {
    }

    public HoldingType(IHolding element) {
        security = element.getSecurity();
        position = element.getPosition();
        purchasePrice = element.getPurchasePrice();
        date = element.getDate();
    }

    public IHolding getElement() {
        return new Holding(security, position, purchasePrice, date);
    }
}
