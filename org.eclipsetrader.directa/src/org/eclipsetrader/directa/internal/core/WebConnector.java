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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.directa.internal.Activator;
import org.eclipsetrader.directa.internal.core.connector.LoginDialog;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class WebConnector {
	private static WebConnector instance;
    private static final String HOST = "www1.directatrading.com";

	private HttpClient client;
	private String userName;
	private String password;

	private String prt = "";
	private String urt = "";
	private String user = "";

	WebConnector() {
	}

	public synchronized static WebConnector getInstance() {
		if (instance == null)
			instance = new WebConnector();
    	return instance;
    }

	public synchronized void login() {
		final IPreferenceStore preferences = getPreferenceStore();
		userName = preferences.getString(Activator.PREFS_USERNAME);
		password = preferences.getString(Activator.PREFS_PASSWORD);

		if (userName.length() == 0 || password.length() == 0) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() : null;
					LoginDialog dlg = new LoginDialog(shell, userName, password);
					if (dlg.open() == LoginDialog.OK) {
						userName = dlg.getUserName();
						password = dlg.getPassword();
						preferences.setValue(Activator.PREFS_USERNAME, userName);
						preferences.setValue(Activator.PREFS_PASSWORD, dlg.isSavePassword() ? password : "");
					}
				}
			});
			if (userName.length() == 0 || password.length() == 0)
				return;
		}

		if (client == null) {
			client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			setupProxy(client, HOST);
		}

		try {
			HttpMethod method = new GetMethod("http://" + HOST + "/trading/collegc_3?USER=" + userName + "&PASSW=" + password + "&PAG=VT4.4.0.0&TAPPO=X");
			method.setFollowRedirects(true);
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
	}

    protected void setupProxy(HttpClient client, String host) {
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
}
