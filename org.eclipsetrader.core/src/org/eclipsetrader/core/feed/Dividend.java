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

package org.eclipsetrader.core.feed;

import java.util.Date;

/**
 * Default implementation of the <code>IDividend</code> interface.
 *
 * @since 1.0
 */
public class Dividend implements IDividend {

    private Date exDate;
    private Double value;

    public Dividend(Date exDate, double value) {
        this.exDate = exDate;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IDividend#getExDate()
     */
    @Override
    public Date getExDate() {
        return exDate;
    }

    public void setExDate(Date exDate) {
        this.exDate = exDate;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.feed.IDividend#getValue()
     */
    @Override
    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
