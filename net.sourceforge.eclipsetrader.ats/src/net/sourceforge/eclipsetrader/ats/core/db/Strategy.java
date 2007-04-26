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
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.core.db.Security;

public class Strategy extends PersistentObject {
	String name;

	String pluginId;

	Component marketManager;

	Component entry;

	Component exit;

	Component moneyManager;

	ObservableList securities = new ObservableList();

	boolean autoStart = false;

	Map params = new HashMap();

	public Strategy() {
	}

	public Strategy(Integer id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		if (this.pluginId != pluginId) {
			this.pluginId = pluginId;
			setChanged();
		}
	}

	public Component getMarketManager() {
		return marketManager;
	}

	public void setMarketManager(Component marketManager) {
		if (this.marketManager != marketManager) {
			this.marketManager = marketManager;
			setChanged();
		}
	}

	public Component getEntry() {
		return entry;
	}

	public void setEntry(Component entry) {
		if (this.entry != entry) {
			this.entry = entry;
			setChanged();
		}
	}

	public Component getExit() {
		return exit;
	}

	public void setExit(Component exit) {
		if (this.exit != exit) {
			this.exit = exit;
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

	public List getSecurities() {
		return Collections.unmodifiableList(securities);
	}

	public void addSecurity(Security security) {
		if (!securities.contains(security)) {
			securities.add(security);
			setChanged();
		}
	}

	public void removeSecurity(Security security) {
		securities.remove(security);
		setChanged();
	}

	public void addSecurityCollectionObserver(ICollectionObserver observer) {
		securities.addCollectionObserver(observer);
	}

	public void removeSecurityCollectionObserver(ICollectionObserver observer) {
		securities.removeCollectionObserver(observer);
	}

	public int countSecurityCollectionObservers() {
		return securities.countObservers();
	}

	public boolean isAutoStart() {
		return autoStart;
	}

	public void setAutoStart(boolean autoStart) {
		if (this.autoStart != autoStart) {
			this.autoStart = autoStart;
			setChanged();
		}
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
}
