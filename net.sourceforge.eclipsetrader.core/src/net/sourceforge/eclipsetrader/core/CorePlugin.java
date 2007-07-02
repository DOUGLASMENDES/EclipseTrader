/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.sourceforge.eclipsetrader.core.db.Account;
import net.sourceforge.eclipsetrader.core.db.DefaultAccount;
import net.sourceforge.eclipsetrader.core.db.PersistentPreferenceStore;
import net.sourceforge.eclipsetrader.core.internal.LogListener;
import net.sourceforge.eclipsetrader.core.internal.Messages;
import net.sourceforge.eclipsetrader.core.internal.XMLRepository;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CorePlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.core"; //$NON-NLS-1$

	public static final String FEED_EXTENSION_POINT = PLUGIN_ID + ".feeds"; //$NON-NLS-1$

	public static final String TRADING_PROVIDERS_EXTENSION_POINT = PLUGIN_ID + ".tradingProviders"; //$NON-NLS-1$

	public static final String PATTERN_EXTENSION_POINT = PLUGIN_ID + ".patterns"; //$NON-NLS-1$

	public static final String ACCOUNT_PROVIDERS_EXTENSION_POINT = PLUGIN_ID + ".accountProviders"; //$NON-NLS-1$

	public static final String LABEL_PROVIDERS_EXTENSION_POINT = PLUGIN_ID + ".viewLabelProviders"; //$NON-NLS-1$

	public static final String LOGGER_PREFERENCES_EXTENSION_POINT = PLUGIN_ID + ".loggingPreferences"; //$NON-NLS-1$

	public static final String REPOSITORY_PREFERENCES_EXTENSION_POINT = PLUGIN_ID + ".customRepository"; //$NON-NLS-1$

	public static final String FEED_RUNNING = "FEED_RUNNING"; //$NON-NLS-1$

	public static final String PREFS_HISTORICAL_PRICE_RANGE = "HISTORICAL_PRICE_RANGE"; //$NON-NLS-1$

	public static final String PREFS_NEWS_DATE_RANGE = "NEWS_DATE_RANGE"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_HISTORY = "UPDATE_HISTORY"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_HISTORY_ONCE = "UPDATE_HISTORY_ONCE"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_HISTORY_LAST = "UPDATE_HISTORY_LAST"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_NEWS = "UPDATE_NEWS"; //$NON-NLS-1$

	public static final String PREFS_UPDATE_CURRENCIES = "UPDATE_CURRENCIES"; //$NON-NLS-1$

	public static final String PREFS_DELETE_CANCELED_ORDERS = "DELETE_CANCELED_ORDERS"; //$NON-NLS-1$

	public static final String PREFS_DELETE_CANCELED_ORDERS_DAYS = "DELETE_CANCELED_ORDERS_DAYS"; //$NON-NLS-1$

	public static final String PREFS_DELETE_FILLED_ORDERS = "DELETE_FILLED_ORDERS"; //$NON-NLS-1$

	public static final String PREFS_DELETE_FILLED_ORDERS_DAYS = "DELETE_FILLED_ORDERS_DAYS"; //$NON-NLS-1$

	private static CorePlugin plugin;

	private static Repository repository;

	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Messages.CorePlugin_DateTimeFormat);

	private static SimpleDateFormat dateTimeParse = new SimpleDateFormat(Messages.CorePlugin_DateTimeParse);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.CorePlugin_DateFormat);

	private static SimpleDateFormat dateParse = new SimpleDateFormat(Messages.CorePlugin_DateParse);

	private static SimpleDateFormat timeFormat = new SimpleDateFormat(Messages.CorePlugin_TimeFormat);

	private static SimpleDateFormat timeParse = new SimpleDateFormat(Messages.CorePlugin_TimeParse);

	private IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(CorePlugin.FEED_RUNNING)) {
				if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING) && CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.PREFS_UPDATE_CURRENCIES)) {
					Job job = new Job(Messages.CorePlugin_UpdateCurrencies) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							return CurrencyConverter.getInstance().updateExchanges(monitor);
						}
					};
					job.setUser(false);
					job.schedule();
				}
			}
		}
	};

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {

		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			IViewReference[] refs = page.getViewReferences();
			for (int i = 0; i < refs.length; i++)
				refs[i].getView(true);
		}

		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		}
	};

	public CorePlugin() {
		plugin = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		System.setProperty("workspace_loc", Platform.getLocation().toPortableString()); //$NON-NLS-1$
		configureLogging();

		LogListener logListener = new LogListener();
		Platform.addLogListener(logListener);
		getLog().addLogListener(logListener);

		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setDefault(FEED_RUNNING, false);
		if (preferenceStore.getDefaultInt(PREFS_HISTORICAL_PRICE_RANGE) == 0)
			preferenceStore.setDefault(PREFS_HISTORICAL_PRICE_RANGE, 5);
		if (preferenceStore.getDefaultInt(PREFS_NEWS_DATE_RANGE) == 0)
			preferenceStore.setDefault(PREFS_NEWS_DATE_RANGE, 3);

		preferenceStore.setDefault(PREFS_DELETE_CANCELED_ORDERS, false);
		preferenceStore.setDefault(PREFS_DELETE_CANCELED_ORDERS_DAYS, 2);
		preferenceStore.setDefault(PREFS_DELETE_FILLED_ORDERS, false);
		preferenceStore.setDefault(PREFS_DELETE_FILLED_ORDERS_DAYS, 5);

		preferenceStore.setValue(FEED_RUNNING, false);
		CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);
	}

	/**
	 * Log4j configurator
	 */
	public void configureLogging() {
		try {
			PreferenceStore preferences = new PreferenceStore();

			// Set the default values from extension registry
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.LOGGER_PREFERENCES_EXTENSION_POINT);
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement element = members[i];
				if (element.getName().equals("logger")) //$NON-NLS-1$
				{
					if (element.getAttribute("defaultValue") != null) //$NON-NLS-1$
					{
						String[] item = element.getAttribute("name").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
						for (int x = 0; x < item.length; x++)
							preferences.setDefault("log4j.logger." + item[x], element.getAttribute("defaultValue")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}

			// Set the default values from the bundle's properties file
			try {
				URL url = CorePlugin.getDefault().getBundle().getResource("log4j.properties"); //$NON-NLS-1$
				Properties properties = new Properties();
				properties.load(url.openStream());
				for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					preferences.setDefault(key, (String) properties.get(key));
				}

				// Read the values from the user's configuration
				File file = CorePlugin.getDefault().getStateLocation().append("log4j.properties").toFile(); //$NON-NLS-1$
				if (file.exists())
					preferences.load(new FileInputStream(file));
			} catch (Exception e) {
				CorePlugin.logException(e);
			}

			// Configure log4j
			Properties properties = new Properties();
			String[] names = preferences.preferenceNames();
			for (int i = 0; i < names.length; i++)
				properties.put(names[i], preferences.getString(names[i]));
			PropertyConfigurator.configure(properties);
		} catch (Exception e) {
			BasicConfigurator.configure();
			logException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		FeedMonitor.stop();
		Level2FeedMonitor.stop();
		if (repository != null)
			repository.dispose();

		getPreferenceStore().setValue(FEED_RUNNING, false);
		CorePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(feedPropertyListener);

		CurrencyConverter.getInstance().dispose();

		super.stop(context);
		plugin = null;
	}

	public static CorePlugin getDefault() {
		return plugin;
	}

	public static Repository getRepository() {
		if (repository == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.REPOSITORY_PREFERENCES_EXTENSION_POINT);
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			if (members.length > 0) {
				IConfigurationElement element = members[0];
				if (element.getName().equals("repository")) {
					if (element.getAttribute("value") != null) {
						try {
							Object ooo = element.createExecutableExtension("value");
							repository = (Repository) ooo;
						} catch (Exception e) {
							repository = new XMLRepository();
						}
					}
				}
			}

			if (repository == null) {
				try {
					Class clazz = Class.forName("net.sourceforge.eclipsetrader.core.RepositoryImpl");
					repository = (Repository) clazz.newInstance();
				} catch (Exception e) {
					repository = new XMLRepository();
				}
			}
		}
		return repository;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static IHistoryFeed createHistoryFeedPlugin(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					members = item.getChildren();
					for (int ii = 0; ii < members.length; ii++) {
						if (members[ii].getName().equals("history")) //$NON-NLS-1$
							try {
								Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
								return (IHistoryFeed) obj;
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					break;
				}
			}
		}

		return null;
	}

	public static IFeed createQuoteFeedPlugin(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					members = item.getChildren();
					for (int ii = 0; ii < members.length; ii++) {
						if (members[ii].getName().equals("quote")) //$NON-NLS-1$
							try {
								Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
								return (IFeed) obj;
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					break;
				}
			}
		}

		return null;
	}

	public static ILevel2Feed createLevel2FeedPlugin(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					members = item.getChildren();
					for (int ii = 0; ii < members.length; ii++) {
						if (members[ii].getName().equals("level2")) //$NON-NLS-1$
							try {
								Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
								return (ILevel2Feed) obj;
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					break;
				}
			}
		}

		return null;
	}

	public static List getAllPatternPlugins() {
		List list = new ArrayList();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++)
				list.add(members[i]);
		}

		Collections.sort(list, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
				String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
				return s0.compareTo(s1);
			}
		});

		return list;
	}

	public static IPattern createPatternPlugin(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					try {
						Object obj = members[i].createExecutableExtension("class"); //$NON-NLS-1$
						return (IPattern) obj;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	public static ITradingProvider createTradeSourcePlugin(String id) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(TRADING_PROVIDERS_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
				{
					try {
						return (ITradingProvider) members[i].createExecutableExtension("class"); //$NON-NLS-1$
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return null;
	}

	public static SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public static SimpleDateFormat getDateParse() {
		return dateParse;
	}

	public static SimpleDateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}

	public static SimpleDateFormat getDateTimeParse() {
		return dateTimeParse;
	}

	public static SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}

	public static SimpleDateFormat getTimeParse() {
		return timeParse;
	}

	public static Account createAccount(String pluginId, PersistentPreferenceStore preferenceStore, List transactions) {
		Account account = null;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.ACCOUNT_PROVIDERS_EXTENSION_POINT);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(pluginId)) //$NON-NLS-1$
				{
					try {
						IAccountProvider provider = (IAccountProvider) members[i].createExecutableExtension("class"); //$NON-NLS-1$
						account = provider.createAccount(preferenceStore, transactions);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		if (account == null) {
			account = new DefaultAccount();
			account.setPreferenceStore(preferenceStore);
			account.setTransactions(transactions);
		}

		return account;
	}

	/**
	 * Returns the name of the extension identified by an extension point id and
	 * a plugin id.
	 * 
	 * @param extensionPointId - the extension point id
	 * @param pluginId - the plugin id
	 * @return the extension name, or null
	 */
	public static String getPluginName(String extensionPointId, String pluginId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionPointId);
		if (extensionPoint != null) {
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();
			for (int i = 0; i < members.length; i++) {
				IConfigurationElement item = members[i];
				if (item.getAttribute("id").equals(pluginId)) //$NON-NLS-1$
					return item.getAttribute("name"); //$NON-NLS-1$
			}
		}

		return null;
	}

	public static void logException(Exception e) {
		String msg = e.getMessage() == null ? e.toString() : e.getMessage();
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
	}
}
