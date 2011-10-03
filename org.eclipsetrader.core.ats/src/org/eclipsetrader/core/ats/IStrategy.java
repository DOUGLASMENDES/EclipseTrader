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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Interface implemented by trading system strategies.
 *
 * @since 1.0
 */
public interface IStrategy extends IAdaptable {

    public static final String PROP_NAME = "name";
    public static final String PROP_INSTRUMENTS = "instruments";
    public static final String PROP_BARS_TIMESPAN = "barsTimeSpan";

    /**
     * Gets the human-readable name of the receiver.
     *
     * @return the name.
     */
    public String getName();

    /**
     * Gets the instruments handled by the receiver.
     * 
     * @return the instruments
     */
    public ISecurity[] getInstruments();

    /**
     * Gets the bars timespan the receiver expects to handle.
     *
     * @return the bars timespan.
     */
    public TimeSpan[] getBarsTimeSpan();
}
