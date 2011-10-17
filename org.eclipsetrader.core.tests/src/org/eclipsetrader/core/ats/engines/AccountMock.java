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

package org.eclipsetrader.core.ats.engines;

import org.eclipse.core.runtime.ListenerList;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.PositionEvent;

public class AccountMock implements IAccount {

    private IPosition[] positions = new IPosition[0];
    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

    public AccountMock() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getId()
     */
    @Override
    public String getId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getDescription()
     */
    @Override
    public String getDescription() {
        return "AccountMock";
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getBalance()
     */
    @Override
    public Cash getBalance() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
     */
    @Override
    public ITransaction[] getTransactions() {
        return new ITransaction[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
    @Override
    public IPosition[] getPositions() {
        return positions;
    }

    public void setPositions(IPosition[] positions) {
        this.positions = positions;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#addPositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void addPositionListener(IPositionListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#removePositionListener(org.eclipsetrader.core.trading.IPositionListener)
     */
    @Override
    public void removePositionListener(IPositionListener listener) {
        listeners.remove(listener);
    }

    public void firePositionChangedEvent(PositionEvent event) {
        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IPositionListener) l[i]).positionChanged(event);
        }
    }
}
