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

package org.eclipsetrader.core.ats.simulation;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;

public class Position implements IPosition {

    private final ISecurity security;
    private Long quantity;
    private Double price;

    public Position(ISecurity security, Long quantity, Double price) {
        this.security = security;
        this.quantity = quantity;
        this.price = price;
    }

    public void add(Long quantity, Double price) {
        if (Math.signum(quantity) == Math.signum(this.quantity) || Math.signum(this.quantity) == 0.0) {
            double total = this.quantity * this.price + quantity * price;
            this.quantity += quantity;
            this.price = total / this.quantity;
        }
        else {
            this.quantity += quantity;
            if (Math.signum(quantity) == Math.signum(this.quantity)) {
                this.price = price;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getSecurity()
     */
    @Override
    public ISecurity getSecurity() {
        return security;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getQuantity()
     */
    @Override
    public Long getQuantity() {
        return quantity;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IPosition#getPrice()
     */
    @Override
    public Double getPrice() {
        return price;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Position: instrument=" + security + ", qty=" + quantity + ", price=" + price;
    }
}
