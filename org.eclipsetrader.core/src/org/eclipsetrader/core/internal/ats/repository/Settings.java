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

package org.eclipsetrader.core.internal.ats.repository;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.internal.ats.TradingSystemProperties;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;

@XmlRootElement(name = "settings")
public class Settings {

    @XmlAttribute(name = "autostart")
    private boolean autostart;

    @XmlElement(name = "uri")
    private URI uri;

    @XmlElement(name = "broker")
    @XmlJavaTypeAdapter(BrokerAdapter.class)
    private IBroker broker;

    @XmlElement(name = "account")
    private String account;

    @XmlElement(name = "backfill")
    private int backfill;

    public Settings() {
    }

    public Settings(URI uri, TradingSystemProperties properties) {
        this.uri = uri;
        this.autostart = properties.isAutostart();
        this.broker = properties.getBroker();
        this.account = properties.getAccount() != null ? properties.getAccount().getId() : null;
        this.backfill = properties.getBackfill();
    }

    @XmlTransient
    public URI getUri() {
        return uri;
    }

    public void setUri(URI strategy) {
        this.uri = strategy;
    }

    @XmlTransient
    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    @XmlTransient
    public IBroker getBroker() {
        return broker;
    }

    @XmlTransient
    public IAccount getAccount() {
        if (broker == null || account == null) {
            return null;
        }
        for (IAccount account : broker.getAccounts()) {
            if (account.getId().equals(this.account)) {
                return account;
            }
        }
        return null;
    }

    @XmlTransient
    public TradingSystemProperties getProperties() {
        TradingSystemProperties properties = new TradingSystemProperties();
        properties.setAutostart(autostart);
        properties.setBroker(broker);
        properties.setAccount(getAccount());
        properties.setBackfill(backfill);
        return properties;
    }
}
