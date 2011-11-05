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

import org.eclipsetrader.core.ats.ITradingSystemContext;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.PricingEnvironment;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

public class SimulationContext implements ITradingSystemContext {

    private final Broker broker;
    private final Account account;
    private final PricingEnvironment pricingEnvironment;

    public SimulationContext(Broker broker, Account account, PricingEnvironment pricingEnvironment) {
        this.broker = broker;
        this.account = account;
        this.pricingEnvironment = pricingEnvironment;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getBroker()
     */
    @Override
    public IBroker getBroker() {
        return broker;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getAccount()
     */
    @Override
    public IAccount getAccount() {
        return account;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getPricingEnvironment()
     */
    @Override
    public IPricingEnvironment getPricingEnvironment() {
        return pricingEnvironment;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystemContext#getInitialBackfillSize()
     */
    @Override
    public int getInitialBackfillSize() {
        return 0;
    }
}
