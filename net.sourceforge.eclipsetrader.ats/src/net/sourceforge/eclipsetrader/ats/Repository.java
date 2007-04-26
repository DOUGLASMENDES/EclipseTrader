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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.ats.core.db.Component;
import net.sourceforge.eclipsetrader.ats.core.db.Strategy;
import net.sourceforge.eclipsetrader.ats.core.db.TradingSystem;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.ObservableList;
import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.DefaultAccount;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Repository {
	Integer nextId = new Integer(1);

	ObservableList systems = new ObservableList();

	private Log log = LogFactory.getLog(getClass());

	private ErrorHandler errorHandler = new ErrorHandler() {

		public void error(SAXParseException exception) throws SAXException {
			log.error(exception, exception);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			log.fatal(exception, exception);
		}

		public void warning(SAXParseException exception) throws SAXException {
			log.warn(exception, exception);
		}
	};

	public Repository() {
	}

	public void clear() {
		nextId = new Integer(1);
		systems = new ObservableList();
	}

	public List allTradingSystems() {
		return Collections.unmodifiableList(systems);
	}

	public void addTradingSystemsCollectionObserver(ICollectionObserver observer) {
		systems.addCollectionObserver(observer);
	}

	public void removeTradingSystemsCollectionObserver(ICollectionObserver observer) {
		systems.removeCollectionObserver(observer);
	}

	public void save(TradingSystem system) {
		if (system.getId() == null) {
			system.setId(nextId);
			nextId = new Integer(nextId.intValue() + 1);
		}

		if (!systems.contains(system))
			systems.add(system);

		for (Iterator iter = system.getStrategies().iterator(); iter.hasNext();) {
			Strategy strategy = (Strategy) iter.next();
			if (strategy.getId() == null) {
				strategy.setId(nextId);
				nextId = new Integer(nextId.intValue() + 1);
			}
			if (strategy.getMarketManager() != null && strategy.getMarketManager().getId() == null) {
				strategy.getMarketManager().setId(nextId);
				nextId = new Integer(nextId.intValue() + 1);
			}
		}

		system.notifyObservers();
	}

	public void delete(TradingSystem system) {
		systems.remove(system);
	}

	public void load() {
		File file = new File(Platform.getLocation().toFile(), "systems.xml"); //$NON-NLS-1$
		if (file.exists() == true) {
			log.info("Loading trading systems");
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setErrorHandler(errorHandler);
				Document document = builder.parse(file);

				Node firstNode = document.getFirstChild();
				nextId = new Integer(firstNode.getAttributes().getNamedItem("nextId").getNodeValue()); //$NON-NLS-1$

				NodeList childNodes = firstNode.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					String nodeName = item.getNodeName();
					if (nodeName.equalsIgnoreCase("system")) //$NON-NLS-1$
					{
						TradingSystem system = loadTradingSystem(item.getChildNodes());
						if (system != null)
							systems.add(system);
					}
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}

	TradingSystem loadTradingSystem(NodeList node) {
		TradingSystem obj = new TradingSystem(new Integer(Integer.parseInt(((Node) node).getAttributes().getNamedItem("id").getNodeValue())));

		for (int i = 0; i < node.getLength(); i++) {
			Node item = node.item(i);
			String nodeName = item.getNodeName();
			Node value = item.getFirstChild();
			if (value != null) {
				if (nodeName.equals("name")) //$NON-NLS-1$
					obj.setName(value.getNodeValue());
				else if (nodeName.equals("account")) //$NON-NLS-1$
				{
					Integer id = new Integer(value.getNodeValue());
					Account account = (Account) CorePlugin.getRepository().load(Account.class, id);
					if (account == null) {
						log.warn("Unknown account (id=" + id + ")");
						account = new DefaultAccount(id);
					}
					obj.setAccount(account);
				} else if (nodeName.equals("tradingProvider")) //$NON-NLS-1$
					obj.setTradingProviderId(value.getNodeValue());
			}
			if (nodeName.equals("moneyManager")) //$NON-NLS-1$
				obj.setMoneyManager(loadComponent(item.getChildNodes()));
			else if (nodeName.equalsIgnoreCase("param") == true) {
				NamedNodeMap map = item.getAttributes();
				obj.getParams().put(map.getNamedItem("key").getNodeValue(), map.getNamedItem("value").getNodeValue());
			}
			if (nodeName.equals("strategy")) //$NON-NLS-1$
				obj.addStrategy(loadStrategy(item.getChildNodes()));
		}

		obj.clearChanged();

		return obj;
	}

	public void save() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			builder.setErrorHandler(errorHandler);
			Document document = builder.getDOMImplementation().createDocument(null, "data", null);

			Element root = document.getDocumentElement();
			root.setAttribute("nextId", String.valueOf(nextId));

			for (Iterator iter = systems.iterator(); iter.hasNext();) {
				TradingSystem system = (TradingSystem) iter.next();

				Element element = document.createElement("system");
				element.setAttribute("id", String.valueOf(system.getId()));

				Element node = document.createElement("name");
				node.appendChild(document.createTextNode(system.getName()));
				element.appendChild(node);

				if (system.getAccount().getId() != null) {
					node = document.createElement("account");
					node.appendChild(document.createTextNode(String.valueOf(system.getAccount().getId())));
					element.appendChild(node);
				}

				node = document.createElement("tradingProvider");
				node.appendChild(document.createTextNode(system.getTradingProviderId()));
				element.appendChild(node);

				if (system.getMoneyManager() != null) {
					node = document.createElement("moneyManager");
					saveComponent(system.getMoneyManager(), document, node);
					element.appendChild(node);
				}

				for (Iterator paramIter = system.getParams().keySet().iterator(); paramIter.hasNext();) {
					String key = (String) paramIter.next();
					node = document.createElement("param");
					node.setAttribute("key", key);
					node.setAttribute("value", (String) system.getParams().get(key));
					element.appendChild(node);
				}

				for (Iterator iter2 = system.getStrategies().iterator(); iter2.hasNext();)
					saveStrategy((Strategy) iter2.next(), document, element);

				root.appendChild(element);
			}

			saveDocument(document, "", "systems.xml");

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	Strategy loadStrategy(NodeList node) {
		NamedNodeMap map = ((Node) node).getAttributes();
		Strategy obj = new Strategy(new Integer(map.getNamedItem("id").getNodeValue()));
		if (map.getNamedItem("pluginId") != null)
			obj.setPluginId(map.getNamedItem("pluginId").getNodeValue());
		if (map.getNamedItem("autostart") != null)
			obj.setAutoStart(map.getNamedItem("autostart").getNodeValue().equals("true"));

		for (int i = 0; i < node.getLength(); i++) {
			Node item = node.item(i);
			String nodeName = item.getNodeName();
			Node value = item.getFirstChild();
			if (value != null) {
				if (nodeName.equals("name")) //$NON-NLS-1$
					obj.setName(value.getNodeValue());
			}
			if (nodeName.equals("marketManager")) //$NON-NLS-1$
				obj.setMarketManager(loadComponent(item.getChildNodes()));
			else if (nodeName.equals("entry")) //$NON-NLS-1$
				obj.setEntry(loadComponent(item.getChildNodes()));
			else if (nodeName.equals("exit")) //$NON-NLS-1$
				obj.setExit(loadComponent(item.getChildNodes()));
			else if (nodeName.equals("moneyManager")) //$NON-NLS-1$
				obj.setMoneyManager(loadComponent(item.getChildNodes()));
			else if (nodeName.equalsIgnoreCase("security") == true) {
				map = item.getAttributes();
				Integer id = new Integer(map.getNamedItem("id").getNodeValue());
				Security security = (Security) CorePlugin.getRepository().load(Security.class, id);
				if (security == null)
					log.warn("Unknown security (id=" + id + ")");
				else
					obj.addSecurity(security);
			}
			if (nodeName.equalsIgnoreCase("param") == true) {
				map = item.getAttributes();
				obj.getParams().put(map.getNamedItem("key").getNodeValue(), map.getNamedItem("value").getNodeValue());
			}
		}

		obj.clearChanged();

		return obj;
	}

	void saveStrategy(Strategy obj, Document document, Element root) {
		Element element = document.createElement("strategy");
		element.setAttribute("id", String.valueOf(obj.getId()));
		if (obj.getPluginId() != null)
			element.setAttribute("pluginId", obj.getPluginId());
		element.setAttribute("autostart", obj.isAutoStart() ? "true" : "false");
		root.appendChild(element);

		if (obj.getName() != null) {
			Element node = document.createElement("name");
			node.appendChild(document.createTextNode(obj.getName()));
			element.appendChild(node);
		}

		if (obj.getMarketManager() != null) {
			Element node = document.createElement("marketManager");
			saveComponent(obj.getMarketManager(), document, node);
			element.appendChild(node);
		}
		if (obj.getEntry() != null) {
			Element node = document.createElement("entry");
			saveComponent(obj.getEntry(), document, node);
			element.appendChild(node);
		}
		if (obj.getExit() != null) {
			Element node = document.createElement("exit");
			saveComponent(obj.getExit(), document, node);
			element.appendChild(node);
		}
		if (obj.getMoneyManager() != null) {
			Element node = document.createElement("moneyManager");
			saveComponent(obj.getMoneyManager(), document, node);
			element.appendChild(node);
		}

		for (Iterator iter = obj.getSecurities().iterator(); iter.hasNext();) {
			Element node = document.createElement("security");
			node.setAttribute("id", String.valueOf(((Security) iter.next()).getId()));
			element.appendChild(node);
		}

		for (Iterator paramIter = obj.getParams().keySet().iterator(); paramIter.hasNext();) {
			String key = (String) paramIter.next();
			Element node = document.createElement("param");
			node.setAttribute("key", key);
			node.setAttribute("value", (String) obj.getParams().get(key));
			element.appendChild(node);
		}
	}

	Component loadComponent(NodeList node) {
		NamedNodeMap map = ((Node) node).getAttributes();
		Component obj = new Component(new Integer(map.getNamedItem("id").getNodeValue()));
		obj.setPluginId(map.getNamedItem("pluginId").getNodeValue());

		for (int i = 0; i < node.getLength(); i++) {
			Node item = node.item(i);
			String nodeName = item.getNodeName();
			Node value = item.getFirstChild();
			if (value != null) {
			}
			if (nodeName.equalsIgnoreCase("param") == true) {
				map = item.getAttributes();
				obj.getPreferences().setValue(map.getNamedItem("key").getNodeValue(), map.getNamedItem("value").getNodeValue());
			}
		}

		obj.clearChanged();

		return obj;
	}

	void saveComponent(Component obj, Document document, Element element) {
		element.setAttribute("id", String.valueOf(obj.getId()));
		element.setAttribute("pluginId", String.valueOf(obj.getPluginId()));

		String[] keys = obj.getPreferences().preferenceNames();
		for (int i = 0; i < keys.length; i++) {
			Element node = document.createElement("param");
			node.setAttribute("key", keys[i]);
			node.setAttribute("value", (String) obj.getPreferences().getString(keys[i]));
			element.appendChild(node);
		}
	}

	protected void saveDocument(Document document, String path, String name) {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			try {
				factory.setAttribute("indent-number", new Integer(4));
			} catch (Exception e) {
			}
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(document);

			File file = new File(Platform.getLocation().toFile(), path);
			file.mkdirs();
			file = new File(file, name);

			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
			out.flush();
			out.close();
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}
}
