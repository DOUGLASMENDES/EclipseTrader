/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Order;

/**
 * Default implementation of the ITradingProvider interface.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class TradingProvider implements ITradingProvider
{

    public TradingProvider()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getSides()
     */
    public List getSides()
    {
        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getTypes()
     */
    public List getTypes()
    {
        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getValidity()
     */
    public List getValidity()
    {
        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#getRoutes()
     */
    public List getRoutes()
    {
        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendNew(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendNew(Order order)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendCancelRequest(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendCancelRequest(Order order)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ITradingProvider#sendReplaceRequest(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public void sendReplaceRequest(Order order)
    {
    }
}
