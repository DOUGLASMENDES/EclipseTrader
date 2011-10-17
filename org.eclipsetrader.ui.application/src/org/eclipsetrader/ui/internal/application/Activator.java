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

package org.eclipsetrader.ui.internal.application;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipsetrader.ui.application";

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

        // TODO Remove after 1.0 release
        migrateSettings();
        migrateDialogSections();
        migrateFiles();
        // -----------------------------
    }

    private void migrateFiles() throws Exception {
        IPath workspacePath = Platform.getLocation().append(".metadata").append(".plugins");

        File file = workspacePath.append("org.eclipsetrader.core.trading").append("alerts.xml").toFile();
        if (file.exists()) {
            File destinationFile = workspacePath.append("org.eclipsetrader.core").append("alerts.xml").toFile();
            if (!destinationFile.exists()) {
                destinationFile.getParentFile().mkdirs();
            }
            else {
                destinationFile.delete();
            }
            file.renameTo(destinationFile);
        }

        file = workspacePath.append("org.eclipsetrader.core.trading").append("market_brokers.xml").toFile();
        if (file.exists()) {
            File destinationFile = workspacePath.append("org.eclipsetrader.core").append("market_brokers.xml").toFile();
            if (!destinationFile.exists()) {
                destinationFile.getParentFile().mkdirs();
            }
            else {
                destinationFile.delete();
            }
            file.renameTo(destinationFile);
        }
    }

    private void migrateSettings() throws Exception {
        IPath workspacePath = Platform.getLocation().append(".metadata").append(".plugins").append("org.eclipse.core.runtime").append(".settings");

        File preferencesFile = workspacePath.append("org.eclipsetrader.ui.prefs").toFile();
        PreferenceStore preferences = new PreferenceStore(preferencesFile.toString());
        if (preferencesFile.exists()) {
            preferences.load();
        }

        File legacyPreferencesFile = workspacePath.append("org.eclipsetrader.ui.charts.prefs").toFile();
        if (legacyPreferencesFile.exists()) {
            PreferenceStore legacyPreferences = new PreferenceStore(legacyPreferencesFile.toString());
            legacyPreferences.load();
            for (String name : legacyPreferences.preferenceNames()) {
                preferences.putValue(name, legacyPreferences.getString(name));
            }
            legacyPreferencesFile.delete();
        }

        legacyPreferencesFile = workspacePath.append("org.eclipsetrader.ui.trading.prefs").toFile();
        if (legacyPreferencesFile.exists()) {
            PreferenceStore legacyPreferences = new PreferenceStore(legacyPreferencesFile.toString());
            legacyPreferences.load();
            for (String name : legacyPreferences.preferenceNames()) {
                preferences.putValue(name, legacyPreferences.getString(name));
            }
            legacyPreferencesFile.delete();
        }

        legacyPreferencesFile = workspacePath.append("org.eclipsetrader.ui.ats.prefs").toFile();
        if (legacyPreferencesFile.exists()) {
            PreferenceStore legacyPreferences = new PreferenceStore(legacyPreferencesFile.toString());
            legacyPreferences.load();
            for (String name : legacyPreferences.preferenceNames()) {
                preferences.putValue(name, legacyPreferences.getString(name));
            }
            legacyPreferencesFile.delete();
        }

        if (!preferencesFile.exists()) {
            preferencesFile.getParentFile().mkdirs();
        }
        preferences.save();
    }

    private void migrateDialogSections() throws Exception {
        IPath workspacePath = Platform.getLocation().append(".metadata").append(".plugins");

        File dialogSettingsFile = workspacePath.append("org.eclipsetrader.ui").append("dialog_settings.xml").toFile();
        DialogSettings dialogSettings = new DialogSettings("Workbench");
        if (dialogSettingsFile.exists()) {
            dialogSettings.load(dialogSettingsFile.toString());
        }

        File legacyDialogSettingsFile = workspacePath.append("org.eclipsetrader.ui.charts").append("dialog_settings.xml").toFile();
        if (legacyDialogSettingsFile.exists()) {
            DialogSettings legacyDialogSettings = new DialogSettings("Workbench");
            legacyDialogSettings.load(legacyDialogSettingsFile.toString());

            IDialogSettings[] childSections = legacyDialogSettings.getSections();
            migrateSections(childSections, dialogSettings);

            legacyDialogSettingsFile.delete();
        }

        legacyDialogSettingsFile = workspacePath.append("org.eclipsetrader.ui.trading").append("dialog_settings.xml").toFile();
        if (legacyDialogSettingsFile.exists()) {
            DialogSettings legacyDialogSettings = new DialogSettings("Workbench");
            legacyDialogSettings.load(legacyDialogSettingsFile.toString());

            IDialogSettings[] childSections = legacyDialogSettings.getSections();
            migrateSections(childSections, dialogSettings);

            legacyDialogSettingsFile.delete();
        }

        legacyDialogSettingsFile = workspacePath.append("org.eclipsetrader.ui.ats").append("dialog_settings.xml").toFile();
        if (legacyDialogSettingsFile.exists()) {
            DialogSettings legacyDialogSettings = new DialogSettings("Workbench");
            legacyDialogSettings.load(legacyDialogSettingsFile.toString());

            IDialogSettings[] childSections = legacyDialogSettings.getSections();
            migrateSections(childSections, dialogSettings);

            legacyDialogSettingsFile.delete();
        }

        if (!dialogSettingsFile.exists()) {
            dialogSettingsFile.getParentFile().mkdirs();
        }
        dialogSettings.save(dialogSettingsFile.toString());
    }

    private void migrateSections(IDialogSettings[] sections, IDialogSettings to) {
        for (int i = 0; i < sections.length; i++) {
            IDialogSettings targetSection = to.getSection(sections[i].getName());
            if (targetSection != null) {
                IDialogSettings[] childSections = sections[i].getSections();
                migrateSections(childSections, targetSection);
            }
            else {
                to.addSection(sections[i]);
            }
        }
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

    /**
     * Logs the given message to the platform log.
     *
     * If you have an exception in hand, call log(String, Throwable) instead.
     *
     * If you have a status object in hand call log(String, IStatus) instead.
     *
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     *
     * @param message
     *            A high level UI message describing when the problem happened.
     */
    public static void log(String message) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
    }

    /**
     * Logs the given message and throwable to the platform log.
     *
     * If you have a status object in hand call log(String, IStatus) instead.
     *
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     *
     * @param message
     *            A high level UI message describing when the problem happened.
     * @param t
     *            The throwable from where the problem actually occurred.
     */
    public static void log(String message, Throwable t) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, t));
    }

    /**
     * Logs the given throwable to the platform log, indicating the class and
     * method from where it is being logged (this is not necessarily where it
     * occurred).
     *
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     *
     * @param clazz
     *            The calling class.
     * @param methodName
     *            The calling method name.
     * @param t
     *            The throwable from where the problem actually occurred.
     */
    public static void log(Class<?> clazz, String methodName, Throwable t) {
        String msg = MessageFormat.format("Exception in {0}.{1}: {2}", new Object[] { clazz.getName(), methodName, t}); //$NON-NLS-1$
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, t));
    }

    /**
     * Logs the given message and status to the platform log.
     *
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     *
     * @param message
     *            A high level UI message describing when the problem happened.
     *            May be <code>null</code>.
     * @param status
     *            The status describing the problem. Must not be null.
     */
    public static void log(String message, IStatus status) {
        getDefault().getLog().log(new Status(status.getSeverity(), PLUGIN_ID, status.getCode(), message, status.getException()));
    }
}
