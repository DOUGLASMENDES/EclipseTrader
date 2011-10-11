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

/**
 * Interface implemented by trade strategies.
 *
 * @since 1.0
 */
public interface ITradeStrategy {

    /**
     * Gets the unique id associated with the receiver.
     *
     * @return the unique id.
     */
    public String getId();

    /**
     * Gets the human-readable name of the receiver.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Starts the receiver using the given context.
     *
     * @param context the context to start.
     * @return the trade system monitor instance.
     */
    public ITradeSystemMonitor start(ITradingSystemContext context);
}
