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

package net.sourceforge.eclipsetrader.ats.core.runnables;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.IComponent;
import net.sourceforge.eclipsetrader.ats.core.IMarketManager;
import net.sourceforge.eclipsetrader.ats.core.MarketManager;
import net.sourceforge.eclipsetrader.ats.core.db.Component;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.ListenerList;

/**
 * Instances of this class manages a running strategy.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class StrategyRunnable {
	/**
	 * The underlying strategy object.
	 */
	Strategy strategy;

	/**
	 * The parent trading system runnable.
	 */
	TradingSystemRunnable parent;

	/**
	 * The underlying market manager.
	 */
	IMarketManager marketManager = new MarketManager();

	/**
	 * The list of security runnable instances.
	 */
	List runnables = new ArrayList();

	/**
	 * Observers that receive notifications when securities are added
	 * or removed from the object.
	 */
	ListenerList observers = new ListenerList(ListenerList.IDENTITY);

	/**
	 * Holds the running status.
	 */
	boolean running = false;

	/**
	 * Observer that receive notifications when a security is added or removed
	 * from the underlying strategy object.
	 */
	ICollectionObserver securityObserver = new ICollectionObserver() {

		public void itemAdded(Object o) {
			addSecurity((Security) o);
		}

		public void itemRemoved(Object o) {
			removeSecurity((Security) o);
		}
	};

	/**
	 * Constructs a runnable instance of the given strategy.
	 * 
	 * @param parent - the parent runnable.
	 * @param strategy - the underlying strategy.
	 */
	public StrategyRunnable(TradingSystemRunnable parent, Strategy strategy) {
		this.parent = parent;
		this.strategy = strategy;

		Component component = strategy.getMarketManager();
		if (component != null) {
			Object obj = ATSPlugin.createExtensionPlugin(ATSPlugin.COMPONENTS_EXTENSION_ID, component.getPluginId());
			if (obj != null)
				this.marketManager = (IMarketManager) obj;
		}

		Security[] securities = (Security[]) strategy.getSecurities().toArray(new Security[0]);
		for (int i = 0; i < securities.length; i++)
			addSecurity(securities[i]);

		strategy.addSecurityCollectionObserver(securityObserver);
	}

	/**
	 * Constructs a runnable instance of a strategy using a specific market manager,
	 * overriding the market manager defined in the strategy.
	 * 
	 * @param parent - the parent runnable.
	 * @param strategy - the underlying strategy.
	 * @param marketManager - the market manager.
	 */
	public StrategyRunnable(TradingSystemRunnable parent, Strategy strategy, IMarketManager marketManager) {
		this.parent = parent;
		this.strategy = strategy;

		if (marketManager != null)
			this.marketManager = marketManager;
	}

	/**
	 * Disposes all resources associated to the receiver.
	 */
	public void dispose() {
		strategy.removeSecurityCollectionObserver(securityObserver);
	}

	/**
	 * Starts the strategy.
	 */
	public synchronized void start() {
		if (!isRunning()) // && getStrategy().getMode() != Strategy.DISABLED)
		{
			for (int i = 0; i < runnables.size(); i++) {
				ComponentRunnable runnable = (ComponentRunnable) runnables.get(i);
				marketManager.addSecurity(runnable.getSecurity());
			}

			marketManager.start();

			for (int i = 0; i < runnables.size(); i++) {
				ComponentRunnable runnable = (ComponentRunnable) runnables.get(i);
				runnable.start();
			}

			setRunning(true);
		}
	}

	/**
	 * Stops the strategy.
	 */
	public synchronized void stop() {
		if (isRunning()) {
			setRunning(false);

			marketManager.stop();

			for (int i = 0; i < runnables.size(); i++) {
				ComponentRunnable runnable = (ComponentRunnable) runnables.get(i);
				runnable.stop();
				marketManager.removeSecurity(runnable.getSecurity());
			}
		}
	}

	/**
	 * Adds a security to the strategy.
	 * 
	 * @param security - the security to add.
	 */
	public void addSecurity(Security security) {
		IComponent component = ATSPlugin.createStrategyPlugin(strategy.getPluginId());
		ComponentRunnable runnable = new ComponentRunnable(this, security, component);
		runnables.add(runnable);
		if (isRunning()) {
			marketManager.addSecurity(security);
			runnable.start();
		}
		notifyItemAdded(runnable);
	}

	/**
	 * Removes a security from the strategy.
	 * 
	 * @param security - the security to remove.
	 */
	public void removeSecurity(Security security) {
		ComponentRunnable[] runnable = getRunnables();
		for (int i = 0; i < runnable.length; i++) {
			if (runnable[i].getSecurity() == security) {
				if (isRunning()) {
					runnable[i].stop();
					marketManager.removeSecurity(security);
				}
				runnables.remove(runnable[i]);
				notifyItemRemoved(runnable[i]);
				runnable[i].dispose();
			}
		}
	}

	/**
	 * Returns the number of security runnables in the receiver.
	 * 
	 * @return the number of runnables.
	 */
	public int getRunnablesCount() {
		return runnables.size();
	}

	/**
	 * Returns the array of security runnables in the receiver.
	 * 
	 * @return the array of runnables.
	 */
	public ComponentRunnable[] getRunnables() {
		return (ComponentRunnable[]) runnables.toArray(new ComponentRunnable[runnables.size()]);
	}

	/**
	 * Adds an observer to the collection of observers that receive 
	 * notifications when a security is added or removed from the receiver.
	 * 
	 * @param observer - the observer to add.
	 */
	public void addRunnablesObserver(ICollectionObserver observer) {
		observers.add(observer);
	}

	/**
	 * Removes an observer from the collection of observers that receive 
	 * notifications when a security is added or removed from the receiver.
	 * 
	 * @param observer - the observer to remove.
	 */
	public void removeRunnablesObserver(ICollectionObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Nofity the collection of observer that a new security was
	 * added to the receiver.
	 * 
	 * @param o - the ComponentRunnable that was added.
	 */
	protected void notifyItemAdded(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemAdded(o);
	}

	/**
	 * Nofity the collection of observer that a security was
	 * removed from the receiver.
	 * 
	 * @param o - the ComponentRunnable that was removed.
	 */
	protected void notifyItemRemoved(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemRemoved(o);
	}

	/**
	 * Returns the parent trading system runnable.
	 * 
	 * @return the parent runnable.
	 */
	public TradingSystemRunnable getParent() {
		return parent;
	}

	/**
	 * Returns the underlying strategy object.
	 * 
	 * @return the strategy object.
	 */
	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * Returns the underlying market manager object.
	 * 
	 * @return the market manager object.
	 */
	public IMarketManager getMarketManager() {
		return marketManager;
	}

	/**
	 * Returns wether the receiver is running or not.
	 * 
	 * @return true if the receiver is running.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sets the receiver's running status.
	 * 
	 * @param running - true if running, false otherwise.
	 */
	protected void setRunning(boolean running) {
		this.running = running;
	}
}
