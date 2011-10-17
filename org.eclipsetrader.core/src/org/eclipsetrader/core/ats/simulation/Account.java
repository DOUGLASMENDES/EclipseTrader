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

package org.eclipsetrader.core.ats.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.Cash;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.ITransaction;
import org.eclipsetrader.core.trading.PositionEvent;

public class Account implements IAccount {

    private final String id;

    private Double balance = 0.0;
    private final List<Transaction> transactions = new ArrayList<Transaction>();
    private final Map<ISecurity, Position> positions = new HashMap<ISecurity, Position>();
    private final ListenerList listeners = new ListenerList();

    public Account() {
        this.id = UUID.randomUUID().toString();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getDescription()
     */
    @Override
    public String getDescription() {
        return "Simulated Account";
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getBalance()
     */
    @Override
    public Cash getBalance() {
        return new Cash(balance, Currency.getInstance(Locale.getDefault()));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getTransactions()
     */
    @Override
    public ITransaction[] getTransactions() {
        return transactions.toArray(new ITransaction[transactions.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IAccount#getPositions()
     */
    @Override
    public IPosition[] getPositions() {
        Collection<Position> c = positions.values();
        return c.toArray(new IPosition[c.size()]);
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

    public void processCompletedOrder(OrderMonitor monitor) {
        Transaction transaction = new Transaction(monitor, monitor.getOrder().getDate());
        monitor.setTransaction(transaction);
        transactions.add(transaction);

        Long quantity = monitor.getOrder().getSide() == IOrderSide.Sell ? -monitor.getFilledQuantity() : monitor.getFilledQuantity();
        double averagePrice = transaction.getAmount().getAmount() / monitor.getFilledQuantity();

        balance -= quantity * averagePrice;

        Position position = positions.get(monitor.getOrder().getSecurity());
        if (position == null) {
            position = new Position(monitor.getOrder().getSecurity(), quantity, averagePrice);
            positions.put(position.getSecurity(), position);
            firePositionOpenedEvent(position);
        }
        else {
            position.add(quantity, averagePrice);
            if (position.getQuantity() == 0L) {
                positions.remove(position.getSecurity());
                firePositionClosedEvent(position);
            }
            else {
                firePositionChangedEvent(position);
            }
        }
    }

    protected void firePositionOpenedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionOpened(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error notifying listeners", t);
                CoreActivator.log(status);
            }
        }
    }

    protected void firePositionClosedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionClosed(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error notifying listeners", t);
                CoreActivator.log(status);
            }
        }
    }

    protected void firePositionChangedEvent(Position position) {
        PositionEvent event = new PositionEvent(this, position);

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((IPositionListener) l[i]).positionChanged(event);
            } catch (Throwable t) {
                Status status = new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error notifying listeners", t);
                CoreActivator.log(status);
            }
        }
    }
}
