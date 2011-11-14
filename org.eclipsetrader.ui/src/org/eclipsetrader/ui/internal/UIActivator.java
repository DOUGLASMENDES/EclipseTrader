/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal;

import java.net.URI;
import java.util.Hashtable;
import java.util.UUID;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.INotificationService;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.internal.adapters.MarketAdapterFactory;
import org.eclipsetrader.ui.internal.adapters.RepositoryAdapterFactory;
import org.eclipsetrader.ui.internal.adapters.SecurityAdapterFactory;
import org.eclipsetrader.ui.internal.adapters.WatchListAdapterFactory;
import org.eclipsetrader.ui.internal.navigator.NavigatorViewItem;
import org.eclipsetrader.ui.internal.navigator.NavigatorViewItemAdapterFactory;
import org.eclipsetrader.ui.internal.repositories.RepositoryViewItem;
import org.eclipsetrader.ui.internal.repositories.RepositoryViewItemAdapterFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class UIActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.ui";

    // The extension points IDs
    public static final String INDICATORS_EXTENSION_ID = "org.eclipsetrader.ui.indicators"; //$NON-NLS-1$

    public static final String PREFS_SHOW_SCALE_TOOLTIPS = "SHOW_SCALE_TOOLTIPS"; //$NON-NLS-1$
    public static final String PREFS_CROSSHAIR_ACTIVATION = "CROSSHAIR_ACTIVATION"; //$NON-NLS-1$
    public static final String PREFS_CROSSHAIR_SUMMARY_TOOLTIP = "CROSSHAIR_SUMMARY_TOOLTIP"; //$NON-NLS-1$
    public static final String PREFS_SHOW_TOOLTIPS = "SHOW_TOOLTIPS"; //$NON-NLS-1$
    public static final String PREFS_INITIAL_BACKFILL_METHOD = "INITIAL_BACKFILL_METHOD"; //$NON-NLS-1$
    public static final String PREFS_INITIAL_BACKFILL_START_DATE = "INITIAL_BACKFILL_START_DATE"; //$NON-NLS-1$
    public static final String PREFS_INITIAL_BACKFILL_YEARS = "INITIAL_BACKFILL_YEARS"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_ALTERNATE_BACKGROUND = "WATCHLIST_ALTERNATE_BACKGROUND"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_ENABLE_TICK_DECORATORS = "WATCHLIST_ENABLE_TICK_DECORATORS"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_POSITIVE_TICK_COLOR = "WATCHLIST_POSITIVE_TICK_COLOR"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_NEGATIVE_TICK_COLOR = "WATCHLIST_NEGATIVE_TICK_COLOR"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_DRAW_TICK_OUTLINE = "WATCHLIST_DRAW_TICK_OUTLINE"; //$NON-NLS-1$
    public static final String PREFS_WATCHLIST_FADE_TO_BACKGROUND = "WATCHLIST_FADE_TO_BACKGROUND"; //$NON-NLS-1$
    public static final String PREFS_TEXT_EDITOR_FONT = "TEXT_EDITOR_FONT"; //$NON-NLS-1$
    public static final String PREFS_CHART_PERIODS = "CHART_PERIODS"; //$NON-NLS-1$
    public static final String PREFS_CHART_BARS = "CHART_BARS"; //$NON-NLS-1$

    public static final String ALERT_NOTIFICATION_IMAGE = "alert_notification_image"; //$NON-NLS-1$
    public static final String ALERT_ADD_IMAGE = "alert_add_image"; //$NON-NLS-1$
    public static final String ALERT_DELETE_IMAGE = "alert_delete_image"; //$NON-NLS-1$
    public static final String ALERT_WIZARD_IMAGE = "alert_wizard_image"; //$NON-NLS-1$

    public static final String IMG_STRATEGY = "strategy";
    public static final String IMG_FOLDER = "folder";
    public static final String IMG_INSTRUMENT = "instrument";
    public static final String IMG_SCRIPT_FOLDER = "script-folder";
    public static final String IMG_SCRIPT_INCLUDE = "script-include";
    public static final String IMG_MAIN_SCRIPT = "main-script";
    public static final String IMG_REMOVE_ICON = "remove";
    public static final String IMG_REMOVE_DISABLED_ICON = "remove-disabled";
    public static final String IMG_DELETE_ICON = "delete";
    public static final String IMG_DELETE_DISABLED_ICON = "delete-disabled";
    public static final String IMG_TRADING_SYSTEM = "trading-system";

    public static final String K_VIEWS_SECTION = "Views";
    public static final String K_URI = "uri";

    // The shared instance
    private static UIActivator plugin;

    private ServiceRegistration notificationServiceRegistration;

    private IRepositoryService repositoryService;
    private IMarketService marketService;

    /**
     * The constructor
     */
    public UIActivator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        AdapterManager.getDefault().registerAdapters(new SecurityAdapterFactory(), ISecurity.class);
        AdapterManager.getDefault().registerAdapters(new WatchListAdapterFactory(), IWatchList.class);
        AdapterManager.getDefault().registerAdapters(new MarketAdapterFactory(), IMarket.class);
        AdapterManager.getDefault().registerAdapters(new RepositoryAdapterFactory(), IRepository.class);

        AdapterManager.getDefault().registerAdapters(new NavigatorViewItemAdapterFactory(), NavigatorViewItem.class);
        AdapterManager.getDefault().registerAdapters(new RepositoryViewItemAdapterFactory(), RepositoryViewItem.class);

        NotificationService notificationService = new NotificationService();
        notificationServiceRegistration = context.registerService(new String[] {
            INotificationService.class.getName(),
            NotificationService.class.getName()
        }, notificationService, new Hashtable<String, Object>());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (notificationServiceRegistration != null) {
            notificationServiceRegistration.unregister();
        }

        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static UIActivator getDefault() {
        return plugin;
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

    public static Image getImageFromRegistry(String key) {
        if (plugin == null || plugin.getImageRegistry() == null) {
            return null;
        }
        return plugin.getImageRegistry().get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(UIConstants.BLANK_OBJECT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/blank_obj.gif")));
        reg.put(UIConstants.FOLDER_OBJECT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/fldr_obj.png")));

        reg.put(UIConstants.MARKET_OBJECT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/market.png")));
        reg.put(UIConstants.MARKET_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/market_fldr.png")));

        reg.put(UIConstants.REPOSITORY, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/repository.png")));
        reg.put(UIConstants.REPOSITORY_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/repository_fldr.png")));
        reg.put(UIConstants.REPOSITORY_OBJECT_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/repository_object_fldr.png")));
        reg.put(UIConstants.REPOSITORY_OBJECT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/repository_object.png")));

        reg.put(UIConstants.DELETE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/delete.gif")));
        reg.put(UIConstants.DELETE_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dlcl16/delete.gif")));

        reg.put(UIConstants.CUT_EDIT_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/cut_edit.gif")));
        reg.put(UIConstants.CUT_EDIT_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/cut_edit.gif")));
        reg.put(UIConstants.COPY_EDIT_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/copy_edit.gif")));
        reg.put(UIConstants.COPY_EDIT_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/copy_edit.gif")));
        reg.put(UIConstants.PASTE_EDIT_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/paste_edit.gif")));
        reg.put(UIConstants.PASTE_EDIT_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/paste_edit.gif")));
        reg.put(UIConstants.DELETE_EDIT_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/delete_edit.gif")));
        reg.put(UIConstants.DELETE_EDIT_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/delete_edit.gif")));

        reg.put(UIConstants.REFRESH_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/refresh.gif")));

        reg.put(UIConstants.COLLAPSEALL_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/collapseall.gif")));
        reg.put(UIConstants.EXPANDALL_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/expandall.gif")));

        reg.put(UIConstants.TREND_STABLE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/trend_stable.gif")));
        reg.put(UIConstants.TREND_UP_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/trend_up.gif")));
        reg.put(UIConstants.TREND_DOWN_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/trend_down.gif")));

        reg.put(UIConstants.TOOLBAR_ARROW_DOWN, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/" + UIConstants.TOOLBAR_ARROW_DOWN)));
        reg.put(UIConstants.TOOLBAR_ARROW_RIGHT, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/" + UIConstants.TOOLBAR_ARROW_RIGHT)));

        reg.put(ALERT_NOTIFICATION_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/eview16/bell.png"))); //$NON-NLS-1$

        reg.put(ALERT_WIZARD_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/wizban/newfile_wiz.gif"))); //$NON-NLS-1$
        reg.put(ALERT_ADD_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/bell_add.png"))); //$NON-NLS-1$
        reg.put(ALERT_DELETE_IMAGE, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/delete.gif"))); //$NON-NLS-1$

        reg.put(IMG_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/folder.png")));
        reg.put(IMG_INSTRUMENT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/shape_square.png")));
        reg.put(IMG_SCRIPT_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/folder_page_white.png")));
        reg.put(IMG_MAIN_SCRIPT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/page_white_code.png")));
        reg.put(IMG_SCRIPT_INCLUDE, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/script_link.png")));
        reg.put(IMG_REMOVE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/remove_exc.gif")));
        reg.put(IMG_REMOVE_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/remove_exc.gif")));
        reg.put(IMG_DELETE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/delete.gif")));
        reg.put(IMG_DELETE_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/delete.gif")));
        reg.put(IMG_TRADING_SYSTEM, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/cog.png")));
    }

    public IRepositoryService getRepositoryService() {
        if (repositoryService == null) {
            BundleContext context = getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
            if (serviceReference != null) {
                repositoryService = (IRepositoryService) context.getService(serviceReference);
            }
        }
        return repositoryService;
    }

    public IMarketService getMarketService() {
        if (marketService == null) {
            BundleContext context = getBundle().getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
            if (serviceReference != null) {
                marketService = (IMarketService) context.getService(serviceReference);
            }
        }
        return marketService;
    }

    public IDialogSettings getDialogSettingsForView(URI uri) {
        String uriString = uri.toString();

        IDialogSettings rootSettings = getDialogSettings().getSection(K_VIEWS_SECTION);
        if (rootSettings == null) {
            rootSettings = getDialogSettings().addNewSection(K_VIEWS_SECTION);
        }

        IDialogSettings[] sections = rootSettings.getSections();
        for (int i = 0; i < sections.length; i++) {
            if (uriString.equals(sections[i].get(K_URI))) {
                return sections[i];
            }
        }

        String uuid = UUID.randomUUID().toString();
        IDialogSettings dialogSettings = rootSettings.addNewSection(uuid);
        dialogSettings.put(K_URI, uriString);

        return dialogSettings;
    }

    public static void log(IStatus status) {
        if (plugin != null) {
            plugin.getLog().log(status);
        }
        else {
            System.err.println(status);
        }
    }

    public static void log(String message, Throwable throwable) {
        Status status = new Status(IStatus.ERROR, PLUGIN_ID, message, throwable);
        if (plugin != null) {
            plugin.getLog().log(status);
        }
        else {
            System.err.println(status);
        }
    }

    public IChartObjectFactory getChartObjectFactory(String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(INDICATORS_EXTENSION_ID);
        if (extensionPoint == null) {
            return null;
        }

        IConfigurationElement targetElement = null;
        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (targetID.equals(strID)) {
                targetElement = configElements[j];
                break;
            }
        }
        if (targetElement == null) {
            return null;
        }

        try {
            return (IChartObjectFactory) targetElement.createExecutableExtension("class"); //$NON-NLS-1$
        } catch (Exception e) {
            Status status = new Status(IStatus.WARNING, PLUGIN_ID, 0, Messages.ChartsUIActivator_IndicatorErrorMessage + targetID, e);
            getLog().log(status);
        }

        return null;
    }

    public IConfigurationElement getChartObjectConfiguration(String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(INDICATORS_EXTENSION_ID);
        if (extensionPoint == null) {
            return null;
        }

        IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (targetID.equals(strID)) {
                return configElements[j];
            }
        }

        return null;
    }

    public static ImageDescriptor imageDescriptorFromPlugin(String imageFilePath) {
        return imageDescriptorFromPlugin(PLUGIN_ID, imageFilePath);
    }
}
