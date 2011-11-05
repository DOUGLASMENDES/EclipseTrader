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

package org.eclipsetrader.core.internal.ats;

import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

public class TradingSystemProperties {

    private boolean autostart;
    private IBroker broker;
    private IAccount account;
    private int backfill;

    public TradingSystemProperties() {
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public IBroker getBroker() {
        return broker;
    }

    public void setBroker(IBroker broker) {
        this.broker = broker;
    }

    public IAccount getAccount() {
        return account;
    }

    public void setAccount(IAccount account) {
        this.account = account;
    }

    public int getBackfill() {
        return backfill;
    }

    public void setBackfill(int backfill) {
        this.backfill = backfill;
    }
}
