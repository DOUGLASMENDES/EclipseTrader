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

package net.sourceforge.eclipsetrader.ats.core.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.eclipsetrader.ats.ATSPlugin;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IBarListener;
import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.ats.core.events.OrderEvent;
import net.sourceforge.eclipsetrader.ats.core.events.PositionEvent;
import net.sourceforge.eclipsetrader.ats.core.runnables.StrategyRunnable;
import net.sourceforge.eclipsetrader.ats.core.runnables.TradingSystemRunnable;
import net.sourceforge.eclipsetrader.ats.ui.report.BacktestReportView;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderSide;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Trading system backtest.
 * 
 * @author Marco Maccaferri
 * @since 1.0
 */
public class Backtest extends Job {
	/**
	 * Order side descriptions mapping.
	 */
	static Map sideLabels = new HashMap();

	/**
	 * Exection manager.
	 */
	BacktestExecutionManager executionManager;

	/**
	 * Market manager.
	 */
	BacktestMarketManager marketManager;

	/**
	 * Main runnable.
	 */
	TradingSystemRunnable tradingSystemRunnable;

	List trades = new ArrayList();

	private int timeInMarket = 0;

	private int marketDays = 0;

	private int flatPeriod = 0;

	private int maxPositions = 0;

	/**
	 * Constructor used to backtest a trading system.
	 * 
	 * @param system the trading system
	 * @param start the start date
	 * @param end the end date
	 */
	public Backtest(TradingSystem system, Date start, Date end) {
		super("Backtest " + system.getName() + " from " + SimpleDateFormat.getDateInstance().format(start) + " to " + SimpleDateFormat.getDateInstance().format(end));

		executionManager = new BacktestExecutionManager();
		marketManager = new BacktestMarketManager(start, end, executionManager);

		tradingSystemRunnable = new TradingSystemRunnable(system, executionManager);

		for (Iterator iter = system.getStrategies().iterator(); iter.hasNext();) {
			Strategy strategy = (Strategy) iter.next();

			StrategyRunnable runnable = new StrategyRunnable(tradingSystemRunnable, strategy, marketManager);

			Security[] securities = (Security[]) strategy.getSecurities().toArray(new Security[0]);
			for (int i = 0; i < securities.length; i++)
				runnable.addSecurity(securities[i]);

			tradingSystemRunnable.addStrategy(runnable);
		}
	}

	/**
	 * Constructor used to backtest a single strategy.
	 * 
	 * @param strategy the strategy
	 * @param start the start date
	 * @param end the end date
	 */
	public Backtest(Strategy strategy, Date start, Date end) {
		super("Backtest " + strategy.getName() + " from " + SimpleDateFormat.getDateInstance().format(start) + " to " + SimpleDateFormat.getDateInstance().format(end));

		executionManager = new BacktestExecutionManager();
		marketManager = new BacktestMarketManager(start, end, executionManager);

		tradingSystemRunnable = new TradingSystemRunnable(new TradingSystem(), executionManager);

		StrategyRunnable runnable = new StrategyRunnable(tradingSystemRunnable, strategy, marketManager);

		Security[] securities = (Security[]) strategy.getSecurities().toArray(new Security[0]);
		for (int i = 0; i < securities.length; i++)
			runnable.addSecurity(securities[i]);

		tradingSystemRunnable.addStrategy(runnable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Backtesting " + getName(), 3);

		try {
			trades.clear();

			StrategyRunnable[] runnables = tradingSystemRunnable.getRunnables();
			for (int i = 0; i < runnables.length; i++) {
				TradeStatisticsListener listener = new TradeStatisticsListener(runnables[i].getStrategy());
				executionManager.addPositionListener(listener);
				executionManager.addOrderListener(listener);
				marketManager.addBarListener(listener);
			}

			TimeInMarketListener timeInMarketListener = new TimeInMarketListener();
			executionManager.addPositionListener(timeInMarketListener);
			marketManager.addBarListener(timeInMarketListener);

			tradingSystemRunnable.start();
			monitor.worked(1);

			marketManager.run();
			monitor.worked(1);

			tradingSystemRunnable.stop();

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						BacktestReportView view = (BacktestReportView) page.showView(BacktestReportView.VIEW_ID);
						view.setInput(Backtest.this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ATSPlugin.PLUGIN_ID, -1, "An error occurred during trading system backtest", e);
			ATSPlugin.getDefault().getLog().log(status);
		}

		monitor.worked(1);
		monitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * Returns the trades performed by the test.
	 * 
	 * @return the trades.
	 */
	public Trade[] getTrades() {
		return (Trade[]) trades.toArray(new Trade[0]);
	}

	public int getTotalBars() {
		return marketManager.getBarCount();
	}

	public int getTimeInMarket() {
		return timeInMarket;
	}

	public int getMarketDays() {
		return marketDays;
	}

	public int getFlatPeriod() {
		return flatPeriod;
	}

	public int getMaxPositions() {
		return maxPositions;
	}

	static {
		sideLabels.put(OrderSide.BUY, "Buy");
		sideLabels.put(OrderSide.SELL, "Sell");
		sideLabels.put(OrderSide.SELLSHORT, "Sell Short");
		sideLabels.put(OrderSide.BUYCOVER, "Buy Cover");
	}

	private class TimeInMarketListener implements IPositionListener, IBarListener {
		int openPositions = 0;

		int outOfMarket = 0;

		Set<Security> securities = new HashSet<Security>();

		private TimeInMarketListener() {
			timeInMarket = 0;
			marketDays = 0;
			maxPositions = 0;
			flatPeriod = 0;
		}

		public void positionChanged(PositionEvent e) {
		}

		public void positionClosed(PositionEvent e) {
			openPositions--;
		}

		public void positionOpened(PositionEvent e) {
			openPositions++;
			maxPositions = Math.max(maxPositions, openPositions);
		}

		public void positionValueChanged(PositionEvent e) {
		}

		public void barClose(BarEvent e) {
			securities.remove(e.security);
			if (securities.isEmpty()) {
				if (openPositions > 0) {
					flatPeriod = Math.max(flatPeriod, outOfMarket);
					outOfMarket = 0;
					timeInMarket++;
				}
				else
					outOfMarket++;
				marketDays++;
			}
		}

		public void barOpen(BarEvent e) {
			securities.add(e.security);
		}
	}

	private class TradeStatisticsListener implements IPositionListener, IOrderListener, IBarListener {
		Strategy strategy;

		Set<Security> securities = new HashSet<Security>();

		Map currentTrades = new HashMap();

		Map lastFilledOrders = new HashMap();

		private TradeStatisticsListener(Strategy strategy) {
			this.strategy = strategy;
			for (Iterator iter = strategy.getSecurities().iterator(); iter.hasNext();)
				securities.add((Security) iter.next());
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IPositionListener#positionOpened(net.sourceforge.eclipsetrader.ats.core.events.PositionEvent)
		 */
		public void positionOpened(PositionEvent e) {
			if (securities.contains(e.security)) {
				Trade trade = (Trade) currentTrades.get(e.security);
				if (trade == null) {
					trade = new Trade(strategy, e.security, e.date, e.position.getQuantity(), e.position.getPrice(), "");
					currentTrades.put(e.security, trade);
				}

				Order order = (Order) lastFilledOrders.get(e.security);
				if (trade != null) {
					trade.setEnterMessage(order.getText());
					lastFilledOrders.remove(e.security);
				}
			}
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IPositionListener#positionChanged(net.sourceforge.eclipsetrader.ats.core.events.PositionEvent)
		 */
		public void positionChanged(PositionEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IPositionListener#positionClosed(net.sourceforge.eclipsetrader.ats.core.events.PositionEvent)
		 */
		public void positionClosed(PositionEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IPositionListener#positionValueChanged(net.sourceforge.eclipsetrader.ats.core.events.PositionEvent)
		 */
		public void positionValueChanged(PositionEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderCancelled(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
		 */
		public void orderCancelled(OrderEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderFilled(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
		 */
		public void orderFilled(OrderEvent e) {
			if (securities.contains(e.security)) {
				lastFilledOrders.put(e.security, e.order);
				if (e.order.getSide() == OrderSide.SELL) {
					Trade trade = (Trade) currentTrades.get(e.security);
					if (trade != null) {
						trade.setExit(e.order.getDate(), e.order.getAveragePrice(), e.order.getText());
						trades.add(trade);
						currentTrades.remove(e.security);
						lastFilledOrders.remove(e.security);
					}
				}
				else if (e.order.getSide() == OrderSide.BUY) {
					Trade trade = (Trade) currentTrades.get(e.security);
					if (trade != null) {
						trade.setEnterMessage(e.order.getText());
						lastFilledOrders.remove(e.security);
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderRejected(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
		 */
		public void orderRejected(OrderEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderStatusChanged(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
		 */
		public void orderStatusChanged(OrderEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IOrderListener#orderSubmitted(net.sourceforge.eclipsetrader.ats.core.events.OrderEvent)
		 */
		public void orderSubmitted(OrderEvent e) {
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barClose(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
		 */
		public void barClose(BarEvent e) {
			if (securities.contains(e.security)) {
				Trade trade = (Trade) currentTrades.get(e.security);
				if (trade != null)
					trade.bars++;
			}
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.eclipsetrader.ats.core.events.IBarListener#barOpen(net.sourceforge.eclipsetrader.ats.core.events.BarEvent)
		 */
		public void barOpen(BarEvent e) {
		}
	}
}
