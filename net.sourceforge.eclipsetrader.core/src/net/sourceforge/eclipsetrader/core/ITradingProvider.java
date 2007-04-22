/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import java.util.List;

import net.sourceforge.eclipsetrader.core.db.Order;

/**
 * Trading providers interface.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public interface ITradingProvider
{
    
    /**
     * Gets a list of the supported order types.
     * 
     * @return a list or OrderType objects
     */
    public List getTypes();
    
    /**
     * Gets a list of the supported order sides.
     * 
     * @return a list or OrderSide objects
     */
    public List getSides();

    /**
     * Gets a list of the supported validity values.
     * 
     * @return a list or OrderValidity objects
     */
    public List getValidity();

    /**
     * Gets a list of the supported routes (or exchanges).
     * 
     * @return a list or OrderRoute objects
     */
    public List getRoutes();

    /**
     * Send a new order to the receiver.
     * The order instance passed as argument may not be the same instance
     * that is saved in the orders repository. Callers should not add observers
     * or keep the passed instance as it may be never updated.
     * 
     * @param order - the order to send
     */
    public void sendNew(Order order);

    /**
     * Send a request to the receiver to cancel the order.
     * 
     * @param order - the order to cancel
     */
    public void sendCancelRequest(Order order);

    /**
     * Send a request to the receiver to replace the order values.
     * 
     * @param order - the order to cancel
     */
    public void sendReplaceRequest(Order order);
}
