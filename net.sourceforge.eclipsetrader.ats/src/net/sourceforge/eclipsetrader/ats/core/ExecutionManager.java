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

package net.sourceforge.eclipsetrader.ats.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.ats.core.events.IOrderListener;
import net.sourceforge.eclipsetrader.ats.core.events.IPositionListener;
import net.sourceforge.eclipsetrader.ats.core.events.OrderEvent;
import net.sourceforge.eclipsetrader.ats.core.events.PositionEvent;
import net.sourceforge.eclipsetrader.ats.core.internal.NullTradingProvider;
import net.sourceforge.eclipsetrader.core.ITradingProvider;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.core.db.OrderSide;
import net.sourceforge.eclipsetrader.core.db.OrderType;
import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.core.runtime.ListenerList;

/**
 * Default implementation of the IExecutionManager interface.
 */
public class ExecutionManager implements IExecutionManager {
	static Map orderSide = new HashMap();

	static Map orderType = new HashMap();

	ListenerList orderListeners = new ListenerList(ListenerList.IDENTITY);

	Map securityOrderListeners = new HashMap();

	ListenerList positionListeners = new ListenerList(ListenerList.IDENTITY);

	Map securityPositionListeners = new HashMap();

	Account account;

	List orders = new ArrayList();

	/**
	 * The trading provider to use to send trading orders.
	 */
	ITradingProvider tradingProvider = new NullTradingProvider();

	public ExecutionManager() {
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
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#execute(net.sourceforge.eclipsetrader.ats.core.Signal)
	 */
	public void execute(Signal signal) {
		Order order = createOrder(signal);
		order.sendNew();
		orders.add(order);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#createOrder(net.sourceforge.eclipsetrader.ats.core.Signal)
	 */
	public Order createOrder(Signal signal) {
		Order order = new Order();
		order.setProvider(getTradingProvider());
		order.setSecurity(signal.getSecurity());
		order.setSide((OrderSide) orderSide.get(signal.getSide()));
		order.setType((OrderType) orderSide.get(signal.getType()));
		order.setPrice(signal.getPrice());
		order.setQuantity(signal.getQuantity());
		return order;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getPosition(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public int getPosition(Security security) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getPositionValue(net.sourceforge.eclipsetrader.core.db.Security)
	 */
	public double getPositionValue(Security security) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getTradingProvider()
	 */
	public ITradingProvider getTradingProvider() {
		return tradingProvider;
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
		if (tradingProvider != null)
			this.tradingProvider = tradingProvider;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#stop(net.sourceforge.eclipsetrader.core.db.Account, net.sourceforge.eclipsetrader.core.ITradingProvider)
	 */
	public void stop(Account account, ITradingProvider tradingProvider) {
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

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetrader.ats.core.IExecutionManager#getOrders()
	 */
	public Order[] getOrders() {
		return (Order[]) orders.toArray(new Order[orders.size()]);
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
