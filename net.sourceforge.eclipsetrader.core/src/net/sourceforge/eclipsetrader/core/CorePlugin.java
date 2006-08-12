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

public class CorePlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.core"; //$NON-NLS-1$
    public static final String FEED_EXTENSION_POINT = PLUGIN_ID + ".feeds"; //$NON-NLS-1$
    public static final String TRADING_PROVIDERS_EXTENSION_POINT = PLUGIN_ID + ".tradingProviders"; //$NON-NLS-1$
    public static final String PATTERN_EXTENSION_POINT = PLUGIN_ID + ".patterns"; //$NON-NLS-1$
    public static final String ACCOUNT_PROVIDERS_EXTENSION_POINT = PLUGIN_ID + ".accountProviders"; //$NON-NLS-1$
    public static final String LOGGER_PREFERENCES_EXTENSION_POINT = PLUGIN_ID + ".loggingPreferences"; //$NON-NLS-1$
    public static final String FEED_RUNNING = "FEED_RUNNING"; //$NON-NLS-1$
    public static final String PREFS_ENABLE_HTTP_PROXY = "ENABLE_HTTP_PROXY"; //$NON-NLS-1$
    public static final String PREFS_PROXY_HOST_ADDRESS = "PROXY_HOST_ADDRESS"; //$NON-NLS-1$
    public static final String PREFS_PROXY_PORT_ADDRESS = "PROXY_PORT_ADDRESS"; //$NON-NLS-1$
    public static final String PREFS_ENABLE_PROXY_AUTHENTICATION = "ENABLE_PROXY_AUTHENTICATION"; //$NON-NLS-1$
    public static final String PREFS_PROXY_USER = "PROXY_USER"; //$NON-NLS-1$
    public static final String PREFS_PROXY_PASSWORD = "PROXY_PASSWORD"; //$NON-NLS-1$
    public static final String PREFS_HISTORICAL_PRICE_RANGE = "HISTORICAL_PRICE_RANGE"; //$NON-NLS-1$
    public static final String PREFS_NEWS_DATE_RANGE = "NEWS_DATE_RANGE"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_HISTORY = "UPDATE_HISTORY"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_HISTORY_ONCE = "UPDATE_HISTORY_ONCE"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_HISTORY_LAST = "UPDATE_HISTORY_LAST"; //$NON-NLS-1$
    public static final String PREFS_UPDATE_NEWS = "UPDATE_NEWS"; //$NON-NLS-1$
    private static CorePlugin plugin;
    private static Repository repository;
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Messages.CorePlugin_DateTimeFormat);
    private static SimpleDateFormat dateTimeParse = new SimpleDateFormat(Messages.CorePlugin_DateTimeParse);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.CorePlugin_DateFormat);
    private static SimpleDateFormat dateParse = new SimpleDateFormat(Messages.CorePlugin_DateParse);
    private static SimpleDateFormat timeFormat = new SimpleDateFormat(Messages.CorePlugin_TimeFormat);
    private static SimpleDateFormat timeParse = new SimpleDateFormat(Messages.CorePlugin_TimeParse);
    private IPropertyChangeListener feedPropertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(CorePlugin.FEED_RUNNING))
            {
                if (CorePlugin.getDefault().getPreferenceStore().getBoolean(CorePlugin.FEED_RUNNING))
                {
                    Job job = new Job(Messages.CorePlugin_UpdateCurrencies) {
                        protected IStatus run(IProgressMonitor monitor)
                        {
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

        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective)
        {
            IViewReference[] refs = page.getViewReferences();
            for (int i = 0; i < refs.length; i++)
                refs[i].getView(true);
        }

        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId)
        {
        }
    };

    public CorePlugin()
    {
        plugin = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);

        System.setProperty("workspace_loc", Platform.getLocation().toPortableString());
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

        preferenceStore.setValue(FEED_RUNNING, false);
        CorePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(feedPropertyListener);
        
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);
    }
    
    public void configureLogging()
    {
        try
        {
            Properties properties = new Properties();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.LOGGER_PREFERENCES_EXTENSION_POINT);

            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement element = members[i]; 
                if (element.getName().equals("logger"))
                {
                    if (element.getAttribute("defaultValue") != null)
                        properties.put("log4j.logger." + element.getAttribute("name"), element.getAttribute("defaultValue"));
                }
            }
            
            try {
                URL url = CorePlugin.getDefault().getBundle().getResource("log4j.properties"); //$NON-NLS-1$
                properties.load(url.openStream());

                File file = CorePlugin.getDefault().getStateLocation().append("log4j.properties").toFile();
                if (file.exists())
                    properties.load(new FileInputStream(file));
            } catch(Exception e) {
                CorePlugin.logException(e);
            }
            
            PropertyConfigurator.configure(properties);
        }
        catch (Exception e)
        {
            BasicConfigurator.configure();
            logException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
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

    public static CorePlugin getDefault()
    {
        return plugin;
    }

    public static Repository getRepository()
    {
        if (repository == null)
        {
            try
            {
                Class clazz = Class.forName("net.sourceforge.eclipsetrader.core.RepositoryImpl");
                repository = (Repository)clazz.newInstance();
            }
            catch (Exception e)
            {
                repository = new XMLRepository();
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
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public static IHistoryFeed createHistoryFeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("history")) //$NON-NLS-1$
                            try {
                                Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
                                return (IHistoryFeed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    public static IFeed createQuoteFeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("quote")) //$NON-NLS-1$
                            try {
                                Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
                                return (IFeed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    public static ILevel2Feed createLevel2FeedPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(FEED_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                {
                    members = item.getChildren();
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        if (members[ii].getName().equals("level2")) //$NON-NLS-1$
                            try {
                                Object obj = members[ii].createExecutableExtension("class"); //$NON-NLS-1$
                                return (ILevel2Feed)obj;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    public static List getAllPatternPlugins()
    {
        List list = new ArrayList();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
                list.add(members[i]);
        }
        
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
                String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
                return s0.compareTo(s1);
            }
        });
        
        return list;
    }
    
    public static IPattern createPatternPlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(PATTERN_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                {
                    try {
                        Object obj = members[i].createExecutableExtension("class"); //$NON-NLS-1$
                        return (IPattern)obj;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return null;
    }
    
    public static TradingProvider createTradeSourcePlugin(String id)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(TRADING_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(id)) //$NON-NLS-1$
                {
                    try {
                        TradingProvider obj = (TradingProvider)members[i].createExecutableExtension("class"); //$NON-NLS-1$
                        obj.setName(item.getAttribute("name"));
                        return obj;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        
        return null;
    }

    public static SimpleDateFormat getDateFormat()
    {
        return dateFormat;
    }

    public static SimpleDateFormat getDateParse()
    {
        return dateParse;
    }

    public static SimpleDateFormat getDateTimeFormat()
    {
        return dateTimeFormat;
    }

    public static SimpleDateFormat getDateTimeParse()
    {
        return dateTimeParse;
    }

    public static SimpleDateFormat getTimeFormat()
    {
        return timeFormat;
    }

    public static SimpleDateFormat getTimeParse()
    {
        return timeParse;
    }

    public static Account createAccount(String pluginId, PersistentPreferenceStore preferenceStore, List transactions)
    {
        Account account = null;
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.ACCOUNT_PROVIDERS_EXTENSION_POINT); //$NON-NLS-1$
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("id").equals(pluginId)) //$NON-NLS-1$
                {
                    try {
                        IAccountProvider provider = (IAccountProvider)members[i].createExecutableExtension("class"); //$NON-NLS-1$
                        account = provider.createAccount(preferenceStore, transactions);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        
        if (account == null)
        {
            account = new DefaultAccount();
            account.setPreferenceStore(preferenceStore);
            account.setTransactions(transactions);
        }
        
        return account;
    }

    public static void logException(Exception e)
    {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, 0, msg, e));
    }
}
