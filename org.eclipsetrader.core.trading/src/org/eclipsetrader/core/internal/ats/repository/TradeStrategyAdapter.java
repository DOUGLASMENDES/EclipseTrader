/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.ats.repository;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.ats.ITradeStrategy;
import org.eclipsetrader.core.internal.trading.Activator;

public class TradeStrategyAdapter extends XmlAdapter<String, ITradeStrategy> {

	public TradeStrategyAdapter() {
	}

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(ITradeStrategy v) throws Exception {
	    return v != null ? v.getId() : null;
    }

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ITradeStrategy unmarshal(String v) throws Exception {
	    return v != null ? getStrategyWithId(v) : null;
    }

    protected ITradeStrategy getStrategyWithId(String id) {
    	ITradeStrategy strategy = null;

    	if (Activator.getDefault() != null)
    		; // TODO strategy = Activator.getDefault().getStrategyWithId(id);

    	if (strategy == null) {
    		// TODO Failsafe
    	}

    	return strategy;
    }
}
