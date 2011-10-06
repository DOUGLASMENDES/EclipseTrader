package org.eclipsetrader.ui.internal.ats;

import java.net.URI;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsetrader.ui.UIConstants;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.ui.ats"; //$NON-NLS-1$

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

    public static final String K_VIEWS_SECTION = "Views";
    public static final String K_URI = "uri";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(UIConstants.COLLAPSEALL_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/collapseall.gif")));
        reg.put(UIConstants.EXPANDALL_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/elcl16/expandall.gif")));
        reg.put(IMG_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/folder.png")));
        reg.put(IMG_INSTRUMENT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/shape_square.png")));
        reg.put(IMG_SCRIPT_FOLDER, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/folder_page_white.png")));
        reg.put(IMG_MAIN_SCRIPT, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/page_white_code.png")));
        reg.put(IMG_SCRIPT_INCLUDE, ImageDescriptor.createFromURL(getBundle().getResource("icons/obj16/script_link.png")));
        reg.put(IMG_REMOVE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/remove_exc.gif")));
        reg.put(IMG_REMOVE_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/remove_exc.gif")));
        reg.put(IMG_DELETE_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/etool16/delete.gif")));
        reg.put(IMG_DELETE_DISABLED_ICON, ImageDescriptor.createFromURL(getBundle().getResource("icons/dtool16/delete.gif")));
    }

    public static Image getImageFromRegistry(String key) {
        if (plugin == null || plugin.getImageRegistry() == null) {
            return null;
        }
        ImageDescriptor descriptor = plugin.getImageRegistry().getDescriptor(key);
        return descriptor != null ? descriptor.createImage() : null;
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
}
