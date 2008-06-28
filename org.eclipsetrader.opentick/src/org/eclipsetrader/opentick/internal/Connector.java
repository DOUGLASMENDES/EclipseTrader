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

package org.eclipsetrader.opentick.internal;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipsetrader.opentick.internal.core.LoginDialog;
import org.otfeed.IConnection;
import org.otfeed.OTConnectionFactory;
import org.otfeed.event.IConnectionStateListener;
import org.otfeed.event.OTError;
import org.otfeed.event.OTHost;

public class Connector {
	private static Connector instance;

	private String userName;
	private String password;
	private IConnection connection;

	Connector() {
		instance = this;
	}

	public static Connector getInstance() {
    	return instance;
    }

	public IConnection getConnection() {
    	return connection;
    }

	public synchronized void connect() {
		if (connection == null) {
			final IPreferenceStore preferences = getPreferenceStore();
			userName = preferences.getString(OTActivator.PREFS_USERNAME);
			password = preferences.getString(OTActivator.PREFS_PASSWORD);

			if (userName.length() == 0 || password.length() == 0) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Shell shell = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() : null;
						LoginDialog dlg = new LoginDialog(shell, userName, password);
						if (dlg.open() == LoginDialog.OK) {
							userName = dlg.getUserName();
							password = dlg.getPassword();
							preferences.setValue(OTActivator.PREFS_USERNAME, userName);
							preferences.setValue(OTActivator.PREFS_PASSWORD, dlg.isSavePassword() ? password : "");
						}
					}
				});
				if (userName.length() == 0 || password.length() == 0)
					return;
			}

			OTConnectionFactory factory = new OTConnectionFactory();
			factory.getHostList().add(new OTHost(preferences.getString(OTActivator.PREFS_SERVER), preferences.getInt(OTActivator.PREFS_PORT)));
			factory.setUsername(userName);
			factory.setPassword(password);
			connection = factory.connect(new IConnectionStateListener() {
				public void onConnected() {
				}

				public void onConnecting(OTHost host) {
				}

				public void onError(OTError error) {
					Status status = new Status(Status.ERROR, OTActivator.PLUGIN_ID, 0, error.toString(), null);
					OTActivator.log(status);

					synchronized(connection) {
						connection.notifyAll();
						connection = null;
					}
				}

				public void onLogin() {
					synchronized(connection) {
						connection.notifyAll();
					}
				}

				public void onRedirect(OTHost host) {
				}
			});

			try {
				synchronized(connection) {
					connection.wait(60 * 1000);
				}
			} catch(Exception e) {
				// Do nothing
			}
		}
	}

	public synchronized void disconnect() {
		if (connection != null) {
			try {
				connection.shutdown();
			} catch (Exception e) {
				// For some reasons, shutdown almost always throws an exception, so ignore it for now
			}
			connection = null;
		}
	}

	protected IPreferenceStore getPreferenceStore() {
		return OTActivator.getDefault().getPreferenceStore();
	}
}
