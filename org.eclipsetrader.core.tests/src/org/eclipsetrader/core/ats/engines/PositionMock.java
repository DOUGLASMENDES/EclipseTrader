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

package org.eclipsetrader.core.ats.engines;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;

public class PositionMock implements IPosition {

    private ISecurity security;
    private Long quantity;
    private Double price;

    public PositionMock(ISecurity security, Long quantity, Double price) {
        this.security = security;
        this.quantity = quantity;
        this.price = price;
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

}
