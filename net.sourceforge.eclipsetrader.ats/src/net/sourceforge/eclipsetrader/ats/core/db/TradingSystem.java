/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.core.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.core.db.visitors.ITradingSystemVisitor;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;

public class TradingSystem extends PersistentObject {
	String name;

	Account account;

	String tradingProviderId;

	Component moneyManager;

	ObservableList strategies = new ObservableList();

	Map params = new HashMap();

	public TradingSystem() {
	}

	public TradingSystem(Integer id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (this.name != name) {
			this.name = name;
			setChanged();
		}
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		if (this.account != account) {
			this.account = account;
			setChanged();
		}
	}

	public String getTradingProviderId() {
		return tradingProviderId;
	}

	public void setTradingProviderId(String tradingProviderId) {
		if (this.tradingProviderId != tradingProviderId) {
			this.tradingProviderId = tradingProviderId;
			setChanged();
		}
	}

	public Component getMoneyManager() {
		return moneyManager;
	}

	public void setMoneyManager(Component moneyManager) {
		if (this.moneyManager != moneyManager) {
			this.moneyManager = moneyManager;
			setChanged();
		}
	}

	public List getStrategies() {
		return Collections.unmodifiableList(strategies);
	}

	public void addStrategy(Strategy strategy) {
		strategies.add(strategy);
		setChanged();
	}

	public void removeStrategy(Strategy strategy) {
		strategies.remove(strategy);
		setChanged();
	}

	public void addStrategyCollectionObserver(ICollectionObserver observer) {
		strategies.addCollectionObserver(observer);
	}

	public void removeStrategyCollectionObserver(ICollectionObserver observer) {
		strategies.removeCollectionObserver(observer);
	}

	public int countStrategyCollectionObservers() {
		return strategies.countObservers();
	}

	public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		if (this.params != params) {
			this.params = params;
			setChanged();
		}
	}

	public String getParameter(String key) {
		return (String) params.get(key);
	}

	public void setParameter(String key, String value) {
		if (params.get(key) != value) {
			params.put(key, value);
			setChanged();
		}
	}

	public void accept(ITradingSystemVisitor visitor) {
		visitor.visit(this);
		for (Iterator iter = strategies.iterator(); iter.hasNext();) {
			Strategy strategy = (Strategy) iter.next();
			visitor.visit(strategy);
		}
	}
}
