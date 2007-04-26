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
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.ats.core.ExecutionManager;
import net.sourceforge.eclipsetrader.ats.core.IExecutionManager;
import net.sourceforge.eclipsetrader.ats.core.IMoneyManager;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.ats.core.internal.NullTradingProvider;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.core.runtime.ListenerList;

/**
 * Instances of this class represents a runnable trading system.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class TradingSystemRunnable extends Observable {
	/**
	 * The underlying trading system.
	 */
	TradingSystem system;

	/**
	 * The account to use for trades.
	 */
	Account account;

	/**
	 * The trading provider to use to send trading orders.
	 */
	ITradingProvider tradingProvider = new NullTradingProvider();

	/**
	 * The, optional, money manager component.
	 */
	IMoneyManager moneyManager;

	/**
	 * The execution manager that translates signals into orders.
	 */
	IExecutionManager executionManager = new ExecutionManager();

	/**
	 * The list of strategy runnable instances.
	 */
	List runnables = new ArrayList();

	/**
	 * Observers that receive notifications when strategies are added
	 * or removed from the object.
	 */
	ListenerList observers = new ListenerList(ListenerList.IDENTITY);

	/**
	 * Holds the running status.
	 */
	boolean running = false;

	/**
	 * Observer that receive notifications when a strategy is added or removed
	 * from the underlying trading system object.
	 */
	ICollectionObserver collectionObserver = new ICollectionObserver() {

		public void itemAdded(Object o) {
			addStrategy((Strategy) o);
		}

		public void itemRemoved(Object o) {
			removeStrategy((Strategy) o);
		}
	};

	/**
	 * Observer for changes to the underlying trading system.
	 */
	Observer observer = new Observer() {

		public void update(Observable o, Object arg) {
			setChanged();
			notifyObservers();
		}
	};

	/**
	 * Construct a runnable instance of the given trading system.
	 * 
	 * @param system - the trading system to run
	 */
	public TradingSystemRunnable(TradingSystem system) {
		this.system = system;

		account = system.getAccount();

		Object obj = CorePlugin.createTradeSourcePlugin(system.getTradingProviderId());
		if (obj != null)
			tradingProvider = (ITradingProvider) obj;

		for (Iterator iter = system.getStrategies().iterator(); iter.hasNext();)
			addStrategy((Strategy) iter.next());

		system.addObserver(observer);
		system.addStrategyCollectionObserver(collectionObserver);
	}

	/**
	 * Construct a runnable instance of a trading system using a specific execution manager.
	 * 
	 * <p>This constructor should be used for backtesting a trading system.</p>
	 * 
	 * @param system - the trading system to run
	 * @param executionManager - the execution manager to use
	 */
	public TradingSystemRunnable(TradingSystem system, IExecutionManager executionManager) {
		this.system = system;

		account = system.getAccount();
		if (executionManager != null)
			this.executionManager = executionManager;
	}

	/**
	 * Disposes all resources associated to the receiver.
	 */
	public void dispose() {
		system.deleteObserver(observer);
		system.removeStrategyCollectionObserver(collectionObserver);

		for (Iterator iter = runnables.iterator(); iter.hasNext();) {
			StrategyRunnable runnable = (StrategyRunnable) iter.next();
			runnable.dispose();
		}
	}

	/**
	 * Starts the trading system and all its strategies.
	 */
	public synchronized void start() {
		if (!isRunning()) {
			executionManager.start(account, tradingProvider);

			for (Iterator iter = runnables.iterator(); iter.hasNext();) {
				StrategyRunnable runnable = (StrategyRunnable) iter.next();
				runnable.start();
			}

			setRunning(true);
		}
	}

	/**
	 * Stops the trading system and all its strategies
	 */
	public synchronized void stop() {
		if (isRunning()) {
			for (Iterator iter = runnables.iterator(); iter.hasNext();) {
				StrategyRunnable runnable = (StrategyRunnable) iter.next();
				runnable.stop();
			}

			executionManager.stop(account, tradingProvider);

			setRunning(false);
		}
	}

	/**
	 * Adds a strategy to the trading system.<br>
	 * This method is meant to be used only internally.
	 * 
	 * @param strategy - the strategy to add.
	 */
	protected void addStrategy(Strategy strategy) {
		addStrategy(new StrategyRunnable(this, strategy));
	}

	/**
	 * Adds a strategy runnable to the trading system.
	 * 
	 * @param strategy - the strategy runnable.
	 */
	public void addStrategy(StrategyRunnable runnable) {
		if (running)
			runnable.start();
		runnables.add(runnable);
		notifyItemAdded(runnable);
	}

	/**
	 * Removes a strategy from the trading system.<br>
	 * This method is meant to be used only internally.
	 * 
	 * @param strategy - the strategy to remove.
	 */
	protected void removeStrategy(Strategy strategy) {
		for (Iterator iter = runnables.iterator(); iter.hasNext();) {
			StrategyRunnable runnable = (StrategyRunnable) iter.next();
			if (runnable.strategy == strategy) {
				removeStrategy(runnable);
				break;
			}
		}
	}

	/**
	 * Removes a strategy runnable from the trading system.
	 * 
	 * @param strategy - the strategy runnable.
	 */
	public void removeStrategy(StrategyRunnable runnable) {
		runnables.remove(runnable);
		if (running)
			runnable.stop();
		runnable.dispose();
		notifyItemRemoved(runnable);
	}

	/**
	 * Returns the underlying trading system object.
	 * 
	 * @return the trading system.
	 */
	public TradingSystem getTradingSystem() {
		return system;
	}

	/**
	 * Returns the number of strategy runnables in the receiver.
	 * 
	 * @return the number of runnables.
	 */
	public int getRunnablesCount() {
		return runnables.size();
	}

	/**
	 * Returns the array of strategy runnables in the receiver.
	 * 
	 * @return the array of runnables.
	 */
	public StrategyRunnable[] getRunnables() {
		return (StrategyRunnable[]) runnables.toArray(new StrategyRunnable[runnables.size()]);
	}

	/**
	 * Adds an observer to the collection of observers that receive 
	 * notifications when a strategy is added or removed from the receiver.
	 * 
	 * @param observer - the observer to add.
	 */
	public void addRunnablesObserver(ICollectionObserver observer) {
		observers.add(observer);
	}

	/**
	 * Removes an observer from the collection of observers that receive 
	 * notifications when a strategy is added or removed from the receiver.
	 * 
	 * @param observer - the observer to remove.
	 */
	public void removeRunnablesObserver(ICollectionObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Nofity the collection of observer that a new strategy was
	 * added to the receiver.
	 * 
	 * @param o - the strategy that was added.
	 */
	protected void notifyItemAdded(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemAdded(o);
	}

	/**
	 * Nofity the collection of observer that a strategy was
	 * removed from the receiver.
	 * 
	 * @param o - the strategy that was removed.
	 */
	protected void notifyItemRemoved(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemRemoved(o);
	}

	/**
	 * Returns the underlying execution manager.
	 * 
	 * @return the execution manager instance.
	 */
	public IExecutionManager getExecutionManager() {
		return executionManager;
	}

	/**
	 * Returns wether the receiver is running or not.
	 * 
	 * @return true if the receiver is running.
	 */
	public boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}
}
