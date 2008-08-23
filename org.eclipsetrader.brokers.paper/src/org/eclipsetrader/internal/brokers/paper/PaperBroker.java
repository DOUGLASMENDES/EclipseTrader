/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.internal.brokers.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PaperBroker implements IBroker, IExecutableExtension, IExecutableExtensionFactory {
	private static PaperBroker instance;

	private String id;
	private String name;
	private IPricingEnvironment pricingEnvironment;

	private List<OrderMonitor> pendingOrders = new ArrayList<OrderMonitor>();

	private IPricingListener pricingListener = new IPricingListener() {
        public void pricingUpdate(PricingEvent event) {
        	for (PricingDelta delta : event.getDelta()) {
        		if (delta.getNewValue() instanceof ITrade)
        			processTrade(event.getSecurity(), (ITrade) delta.getNewValue());
        	}
        }
	};

	public PaperBroker() {
	}

	public PaperBroker(IPricingEnvironment pricingEnvironment) {
	    this.pricingEnvironment = pricingEnvironment;
    }

	public static PaperBroker getInstance() {
		if (instance == null)
			instance = new PaperBroker();
    	return instance;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
     */
    public Object create() throws CoreException {
		if (instance == null)
			instance = this;
    	return instance;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#connect()
	 */
	public void connect() {
		if (pricingEnvironment == null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
			if (serviceReference != null) {
				IMarketService marketService = (IMarketService) context.getService(serviceReference);
				pricingEnvironment = marketService.getPricingEnvironment();
				context.ungetService(serviceReference);
			}
		}
		if (pricingEnvironment != null)
			pricingEnvironment.addPricingListener(pricingListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#disconnect()
	 */
	public void disconnect() {
		if (pricingEnvironment != null)
			pricingEnvironment.removePricingListener(pricingListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#canTrade(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public boolean canTrade(ISecurity security) {
		return true;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getSecurityFromSymbol(java.lang.String)
     */
    public ISecurity getSecurityFromSymbol(String symbol) {
		ISecurity security = null;

		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

				ISecurity[] securities = service.getSecurities();
				for (int i = 0; i < securities.length; i++) {
					IFeedIdentifier identifier = securities[i].getIdentifier();
					if (identifier != null && symbol.equals(identifier.getSymbol())) {
						security = securities[i];
						break;
					}
				}

				context.ungetService(serviceReference);
			}
		}

		if (security == null)
			security = new Security("Unknown", new FeedIdentifier(symbol, null));

		return security;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedSides()
	 */
	public IOrderSide[] getAllowedSides() {
		return new IOrderSide[] {
				IOrderSide.Buy,
				IOrderSide.Sell,
			};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedTypes()
	 */
	public IOrderType[] getAllowedTypes() {
		return new IOrderType[] {
				IOrderType.Limit,
				IOrderType.Market,
			};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedValidity()
	 */
	public IOrderValidity[] getAllowedValidity() {
		return new IOrderValidity[] {
				IOrderValidity.Day,
			};
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedRoutes()
     */
    public IOrderRoute[] getAllowedRoutes() {
	    return new IOrderRoute[0];
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getOrders()
	 */
	public IOrderMonitor[] getOrders() {
		synchronized(pendingOrders) {
			return pendingOrders.toArray(new IOrderMonitor[pendingOrders.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#prepareOrder(org.eclipsetrader.core.trading.IOrder)
	 */
	public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
		if (order.getAccount() != null && !(order.getAccount() instanceof Account))
			throw new BrokerException("Invalid account");

		OrderMonitor monitor = new OrderMonitor(this, order) {
            @Override
            public void cancel() throws BrokerException {
				synchronized(pendingOrders) {
					pendingOrders.remove(this);
				}
            	setStatus(IOrderStatus.Canceled);
            }

            @Override
            public void submit() throws BrokerException {
        		synchronized(pendingOrders) {
        			pendingOrders.add(this);
        		}
				setId(Long.toHexString(UUID.randomUUID().getLeastSignificantBits()));
            	setStatus(IOrderStatus.PendingNew);
            }
		};
		return monitor;
	}

	protected void processTrade(ISecurity security, ITrade trade) {
		OrderMonitor[] monitors;
		synchronized(pendingOrders) {
			monitors = pendingOrders.toArray(new OrderMonitor[pendingOrders.size()]);
		}
		for (int i = 0; i < monitors.length; i++) {
			IOrder order = monitors[i].getOrder();
			if (order.getSecurity() == security) {
				if (order.getType() == IOrderType.Market ||
				   (order.getType() == IOrderType.Limit &&
						   ((order.getSide() == IOrderSide.Buy && trade.getPrice() <= order.getPrice()) ||
							(order.getSide() == IOrderSide.Sell && trade.getPrice() >= order.getPrice())))) {

					double totalPrice = monitors[i].getFilledQuantity() != null ? monitors[i].getFilledQuantity() * monitors[i].getAveragePrice() : 0.0;
					long filledQuantity = monitors[i].getFilledQuantity() != null ? monitors[i].getFilledQuantity() : 0L;
					long remainQuantity = order.getQuantity() - filledQuantity;

					long quantity = trade.getSize() != null && trade.getSize() < remainQuantity ? trade.getSize() : remainQuantity;
					filledQuantity += quantity;
					totalPrice += quantity * trade.getPrice();

					monitors[i].setFilledQuantity(filledQuantity);
					monitors[i].setAveragePrice(totalPrice / filledQuantity);

					if (quantity != 0)
						monitors[i].addTransaction(new StockTransaction(monitors[i].getOrder().getSecurity(), quantity, trade.getPrice()));

					if (monitors[i].getFilledQuantity() == order.getQuantity()) {
						monitors[i].setStatus(IOrderStatus.Filled);
						monitors[i].fireOrderCompletedEvent();
						synchronized(pendingOrders) {
							pendingOrders.remove(monitors[i]);
						}

						Account account = (Account) monitors[i].getOrder().getAccount();
						if (account != null)
							account.processCompletedOrder(monitors[i]);
					}
					else
						monitors[i].setStatus(IOrderStatus.Partial);
				}
			}
		}
    }
}
