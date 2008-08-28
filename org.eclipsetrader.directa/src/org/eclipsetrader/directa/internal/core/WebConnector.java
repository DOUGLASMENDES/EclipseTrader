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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.Order;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.connector.LoginDialog;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WebConnector {
	private static WebConnector instance;
	private static final String HOST = "www1.directatrading.com";

	public static final String[] PROPERTIES = new String[] {
		"org.eclipsetrader.directa.symbol",
		"org.eclipsetrader.directaworld.symbol",
		"org.eclipsetrader.borsaitalia.code",
	};

	private HttpClient client;
	private String userName;
	private String password;

	private String prt = "";
	private String urt = "";
	private String user = "";
	Map<String, OrderMonitor> orders = new HashMap<String, OrderMonitor>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private NumberFormat numberFormatter = NumberFormat.getInstance(Locale.ITALY);

	private Log logger = LogFactory.getLog(getClass());

	WebConnector() {
	}

	public synchronized static WebConnector getInstance() {
		if (instance == null)
			instance = new WebConnector();
		return instance;
	}

	public boolean isLoggedIn() {
		return user != null && !"".equals(user);
	}

	public synchronized void login() {
		final IPreferenceStore preferences = getPreferenceStore();
		if (userName == null)
			userName = preferences.getString(Activator.PREFS_USERNAME);
		if (password == null)
			password = preferences.getString(Activator.PREFS_PASSWORD);

		prt = "";
		urt = "";
		user = "";

		do {
			if (userName.length() == 0 || password.length() == 0) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Shell shell = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() : null;
						LoginDialog dlg = new LoginDialog(shell, userName, password);
						if (dlg.open() == LoginDialog.OK) {
							userName = dlg.getUserName();
							password = dlg.getPassword();
							if (dlg.isSavePassword()) {
								preferences.setValue(Activator.PREFS_USERNAME, userName);
								preferences.setValue(Activator.PREFS_PASSWORD, dlg.isSavePassword() ? password : "");
							}
						}
						else {
							userName = null;
							password = null;
						}
					}
				});
				if (userName == null || password == null)
					return;
			}

			if (client == null) {
				client = new HttpClient();
				client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
				setupProxy(client, HOST);
			}

			try {
				HttpMethod method = new GetMethod("http://" + HOST + "/trading/collegc_3");
				method.setFollowRedirects(true);
				method.setQueryString(new NameValuePair[] {
						new NameValuePair("USER", userName),
						new NameValuePair("PASSW", password),
						new NameValuePair("PAG", "VT4.4.0.6"),
						new NameValuePair("TAPPO", "X"),
					});

				logger.info(method.getURI().toString());
				client.executeMethod(method);

				Parser parser = Parser.createParser(method.getResponseBodyAsString(), "");
				NodeList list = parser.extractAllNodesThatMatch(new NodeClassFilter(RemarkNode.class));
				for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();) {
					RemarkNode node = (RemarkNode) iter.nextNode();
					String text = node.getText();
					if (text.startsWith("USER"))
						user = text.substring(4);
					if (text.startsWith("URT"))
						urt = text.substring(3);
					else if (text.startsWith("PRT"))
						prt = text.substring(3);
				}
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error connecting to login server", e);
				Activator.log(status);
				return;
			}

			if (user.equals("") || prt.equals("") || urt.equals(""))
				password = "";

		} while (user.equals("") || prt.equals("") || urt.equals(""));
	}

	private void setupProxy(HttpClient client, String host) {
		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference reference = context.getServiceReference(IProxyService.class.getName());
			if (reference != null) {
				IProxyService proxy = (IProxyService) context.getService(reference);
				IProxyData data = proxy.getProxyDataForHost(host, IProxyData.HTTP_PROXY_TYPE);
				if (data != null) {
					if (data.getHost() != null)
						client.getHostConfiguration().setProxy(data.getHost(), data.getPort());
					if (data.isRequiresAuthentication())
						client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));
				}
				context.ungetService(reference);
			}
		}
	}

	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public String getPrt() {
		return prt;
	}

	public String getUrt() {
		return urt;
	}

	public String getUser() {
		return user;
	}

	public Collection<OrderMonitor> getOrders() {
		return orders.values();
	}

	public void updateOrders() {
		try {
			GetMethod method = new GetMethod("http://" + HOST + "/jscript/ordinij");
			method.setFollowRedirects(true);
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("DSUSER", user),
					new NameValuePair("DSTITO", ""),
					new NameValuePair("DSFUNZ", "2"),
					new NameValuePair("PAG", "VT4.4.0.6"),
					new NameValuePair("TAPPO", "X"),
				});

			logger.info(method.getURI().toString());
			client.executeMethod(method);

			Set<String> set = new HashSet<String>();

			String line = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			while ((line = in.readLine()) != null) {
				logger.trace(line);
				if (line.startsWith("tr01[")) {
					OrderMonitor tracker = parseOrderLine(line);
					orders.put(tracker.getId(), tracker);
					set.add(tracker.getId());
				}
			}

			for (Iterator<OrderMonitor> iter = orders.values().iterator(); iter.hasNext(); ) {
				if (!set.contains(iter.next().getId()))
					iter.remove();
			}

			in.close();
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error updating orders", e);
			Activator.log(status);
		}
	}

	private static final int IDX_SYMBOL = 0;
	private static final int IDX_ID = 1;
	private static final int IDX_STATUS = 2;
	private static final int IDX_DATE = 3;
	private static final int IDX_TIME = 4;
	private static final int IDX_SIDE = 5;
	private static final int IDX_QUANTITY = 6;
	private static final int IDX_PRICE = 7;
	private static final int IDX_FILLED_QUANTITY = 10;
	private static final int IDX_AVERAGE_PRICE = 11;

	protected OrderMonitor parseOrderLine(String line) throws ParseException {
		int sidx = line.indexOf("\"") + 1;
		int eidx = line.indexOf("\"", sidx);
		String[] item = line.substring(sidx, eidx).split(";");

		OrderMonitor tracker = orders.get(item[IDX_ID]);
		if (tracker == null) {
			Order order = new Order(
					null,
					!item[IDX_PRICE].equals("") ? IOrderType.Limit : IOrderType.Market,
					item[IDX_SIDE].equalsIgnoreCase("V") ? IOrderSide.Sell : IOrderSide.Buy,
					getSecurityFromSymbol(item[IDX_SYMBOL]),
					Long.parseLong(item[IDX_QUANTITY]),
					!item[IDX_PRICE].equals("") ? numberFormatter.parse(item[IDX_PRICE]).doubleValue() : null
				);
			tracker = new OrderMonitor(this, BrokerConnector.getInstance(), order);
			tracker.setId(item[IDX_ID]);
		}

		IOrder order = tracker.getOrder();;

		try {
			Method classMethod = order.getClass().getMethod("setDate", Date.class);
			if (classMethod != null)
				classMethod.invoke(order, dateFormatter.parse(item[IDX_DATE] + " " + item[IDX_TIME]));
		} catch(Exception e) {
		}

		if (!item[IDX_AVERAGE_PRICE].equals("")) {
			try {
				tracker.setAveragePrice(numberFormatter.parse(item[IDX_AVERAGE_PRICE]).doubleValue());
			} catch(Exception e) {
			}
		}

		if (!item[IDX_FILLED_QUANTITY].equals("")) {
			try {
				tracker.setFilledQuantity(numberFormatter.parse(item[IDX_FILLED_QUANTITY]).longValue());
			} catch(Exception e) {
			}
		}

		IOrderStatus status = tracker.getStatus();
		if (item[IDX_STATUS].equals("e"))
			status = IOrderStatus.Filled;
		else if (item[IDX_STATUS].equals("n"))
			status = IOrderStatus.PendingNew;
		else if (item[IDX_STATUS].equals("zA"))
			status = IOrderStatus.Canceled;
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

	protected ISecurity getSecurityFromSymbol(String symbol) {
		ISecurity security = null;

		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

				ISecurity[] securities = service.getSecurities();
				for (int i = 0; i < securities.length; i++) {
					String feedSymbol = getSecurityFeedSymbol(securities[i]);
					if (feedSymbol != null && feedSymbol.equals(symbol)) {
						security = securities[i];
						break;
					}
				}

				context.ungetService(serviceReference);
			}
		}

		if (security == null)
			security = new Security(symbol, new FeedIdentifier(symbol, null));

		return security;
	}

	protected String getSecurityFeedSymbol(ISecurity security) {
		IFeedIdentifier identifier = security.getIdentifier();
		if (identifier == null)
			return null;

		String symbol = identifier.getSymbol();
		IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
		if (properties != null) {
			for (int p = 0; p < PROPERTIES.length; p++) {
				if (properties.getProperty(PROPERTIES[p]) != null) {
					symbol = properties.getProperty(PROPERTIES[p]);
					break;
				}
			}
		}

		return symbol;
	}

	public boolean sendOrder(OrderMonitor tracker) {
		boolean ok = false;
		boolean confirm = false;
		String inputLine;

		IOrder order = tracker.getOrder();

		List<NameValuePair> query = new ArrayList<NameValuePair>();
		query.add(new NameValuePair("ACQAZ", order.getSide() == IOrderSide.Buy ? String.valueOf(order.getQuantity()) : ""));
		query.add(new NameValuePair("VENAZ", order.getSide() == IOrderSide.Sell ? String.valueOf(order.getQuantity()) : ""));
		query.add(new NameValuePair("PRZACQ", order.getType() != IOrderType.Market ? numberFormatter.format(order.getPrice()) : ""));
		query.add(new NameValuePair("SCTLX", "immetti Borsa Ita"));
		query.add(new NameValuePair("USER", user));
		query.add(new NameValuePair("GEST", "AZIONARIO"));
		query.add(new NameValuePair("TITO", getSecurityFeedSymbol(order.getSecurity())));
		query.add(new NameValuePair("QPAR", ""));
		if (order.getValidity() == IOrderValidity.GoodTillCancel || order.getValidity() == BrokerConnector.Valid30Days)
			query.add(new NameValuePair("VALID", "M"));
		query.add(new NameValuePair("FAS5", order.getRoute() != null ? order.getRoute().getId() : BrokerConnector.Immediate.getId()));

		// Inserisce l'ordine di acquisto
		try {
			GetMethod method = new GetMethod("http://" + HOST + "/trading/ordimm5c");
			method.setFollowRedirects(true);
			query.add(new NameValuePair("MODO", "C"));
			method.setQueryString(query.toArray(new NameValuePair[query.size()]));

			logger.info(method.getURI().toString());
			client.executeMethod(method);

			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			while ((inputLine = in.readLine()) != null) {
				logger.trace(inputLine);
				if (inputLine.indexOf("VI TRASMETTO L'ORDINE DI") != -1) {
					ok = true;
					confirm = true;
				}
				if (inputLine.indexOf("ORDINE IMMESSO") != -1) {
					ok = true;
					confirm = false;
				}

				if (!confirm) {
					int s = inputLine.indexOf("<i>rif.&nbsp;");
					if (s != -1) {
						s = inputLine.indexOf(">", s + 13) + 1;
						int e = inputLine.indexOf("<", s);
						tracker.setId(inputLine.substring(s, e));
						logger.info(tracker.toString());
					}
				}
			}
			in.close();
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error sending order [" + order.toString() + "]", e);
			Activator.log(status);
		}

		// Se viene richiesta invia anche la conferma d'ordine
		if (ok && confirm) {
			ok = false;

			try {
				GetMethod method = new GetMethod("http://" + HOST + "/trading/ordimm5c");
				method.setFollowRedirects(true);
				query.remove(new NameValuePair("MODO", "C"));
				query.add(new NameValuePair("MODO", "V"));
				method.setQueryString(query.toArray(new NameValuePair[query.size()]));

				logger.info(method.getURI().toString());
				client.executeMethod(method);

				BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
				while ((inputLine = in.readLine()) != null) {
					logger.trace(inputLine);
					if (inputLine.indexOf("ORDINE IMMESSO") != -1)
						ok = true;

					if (ok) {
						int s = inputLine.indexOf("<i>rif.&nbsp;");
						if (s != -1) {
							s = inputLine.indexOf(">", s) + 1;
							int e = inputLine.indexOf("<", s);
							tracker.setId(inputLine.substring(s, e));
							logger.info(tracker.toString());
						}
					}
				}
				in.close();
			} catch (Exception e) {
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error confirming order [" + order.toString() + "]", e);
				Activator.log(status);
			}
		}

		if (tracker.getId() != null)
			orders.put(tracker.getId(), tracker);

		tracker.setStatus(IOrderStatus.PendingNew);

		return ok;
	}

	public boolean cancelOrder(OrderMonitor tracker) {
		boolean ok = false;
		String inputLine;

		try {
			GetMethod method = new GetMethod("http://" + HOST + "/trading/ordmod5c");
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("TAST", "REVOCA"),
					new NameValuePair("USER", user),
					new NameValuePair("RIF", tracker.getId()),
					new NameValuePair("TIPO", "I"),
					new NameValuePair("PRZO", ""),
					new NameValuePair("TITO", getSecurityFeedSymbol(tracker.getOrder().getSecurity())),
					new NameValuePair("FILL", "REVOCA"),
				});

			logger.info(method.getURI().toString());
			client.executeMethod(method);

			BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			while ((inputLine = in.readLine()) != null) {
				logger.trace(inputLine);
				if (inputLine.indexOf("INOLTRATA LA RICHIESTA DI REVOCA") != -1 || inputLine.indexOf("RICH.ANN.") != -1)
					ok = true;
			}
			in.close();
		} catch (Exception e) {
			logger.error(e, e);
		}

		if (ok)
			tracker.setStatus(IOrderStatus.PendingCancel);

		return ok;
	}

	public void importWatchlists() {
		try {
			GetMethod method = new GetMethod("http://" + HOST + "/trading/select");
			method.setFollowRedirects(true);
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("USER", user),
					new NameValuePair("INCR", "N"),
				});

			logger.info(method.getURI().toString());
			client.executeMethod(method);

			Parser parser = Parser.createParser(method.getResponseBodyAsString(), "");
			NodeList list = parser.extractAllNodesThatMatch(new HasAttributeFilter("name", "DEVAR"));
			for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();) {
				Object o = iter.nextNode();
				if (o instanceof SelectTag) {
					OptionTag[] options = ((SelectTag) o).getOptionTags();
					for (int i = 0; i < options.length; i++) {
						if (options[i].getValue().equals("A0") || options[i].getValue().equals("AX"))
							continue;
						System.out.println(options[i].getValue() + " -> " + options[i].getOptionText());
						getWatchlist(options[i].getValue(), options[i].getOptionText());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void getWatchlist(String id, String title) {
		try {
			HttpMethod method = new GetMethod("http://" + HOST + "/trading/tabelc_4");
			method.setFollowRedirects(true);
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("USER", user),
					new NameValuePair("DEVAR", id),
				});

			logger.info(method.getURI().toString());
			client.executeMethod(method);

			Parser parser = Parser.createParser(method.getResponseBodyAsString(), "");
			NodeList list = parser.extractAllNodesThatMatch(new NodeClassFilter(TableRow.class));
			for (SimpleNodeIterator iter = list.elements(); iter.hasMoreNodes();) {
				TableRow row = (TableRow) iter.nextNode();
				if (row.getChildCount() == 23) {
					if (row.getChild(1) instanceof TableHeader)
						continue;

					String symbol = "";
					String isin = "";
					String description = "";

					LinkTag link = (LinkTag) ((TableColumn) row.getChild(1)).getChild(1);
					int s = link.getText().indexOf("TITO=");
					if (s != -1) {
						s += 5;
						int e = link.getText().indexOf("&", s);
						if (e == -1)
							e = link.getText().length();
						symbol = link.getText().substring(s, e);
					}
					description = link.getFirstChild().getText();
					description = description.replaceAll("[\r\n]", " ").trim();

					link = (LinkTag) ((TableColumn) row.getChild(5)).getChild(0);
					s = link.getText().indexOf("tlv=");
					if (s != -1) {
						s += 4;
						int e = link.getText().indexOf("&", s);
						if (e == -1)
							e = link.getText().length();
						isin = link.getText().substring(s, e);
					}

					System.out.println(symbol + " " + isin + " (" + description + ")");
				}
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}
}
