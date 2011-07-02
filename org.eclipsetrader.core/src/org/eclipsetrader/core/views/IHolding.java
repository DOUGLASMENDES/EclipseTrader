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

package org.eclipsetrader.core.views;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Interface for objects that describes a position in a portfolio.
 *
 * @since 1.0
 */
public interface IHolding extends IAdaptable {

    /**
     * The traded security.
     *
     * @return the security.
     */
    public ISecurity getSecurity();

    /**
     * The position in shares or security's units.
     *
     * @return the position.
     */
    public Long getPosition();

    /**
     * The average prices at which the shares were purchased.
     *
     * @return the price.
     */
    public Double getPurchasePrice();

    /**
     * The date at which the position was opened.
     *
     * @return the date.
     */
    public Date getDate();
}
