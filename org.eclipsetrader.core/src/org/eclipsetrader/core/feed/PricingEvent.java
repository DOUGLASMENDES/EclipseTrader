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

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Event used to notify listeners about changes in the pricing
 * environment for a security.
 *
 * @since 1.0
 */
public class PricingEvent {

    private ISecurity security;
    private PricingDelta[] delta;

    public PricingEvent(ISecurity security, PricingDelta[] delta) {
        this.security = security;
        this.delta = delta;
    }

    /**
     * Returns the security associated with the event.
     *
     * @return the security.
     */
    public ISecurity getSecurity() {
        return security;
    }

    /**
     * Returns the pricing objects that are changed since the last event.
     *
     * @return the pricing objects.
     */
    public PricingDelta[] getDelta() {
        return delta;
    }
}
