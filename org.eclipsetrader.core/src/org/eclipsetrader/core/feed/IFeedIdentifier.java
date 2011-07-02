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

import org.eclipse.core.runtime.IAdaptable;

/**
 * Uniquely identify a feed symbol to a connector.
 * <p>Implementations can optionally adapt to <code>IFeedProperties</code>
 * interface to provide additional properties.</p>
 *
 * @since 1.0
 */
public interface IFeedIdentifier extends IAdaptable {

    /**
     * Symbol property.
     */
    public static final String PROP_SYMBOL = "symbol";

    /**
     * Properties set property.
     */
    public static final String PROP_PROPERTIES = "properties";

    /**
     * Returns the symbol used to identify the security.
     *
     * @return the symbol
     */
    public String getSymbol();
}
