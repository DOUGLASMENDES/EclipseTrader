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

package org.eclipsetrader.core.internal.ats;

import java.util.ArrayList;
import java.util.List;

import org.eclipsetrader.core.ats.IStrategy;
import org.eclipsetrader.core.ats.ITradingInstrument;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.ats.ITradingSystemContext;
import org.eclipsetrader.core.ats.engines.JavaScriptEngine;
import org.eclipsetrader.core.instruments.ISecurity;

public class TradingSystem implements ITradingSystem {

    private final IStrategy strategy;
    private final List<TradingInstrument> instruments = new ArrayList<TradingInstrument>();

    private JavaScriptEngine engine;

    public TradingSystem(IStrategy strategy) {
        this.strategy = strategy;

        for (ISecurity security : strategy.getInstruments()) {
            instruments.add(new TradingInstrument(security));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#getStrategy()
     */
    @Override
    public IStrategy getStrategy() {
        return strategy;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#getInstruments()
     */
    @Override
    public ITradingInstrument[] getInstruments() {
        return instruments.toArray(new ITradingInstrument[instruments.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#start(org.eclipsetrader.core.ats.ITradingSystemContext)
     */
    @Override
    public void start(ITradingSystemContext context) throws Exception {
        engine = new JavaScriptEngine(this, context.getPricingEnvironment(), context.getAccount(), context.getBroker());
        engine.start();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.ats.ITradingSystem#stop()
     */
    @Override
    public void stop() {
        if (engine != null) {
            engine.stop();
            engine.dispose();
            engine = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(strategy.getClass())) {
            return strategy;
        }
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }
        return null;
    }
}
