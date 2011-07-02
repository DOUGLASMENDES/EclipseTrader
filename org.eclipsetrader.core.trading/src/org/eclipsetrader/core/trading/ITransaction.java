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

package org.eclipsetrader.core.trading;

import java.util.Date;

import org.eclipsetrader.core.Cash;

public interface ITransaction {

    /**
     * Gets the unique transaction id.
     *
     * @return the unique id.
     */
    public String getId();

    /**
     * Gets the date.
     *
     * @return the date.
     */
    public Date getDate();

    /**
     * Get a description of this transaction.
     *
     * @return the description.
     */
    public String getDescription();

    /**
     * Gets the cash amount.
     *
     * @return the amount.
     */
    public Cash getAmount();

    /**
     * Gets the order that originated this transaction, if any.
     *
     * @return the order reference, or <code>null</code> if the transaction wasn't
     * originated from an order.
     */
    public IOrder getOrder();

    /**
     * Gets the detailed transactions that composes the receiver, if any.
     *
     * @return the transactions.
     */
    public ITransaction[] getTransactions();
}
