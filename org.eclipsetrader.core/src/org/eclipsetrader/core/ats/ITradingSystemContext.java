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

package org.eclipsetrader.core.ats;

import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

/**
 * Defines the contex associated with running trading systems.
 *
 * @since 1.0
 */
public interface ITradingSystemContext {

    /**
     * Gets the broker.
     *
     * @return the broker.
     */
    public IBroker getBroker();

    /**
     * Gets the account.
     *
     * @return the account.
     */
    public IAccount getAccount();

    /**
     * Gets the pricing environment instance.
     *
     * @return the pricing environment.
     */
    public IPricingEnvironment getPricingEnvironment();

    /**
     * Disposes the context and all associated resources.
     */
    public void dispose();

    /**
     * Returns the number of bars to backfill before the strategy starts.
     * 
     * @return the number of bars.
     */
    public int getInitialBackfillSize();
}
