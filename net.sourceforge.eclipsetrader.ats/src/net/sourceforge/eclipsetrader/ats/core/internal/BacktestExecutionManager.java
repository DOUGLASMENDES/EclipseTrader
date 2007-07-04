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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sourceforge.eclipsetrader.ats.core.IExecutionManager;
import net.sourceforge.eclipsetrader.ats.core.Signal;
import net.sourceforge.eclipsetrader.ats.core.SignalSide;
import net.sourceforge.eclipsetrader.ats.core.SignalType;
import net.sourceforge.eclipsetrader.ats.core.events.BarEvent;
import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.ats.core.events.OrderEvent;
import net.sourceforge.eclipsetrader.ats.core.events.PositionEvent;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.DefaultAccount;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderSide;
import net.sourceforge.eclipsetrader.core.db.OrderStatus;
import net.sourceforge.eclipsetrader.core.db.OrderType;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Transaction;

import org.eclipse.core.runtime.ListenerList;

public class BacktestExecutionManager implements IExecutionManager {
	static Map orderSide = new HashMap();

	static Map orderType = new HashMap();

	Account account = new DefaultAccount();

	ITradingProvider tradingProvider = new NullTradingProvider() {
		@Override
		public void sendNew(Order order) {
			super.sendNew(order);
			ordersCollectionObserver.itemAdded(order);
		}
	};

	ListenerList orderListeners = new ListenerList(ListenerList.IDENTITY);

	Map securityOrderListeners = new HashMap();

	ListenerList positionListeners = new ListenerList(ListenerList.IDENTITY);

	Map securityPositionListeners = new HashMap();

	Date currentDate;

	Map orders = new HashMap();

	Map positions = new HashMap();

	Map positionValues = new HashMap();

	ICollectionObserver ordersCollectionObserver = new ICollectionObserver() {
		public void itemAdded(Object o) {
			if (o instanceof Order) {
				Order order = (Order) o;

				if (order.getAccount() == account && order.getProvider() == tradingProvider) {
					orders.put(order, order.getStatus());
					fireOrderSubmitted(order);

					if (order.getStatus().equals(OrderStatus.FILLED))
						fireOrderFilled(order);
					else if (order.getStatus().equals(OrderStatus.REJECTED))
						fireOrderRejected(order);
					else if (order.getStatus().equals(OrderStatus.CANCELED))
						fireOrderCancelled(order);

					order.addObserver(orderObserver);
				}
			}
		}

		public void itemRemoved(Object o) {
			((Order) o).deleteObserver(orderObserver);
			orders.remove(o);
		}
	};

	Observer orderObserver = new Observer() {
		public void update(Observable o, Object arg) {
			Order order = (Order) o;
			OrderStatus oldStatus = (OrderStatus) orders.get(order);

			fireOrderStatusChanged(order);

			if (!order.getStatus().equals(oldStatus)) {
				orders.put(order, order.getStatus());

				if (order.getStatus().equals(OrderStatus.FILLED))
					fireOrderFilled(order);
				else if (order.getStatus().equals(OrderStatus.REJECTED))
					fireOrderRejected(order);
				else if (order.getStatus().equals(OrderStatus.CANCELED))
					fireOrderCancelled(order);
			}
		}
	};

	Observer accountObserver = new Observer() {
		public void update(Observable o, Object arg) {
			updatePositions((Account) o);
		}
	};

	public BacktestExecutionManager() {
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#addOrderListener(net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void addOrderListener(IOrderListener listener) {
		orderListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#addOrderListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void addOrderListener(Security security, IOrderListener listener) {
		ListenerList list = (ListenerList) securityOrderListeners.get(security);
		if (list == null) {
			list = new ListenerList(ListenerList.IDENTITY);
			securityOrderListeners.put(security, list);
		}
		list.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#addPositionListener(net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void addPositionListener(IPositionListener listener) {
		positionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#addPositionListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void addPositionListener(Security security, IPositionListener listener) {
		ListenerList list = (ListenerList) securityPositionListeners.get(security);
		if (list == null) {
			list = new ListenerList(ListenerList.IDENTITY);
			securityPositionListeners.put(security, list);
		}
		list.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#createOrder(net.sourceforge.eclipsetrader.ats.core.Signal)
	 */
	public Order createOrder(Signal signal) {
		Order order = new Order();
		order.setDate(currentDate);
		order.setAccount(account);
		order.setProvider(tradingProvider);
		order.setSecurity(signal.getSecurity());
		order.setSide((OrderSide) orderSide.get(signal.getSide()));
		order.setType((OrderType) orderType.get(signal.getType()));
		order.setPrice(signal.getPrice());
		order.setQuantity(signal.getQuantity());
		return order;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#execute(net.sourceforge.eclipsetrader.ats.core.Signal)
	 */
	public void execute(Signal signal) {
		Order order = createOrder(signal);
		order.sendNew();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getPosition(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public int getPosition(Security security) {
		PortfolioPosition position = (PortfolioPosition) positions.get(security);
		return position == null ? 0 : position.getQuantity();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getPositionValue(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public double getPositionValue(Security security) {
		PortfolioPosition position = (PortfolioPosition) positions.get(security);
		return position == null ? 0 : position.getMarketValue();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#removeOrderListener(net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void removeOrderListener(IOrderListener listener) {
		orderListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#removeOrderListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IOrderListener)
	 */
	public void removeOrderListener(Security security, IOrderListener listener) {
		ListenerList list = (ListenerList) securityOrderListeners.get(security);
		if (list != null)
			list.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#removePositionListener(net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void removePositionListener(IPositionListener listener) {
		positionListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#removePositionListener(net.sourceforge.eclipsetrader.core.db.Security, net.sourceforge.eclipsetrader.ats.core.events.IPositionListener)
	 */
	public void removePositionListener(Security security, IPositionListener listener) {
		ListenerList list = (ListenerList) securityPositionListeners.get(security);
		if (list != null)
			list.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#start(net.sourceforge.eclipsetrader.core.db.Account, net.sourceforge.eclipsetrader.core.ITradingProvider)
	 */
	public void start(Account account, ITradingProvider tradingProvider) {
		this.account.setInitialBalance(1000);
		this.account.addObserver(accountObserver);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#stop(net.sourceforge.eclipsetrader.core.db.Account, net.sourceforge.eclipsetrader.core.ITradingProvider)
	 */
	public void stop(Account account, ITradingProvider tradingProvider) {
		Object[] o = orders.keySet().toArray();
		for (int i = 0; i < o.length; i++)
			((Order) o[i]).deleteObserver(orderObserver);

		this.account.deleteObserver(accountObserver);
	}

	OrderEvent createOrderEvent(Order order) {
		OrderEvent e = new OrderEvent();
		e.order = order;
		e.account = order.getAccount();
		e.security = order.getSecurity();
		return e;
	}

	protected void fireOrderSubmitted(Order order) {
		OrderEvent e = createOrderEvent(order);

		ListenerList list = (ListenerList) securityOrderListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IOrderListener) l[i]).orderSubmitted(e);
		}

		Object[] l = orderListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IOrderListener) l[i]).orderSubmitted(e);
	}

	protected void fireOrderStatusChanged(Order order) {
		OrderEvent e = createOrderEvent(order);

		ListenerList list = (ListenerList) securityOrderListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IOrderListener) l[i]).orderStatusChanged(e);
		}

		Object[] l = orderListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IOrderListener) l[i]).orderStatusChanged(e);
	}

	protected void fireOrderCancelled(Order order) {
		OrderEvent e = createOrderEvent(order);

		ListenerList list = (ListenerList) securityOrderListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IOrderListener) l[i]).orderCancelled(e);
		}

		Object[] l = orderListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IOrderListener) l[i]).orderCancelled(e);
	}

	protected void fireOrderFilled(Order order) {
		OrderEvent e = createOrderEvent(order);

		ListenerList list = (ListenerList) securityOrderListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IOrderListener) l[i]).orderFilled(e);
		}

		Object[] l = orderListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IOrderListener) l[i]).orderFilled(e);
	}

	protected void fireOrderRejected(Order order) {
		OrderEvent e = createOrderEvent(order);

		ListenerList list = (ListenerList) securityOrderListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IOrderListener) l[i]).orderRejected(e);
		}

		Object[] l = orderListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IOrderListener) l[i]).orderRejected(e);
	}

	protected void firePositionOpened(PortfolioPosition position) {
		PositionEvent e = new PositionEvent();
		e.date = currentDate;
		e.account = account;
		e.security = position.getSecurity();
		e.position = position;

		ListenerList list = (ListenerList) securityPositionListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IPositionListener) l[i]).positionOpened(e);
		}

		Object[] l = positionListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IPositionListener) l[i]).positionOpened(e);
	}

	protected void firePositionClosed(PortfolioPosition position) {
		PositionEvent e = new PositionEvent();
		e.date = currentDate;
		e.account = account;
		e.security = position.getSecurity();
		e.position = position;

		ListenerList list = (ListenerList) securityPositionListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IPositionListener) l[i]).positionClosed(e);
		}

		Object[] l = positionListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IPositionListener) l[i]).positionClosed(e);
	}

	protected void firePositionChanged(PortfolioPosition position, PortfolioPosition oldPosition) {
		PositionEvent e = new PositionEvent();
		e.date = currentDate;
		e.account = account;
		e.security = position.getSecurity();
		e.position = position;
		e.oldPosition = oldPosition;

		ListenerList list = (ListenerList) securityPositionListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IPositionListener) l[i]).positionChanged(e);
		}

		Object[] l = positionListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IPositionListener) l[i]).positionChanged(e);
	}

	protected void firePositionValueChanged(PortfolioPosition position) {
		PositionEvent e = new PositionEvent();
		e.date = currentDate;
		e.account = account;
		e.security = position.getSecurity();
		e.position = position;

		ListenerList list = (ListenerList) securityPositionListeners.get(e.security);
		if (list != null) {
			Object[] l = list.getListeners();
			for (int i = 0; i < l.length; i++)
				((IPositionListener) l[i]).positionValueChanged(e);
		}

		Object[] l = positionListeners.getListeners();
		for (int i = 0; i < l.length; i++)
			((IPositionListener) l[i]).positionValueChanged(e);
	}

	public void barOpen(BarEvent e) {
		currentDate = e.date;

		Order[] orders = getOrders();
		for (int i = 0; i < orders.length; i++) {
			if (orders[i].getSecurity().equals(e.security) && orders[i].getStatus() == OrderStatus.NEW) {
				if (currentDate.after(orders[i].getDate())) {
					if (orders[i].getType() == OrderType.MARKET) {
						if (orders[i].getSide() == OrderSide.BUY) {
							double amount = orders[i].getQuantity() * e.price;
							if (account.getBalance() < amount) {
								orders[i].setStatus(OrderStatus.REJECTED);
								orders[i].setText("Not enough funds to fill the order");
								orders[i].notifyObservers();
								continue;
							}
						}

						orders[i].setFilledQuantity(orders[i].getQuantity());
						orders[i].setAveragePrice(e.price);
						orders[i].setStatus(OrderStatus.FILLED);
						orders[i].notifyObservers();

						Transaction transaction = new Transaction();
						transaction.setDate(currentDate);
						transaction.setPrice(e.price);
						if (orders[i].getSide() == OrderSide.BUY || orders[i].getSide() == OrderSide.BUYCOVER)
							transaction.setQuantity(orders[i].getFilledQuantity());
						else
							transaction.setQuantity(-orders[i].getFilledQuantity());
						transaction.setSecurity(orders[i].getSecurity());
						account.getTransactions().add(transaction);
						account.notifyObservers();
					}
				}
			}
		}
	}

	public void barClose(BarEvent e) {
		currentDate = e.date;
	}

	void updatePositions(Account account) {
		Map newPositions = new HashMap();
		for (Iterator iter = account.getPortfolio().iterator(); iter.hasNext();) {
			PortfolioPosition p = (PortfolioPosition) iter.next();
			newPositions.put(p.getSecurity(), p);
		}

		Set securities = new HashSet();
		securities.addAll(positions.keySet());
		securities.addAll(newPositions.keySet());

		for (Iterator iter = securities.iterator(); iter.hasNext();) {
			Security security = (Security) iter.next();
			PortfolioPosition oldPosition = (PortfolioPosition) positions.get(security);
			PortfolioPosition newPosition = (PortfolioPosition) newPositions.get(security);

			if (newPosition != null && oldPosition == null) {
				firePositionOpened(newPosition);
				positionValues.put(security, new Double(newPosition.getMarketValue()));
			} else if (newPosition == null && oldPosition != null) {
				firePositionClosed(oldPosition);
				positionValues.remove(security);
			} else if (newPosition != null && oldPosition != null) {
				if (newPosition.getQuantity() != oldPosition.getQuantity())
					firePositionChanged(newPosition, oldPosition);
				else {
					Double oldValue = (Double) positionValues.get(security);
					if (newPosition.getMarketValue() != oldValue.doubleValue()) {
						positionValues.put(security, new Double(newPosition.getMarketValue()));
						firePositionValueChanged(newPosition);
					}
				}
			}
		}

		positions = newPositions;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getOrders()
	 */
	public Order[] getOrders() {
		Collection c = orders.keySet();
		return (Order[]) c.toArray(new Order[c.size()]);
	}

	static {
		orderSide.put(SignalSide.BUY, OrderSide.BUY);
		orderSide.put(SignalSide.SELL, OrderSide.SELL);
		orderSide.put(SignalSide.SELLSHORT, OrderSide.SELLSHORT);
		orderSide.put(SignalSide.BUYCOVER, OrderSide.BUYCOVER);

		orderType.put(SignalType.MARKET, OrderType.MARKET);
		orderType.put(SignalType.LIMIT, OrderType.LIMIT);
		orderType.put(SignalType.STOP, OrderType.STOP);
		orderType.put(SignalType.STOPLIMIT, OrderType.STOPLIMIT);
	}
}
