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

package net.sourceforge.eclipsetrader.ats;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * The Trading System Manager keeps track of the running trading systems.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class TradingSystemManager {
	/**
	 * Singleton instance of the trading system manager.
	 */
	public static TradingSystemManager instance;

	/**
	 * Observers that receive notifications when trading systems are added
	 * or removed from the database.
	 */
	ListenerList observers = new ListenerList(ListenerList.IDENTITY);

	/**
	 * The managed trading system runnables.
	 */
	List runnables = new ArrayList();

	/**
	 * Trading system collection observer.
	 */
	ICollectionObserver observer = new ICollectionObserver() {

		public void itemAdded(Object o) {
			addTradingSystem((TradingSystem) o);
		}

		public void itemRemoved(Object o) {
			removeTradingSystem((TradingSystem) o);
		}
	};

	/**
	 * Listener that listens for changes in the CorePlugin.FEED_RUNNING property
	 * and starts or stops the trading systems accordingly.
	 */
	IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getProperty().equals(CorePlugin.FEED_RUNNING)) {
				Boolean value = (Boolean) e.getNewValue();
				if (value.booleanValue())
					startAll();
				else
					stopAll();
			}
		}
	};

	/**
	 * Creates an instance of the manager.
	 */
	protected TradingSystemManager() {
	}

	/**
	 * Returns the singleton instance of the manager.
	 * 
	 * @return the trading system manager.
	 */
	public synchronized static TradingSystemManager getInstance() {
		if (instance == null) {
			instance = new TradingSystemManager();
			ATSPlugin.getRepository().addTradingSystemsCollectionObserver(instance.observer);
			CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(instance.feedPropertyListener);
		}
		return instance;
	}

	/**
	 * Adds a new trading system to the manager.
	 * 
	 * @param system - the trading system to add.
	 */
	public void addTradingSystem(TradingSystem system) {
		TradingSystemRunnable runnable = new TradingSystemRunnable(system);
		runnables.add(runnable);
		notifyItemAdded(runnable);

		if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
			runnable.start();
	}

	/**
	 * Removes a trading system from the manager.
	 * 
	 * @param system - the trading system to remove.
	 */
	public void removeTradingSystem(TradingSystem system) {
		TradingSystemRunnable[] systems = getRunnables();
		for (int i = 0; i < systems.length; i++) {
			if (systems[i].getTradingSystem() == system) {
				runnables.remove(systems[i]);
				systems[i].stop();
				notifyItemRemoved(systems[i]);
			}
		}
	}

	/**
	 * Starts all trading systems.
	 */
	public void startAll() {
		/*        TradingSystemRunnable[] systems = getRunnables();
		 for (int i = 0; i < systems.length; i++)
		 systems[i].start();*/
	}

	/**
	 * Stops all trading systems.
	 */
	public void stopAll() {
		/*        TradingSystemRunnable[] systems = getRunnables();
		 for (int i = 0; i < systems.length; i++)
		 systems[i].stop();*/
	}

	/**
	 * Returns the number of managed trading systems.
	 * 
	 * @return the number of trading systems.
	 */
	public int getRunnablesCount() {
		return runnables.size();
	}

	/**
	 * Returns an array of the manager trading systems.
	 * 
	 * @return the trading systems array.
	 */
	public TradingSystemRunnable[] getRunnables() {
		return (TradingSystemRunnable[]) runnables.toArray(new TradingSystemRunnable[runnables.size()]);
	}

	/**
	 * Adds an observer to the collection of observers that receive 
	 * notifications when a trading system is added or removed from the manager.
	 * 
	 * @param observer - the observer to add.
	 */
	public void addRunnablesObserver(ICollectionObserver observer) {
		observers.add(observer);
	}

	/**
	 * Removes an observer from the collection of observers that receive 
	 * notifications when a trading system is added or removed from the manager.
	 * 
	 * @param observer - the observer to remove.
	 */
	public void removeRunnablesObserver(ICollectionObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Nofity the collection of observer that a new trading system was
	 * added to the manager.
	 * 
	 * @param o - the trading system that was added.
	 */
	protected void notifyItemAdded(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemAdded(o);
	}

	/**
	 * Nofity the collection of observer that a trading system was
	 * removed from the manager.
	 * 
	 * @param o - the trading system that was removed.
	 */
	protected void notifyItemRemoved(Object o) {
		Object[] obs = observers.getListeners();
		for (int i = 0; i < obs.length; i++)
			((ICollectionObserver) obs[i]).itemRemoved(o);
	}
}
