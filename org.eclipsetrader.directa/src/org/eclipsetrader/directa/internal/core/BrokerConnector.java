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

package org.eclipsetrader.directa.internal.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.directa.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BrokerConnector implements IBroker, IExecutableExtension, IExecutableExtensionFactory, Runnable {
	public static final IOrderRoute Immediate = new OrderRoute("1", "immed");
	public static final IOrderRoute MTA = new OrderRoute("2", "MTA");
	public static final IOrderRoute CloseMTA = new OrderRoute("4", "clos-MTA");
	public static final IOrderRoute AfterHours = new OrderRoute("5", "AfterHours");
	public static final IOrderRoute Open = new OrderRoute("7", "open//");

	public static final IOrderValidity Valid30Days = new OrderValidity("30days", "30 Days");

	private static BrokerConnector instance;

	private String id;
	private String name;
	private String server = "213.92.13.4";
	private int port = 1080;

	Set<OrderMonitor> orders = new HashSet<OrderMonitor>();
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private SocketChannel socketChannel;
	private Thread thread;
	private Log logger = LogFactory.getLog(getClass());

	public BrokerConnector() {
	}

	public static BrokerConnector getInstance() {
		if (instance == null)
			instance = new BrokerConnector();
		return instance;
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
	 * @see org.eclipsetrader.core.trading.IBroker#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#connect()
     */
    public void connect() {
    	if ("".equals(WebConnector.getInstance().getUser()))
    		WebConnector.getInstance().login();

    	if (thread == null || !thread.isAlive()) {
    		thread = new Thread(this, getName() + " - Orders Monitor");
        	logger.info("Starting " + thread.getName());
        	thread.start();
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#disconnect()
     */
    public void disconnect() {
    	if (thread != null) {
    		try {
    			if (socketChannel != null)
    				socketChannel.close();
            } catch (IOException e) {
	            // Do nothing
            }
    		try {
    			thread.interrupt();
	            thread.join(30 * 1000);
            } catch (InterruptedException e) {
	            // Do nothing
            }
        	logger.info("Stopped " + thread.getName());
    		thread = null;
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#canTrade(org.eclipsetrader.core.instruments.ISecurity)
     */
    public boolean canTrade(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null)
			return false;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			for (int p = 0; p < WebConnector.PROPERTIES.length; p++) {
				if (properties.getProperty(WebConnector.PROPERTIES[p]) != null)
					return true;
			}
		}

	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
     */
    public String getSymbolFromSecurity(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null)
			return null;

		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			for (int p = 0; p < WebConnector.PROPERTIES.length; p++) {
				if (properties.getProperty(WebConnector.PROPERTIES[p]) != null)
					return properties.getProperty(WebConnector.PROPERTIES[p]);
			}
		}

		return null;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getSecurityFromSymbol(java.lang.String)
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
					String feedSymbol = getSymbolFromSecurity(securities[i]);
					if (feedSymbol != null && feedSymbol.equals(symbol)) {
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
     * @see org.eclipsetrader.core.trading.IBroker#prepareOrder(org.eclipsetrader.core.trading.IOrder)
     */
    public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
		if (order.getType() != IOrderType.Limit && order.getType() != IOrderType.Market)
			throw new BrokerException("Invalid order type, must be Limit or Market");
		if (order.getSide() != IOrderSide.Buy && order.getSide() != IOrderSide.Sell)
			throw new BrokerException("Invalid order side, must be Buy or Sell");
		if (order.getValidity() != IOrderValidity.Day && order.getValidity() != Valid30Days)
			throw new BrokerException("Invalid order validity, must be Day or 30 Days");

		return new OrderMonitor(WebConnector.getInstance(), this, order);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedSides()
	 */
	public IOrderSide[] getAllowedSides() {
		return new IOrderSide[] {
				IOrderSide.Buy,
				IOrderSide.Sell,
			};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedTypes()
	 */
	public IOrderType[] getAllowedTypes() {
		return new IOrderType[] {
				IOrderType.Limit,
				IOrderType.Market,
			};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getAllowedValidity()
	 */
	public IOrderValidity[] getAllowedValidity() {
		return new IOrderValidity[] {
				IOrderValidity.Day,
				Valid30Days,
			};
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#getAllowedRoutes()
     */
    public IOrderRoute[] getAllowedRoutes() {
	    return new IOrderRoute[] {
    			BrokerConnector.Immediate,
    			BrokerConnector.MTA,
    			BrokerConnector.CloseMTA,
    			BrokerConnector.Open,
    			BrokerConnector.AfterHours,
    		};
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getOrders()
	 */
	public IOrderMonitor[] getOrders() {
		synchronized(orders) {
			return orders.toArray(new IOrderMonitor[orders.size()]);
		}
	}

	private static final String LOGIN = "21";
	private static final String UNKNOWN55 = "55";
	private static final String UNKNOWN70 = "70";
	private static final String HEARTBEAT = "40";

	/* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	Selector socketSelector;
		ByteBuffer dst = ByteBuffer.wrap(new byte[2048]);

		try {
	    	// Create a non-blocking socket channel
	        socketChannel = SocketChannel.open();
	        socketChannel.configureBlocking(false);

	        socketChannel.socket().setReceiveBufferSize(32768);
	        socketChannel.socket().setSoLinger(true, 1);
	        socketChannel.socket().setSoTimeout(0x15f90);
	        socketChannel.socket().setReuseAddress(true);

	        // Kick off connection establishment
	        socketChannel.connect(new InetSocketAddress(server, port));

	        // Create a new selector
	        socketSelector = SelectorProvider.provider().openSelector();

	        // Register the server socket channel, indicating an interest in
	        // accepting new connections
	        socketChannel.register(socketSelector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
        } catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to orders monitor", e); //$NON-NLS-1$
			Activator.log(status);
			return;
        }

        for (;;) {
    		try {
            	if (socketSelector.select(30 * 1000) == 0) {
                   	logger.trace(">" + HEARTBEAT);
    				socketChannel.write(ByteBuffer.wrap(new String(HEARTBEAT + "\r\n").getBytes()));
            	}
            } catch (Exception e) {
            	break;
            }

			// Iterate over the set of keys for which events are available
			Iterator<SelectionKey> selectedKeys = socketSelector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid()) {
					continue;
				}

				try {
					// Check what event is available and deal with it
					if (key.isConnectable()) {
						// Finish the connection. If the connection operation failed
						// this will raise an IOException.
						try {
							socketChannel.finishConnect();
						} catch (IOException e) {
							// Cancel the channel's registration with our selector
							key.cancel();
							return;
						}

						// Register an interest in writing on this channel
						key.interestOps(SelectionKey.OP_WRITE);
					}
					if (key.isWritable()) {
			        	logger.info(">" + LOGIN + WebConnector.getInstance().getUser());
						socketChannel.write(ByteBuffer.wrap(new String(LOGIN + WebConnector.getInstance().getUser() + "\r\n").getBytes()));

						// Register an interest in reading on this channel
						key.interestOps(SelectionKey.OP_READ);
					}
					if (key.isReadable()) {
						dst.clear();
						int readed = socketChannel.read(dst);
						if (readed > 0) {
							String[] s = new String(dst.array(), 0, readed).split("\r\n");
							for (int i = 0; i < s.length; i++) {
								logger.info("<" + s[i]);

								if (s[i].endsWith(";" + WebConnector.getInstance().getUser() + ";")) {
									logger.info(">" + UNKNOWN70);
									socketChannel.write(ByteBuffer.wrap(new String(UNKNOWN70 + "\r\n").getBytes()));
						        	logger.info(">" + UNKNOWN55);
									socketChannel.write(ByteBuffer.wrap(new String(UNKNOWN55 + "\r\n").getBytes()));
								}

								if (s[i].indexOf(";6;5;") != -1 || s[i].indexOf(";8;0;") != -1) {
		        		        	try {
		        	                    OrderMonitor monitor = parseOrderLine(s[i]);

		        	                    OrderDelta[] delta;
		        	                    synchronized(orders) {
			        	    	        	if (!orders.contains(monitor)) {
			        	    	        		orders.add(monitor);
			        	    	    			delta = new OrderDelta[] { new OrderDelta(OrderDelta.KIND_ADDED, monitor) };
			        	    	        	}
			        	    	        	else
			        	    	    			delta = new OrderDelta[] { new OrderDelta(OrderDelta.KIND_UPDATED, monitor) };
		        	                    }
	        	    	    			fireUpdateNotifications(delta);
		        		        	} catch (ParseException e) {
		        		    			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error parsing line: " + s[i], e); //$NON-NLS-1$
		        		    			Activator.log(status);
		                            }
		        		        }
							}
						}
					}
	            } catch (Exception e) {
	    			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Connection error", e); //$NON-NLS-1$
	    			Activator.log(status);
	            }
			}
        }
    }

	private static final int IDX_ID = 3;
	private static final int IDX_STATUS = 4;
	private static final int IDX_SYMBOL = 5;
	private static final int IDX_AVERAGE_PRICE = 10;
	private static final int IDX_QUANTITY = 15;
	private static final int IDX_PRICE = 16;
	private static final int IDX_SIDE = 18;
	private static final int IDX_DATE = 19;
	private static final int IDX_TIME = 20;
	private static final int IDX_FILLED_QUANTITY = 25;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HHmmss");
	private NumberFormat numberFormatter = NumberFormat.getInstance(Locale.US);

	protected OrderMonitor parseOrderLine(String line) throws ParseException {
		String[] item = line.split(";");

		OrderMonitor tracker = null;
		synchronized(orders) {
			for (OrderMonitor m : orders) {
				if (item[IDX_ID].equals(m.getId())) {
					tracker = m;
					break;
				}
			}
			if (tracker == null) {
				for (OrderMonitor m : orders) {
					if (m.getId() == null && getSymbolFromSecurity(m.getOrder().getSecurity()).equals(item[IDX_SYMBOL])) {
						tracker = m;
						tracker.setId(item[IDX_ID]);
						break;
					}
				}
			}
		}
		if (tracker == null) {
			Long quantity = !item[IDX_QUANTITY].equals("") ? Long.parseLong(item[IDX_QUANTITY]) : null;
			if (quantity == null && item.length > IDX_FILLED_QUANTITY && !item[IDX_FILLED_QUANTITY].equals("")) {
				try {
					quantity = numberFormatter.parse(item[IDX_FILLED_QUANTITY]).longValue();
				} catch(Exception e) {
				}
			}
			Order order = new Order(
					null,
					!item[IDX_PRICE].equals("") ? IOrderType.Limit : IOrderType.Market,
					item[IDX_SIDE].equalsIgnoreCase("V") ? IOrderSide.Sell : IOrderSide.Buy,
					getSecurityFromSymbol(item[IDX_SYMBOL]),
					quantity,
					!item[IDX_PRICE].equals("") ? numberFormatter.parse(item[IDX_PRICE]).doubleValue() : null
				);
			tracker = new OrderMonitor(WebConnector.getInstance(), BrokerConnector.getInstance(), order);
			tracker.setId(item[IDX_ID]);
		}

		IOrder order = tracker.getOrder();;

		try {
			Method classMethod = order.getClass().getMethod("setDate", Date.class);
			if (classMethod != null) {
				if (item[IDX_TIME].length() < 6)
					item[IDX_TIME] = "0" + item[IDX_TIME];
				classMethod.invoke(order, dateFormatter.parse(item[IDX_DATE] + " " + item[IDX_TIME]));
			}
		} catch(Exception e) {
		}

		if (item[IDX_STATUS].equals("e") || item[IDX_STATUS].equals("e ")) {
			if (!item[IDX_AVERAGE_PRICE].equals("")) {
				try {
					tracker.setAveragePrice(numberFormatter.parse(item[IDX_AVERAGE_PRICE]).doubleValue());
				} catch(Exception e) {
				}
			}
			else
				tracker.setAveragePrice(numberFormatter.parse(item[IDX_PRICE]).doubleValue());

			if (!item[IDX_FILLED_QUANTITY].equals("")) {
				try {
					tracker.setFilledQuantity(numberFormatter.parse(item[IDX_FILLED_QUANTITY]).longValue());
				} catch(Exception e) {
				}
			}
		}

		IOrderStatus status = tracker.getStatus();
		if (item[IDX_STATUS].equals("e") || item[IDX_STATUS].equals("e "))
			status = IOrderStatus.Filled;
		else if (item[IDX_STATUS].equals("n") || item[IDX_STATUS].equals("n ") || item[IDX_STATUS].equals("j"))
			status = IOrderStatus.PendingNew;
		else if (item[IDX_STATUS].equals("zA") || item[IDX_STATUS].equals("z "))
			status = IOrderStatus.Canceled;
		else if (item[IDX_STATUS].equals("na"))
			status = IOrderStatus.PendingCancel;
		else
			status = IOrderStatus.PendingNew;

		if (status != IOrderStatus.Canceled) {
			if (tracker.getFilledQuantity() != null && !tracker.getFilledQuantity().equals(order.getQuantity()))
				status = IOrderStatus.Partial;
		}

		if ((status == IOrderStatus.Filled || status == IOrderStatus.Canceled || status == IOrderStatus.Rejected) && tracker.getStatus() != status) {
			tracker.setStatus(status);
			tracker.fireOrderCompletedEvent();
		}
		else
			tracker.setStatus(status);

		return tracker;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    public void addOrderChangeListener(IOrderChangeListener listener) {
		listeners.add(listener);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
     */
    public void removeOrderChangeListener(IOrderChangeListener listener) {
		listeners.remove(listener);
    }

    protected void fireUpdateNotifications(OrderDelta[] deltas) {
    	if (deltas.length != 0) {
    		OrderChangeEvent event = new OrderChangeEvent(this, deltas);
        	Object[] l = listeners.getListeners();
    		for (int i = 0; i < l.length; i++) {
    			try {
    				((IOrderChangeListener) l[i]).orderChanged(event);
    			} catch(Throwable e) {
        			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", e); //$NON-NLS-1$
        			Activator.log(status);
    			}
    		}
    	}
    }

	public void addWithNotification(OrderMonitor orderMonitor) {
		synchronized(orders) {
			if (!orders.contains(orderMonitor))
				orders.add(orderMonitor);
		}
		fireUpdateNotifications(new OrderDelta[] {
				new OrderDelta(OrderDelta.KIND_ADDED, orderMonitor),
			});
    }
}
