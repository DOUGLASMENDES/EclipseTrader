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


/**
 * Represent a change of pricing values.
 *
 * @since 1.0
 */
public class PricingDelta {

    private Object oldValue;
    private Object newValue;

    /**
     * Constructor.
     *
     * @param oldValue the old pricing value.
     * @param newValue the new pricing value.
     */
    public PricingDelta(Object oldValue, Object newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the old pricing value.
     *
     * @return the old value.
     * @see ITrade
     * @see IQuote
     * @see ITodayOHL
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the new pricing value.
     *
     * @return the old value.
     * @see ITrade
     * @see IQuote
     * @see ITodayOHL
     */
    public Object getNewValue() {
        return newValue;
    }
}
