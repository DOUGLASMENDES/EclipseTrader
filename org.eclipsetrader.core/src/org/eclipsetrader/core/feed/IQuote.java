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

/**
 * Interface to get the level 1 quotes.
 *
 * @since 1.0
 */
public interface IQuote {

    /**
     * Returns the bid price.
     *
     * @return the price.
     */
    public Double getBid();

    /**
     * Returns the ask price.
     *
     * @return the price.
     */
    public Double getAsk();

    /**
     * Returns the number of lots in the bid.
     *
     * @return the number of lots.
     */
    public Long getBidSize();

    /**
     * Returns the number of lots in the ask.
     *
     * @return the number of lots.
     */
    public Long getAskSize();
}
