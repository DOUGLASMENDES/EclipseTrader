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

package org.eclipsetrader.core.instruments;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IFeedIdentifier;

/**
 * Base interface implemented by all security instruments.
 *
 * @since 1.0
 */
public interface ISecurity extends IAdaptable {

    /**
     * Gets the security name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Gets the feed identifier, or null if the security doesn't have an identifier.
     *
     * @return the identifier, or null.
     */
    public IFeedIdentifier getIdentifier();

    /**
     * Gets the user defined properties collection.
     *
     * @return the user properies.
     */
    public IUserProperties getProperties();
}
