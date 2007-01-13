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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ui.internal.Messages;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LoggerPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    Button console;
    Button file;
    Combo format;
    Combo rootLogger;
    Map loggers = new HashMap();
    IExtensionRegistry registry;
    IExtensionPoint extensionPoint;
    PreferenceStore preferences = new PreferenceStore();

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        preferences.setFilename(CorePlugin.getDefault().getStateLocation().append("log4j.properties").toOSString()); //$NON-NLS-1$

        registry = Platform.getExtensionRegistry();
        extensionPoint = registry.getExtensionPoint(CorePlugin.LOGGER_PREFERENCES_EXTENSION_POINT);
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();
        for (int i = 0; i < members.length; i++)
        {
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
        
        try {
            URL url = CorePlugin.getDefault().getBundle().getResource("log4j.properties"); //$NON-NLS-1$
            Properties properties = new Properties();
            properties.load(url.openStream());
            for (Iterator iter = properties.keySet().iterator(); iter.hasNext(); )
            {
                String key = (String)iter.next();
                preferences.setDefault(key, (String)properties.get(key));
            }

            File file = CorePlugin.getDefault().getStateLocation().append("log4j.properties").toFile(); //$NON-NLS-1$
            if (file.exists())
                preferences.load(new FileInputStream(file));
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        String rootValue = preferences.getString("log4j.rootLogger"); //$NON-NLS-1$
        String currentPattern = preferences.getString("log4j.appender.stdout.layout.ConversionPattern"); //$NON-NLS-1$

        console = new Button(content, SWT.CHECK);
        console.setText(Messages.LoggerPreferencesPage_WriteToConsole);
        console.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        if (rootValue != null && rootValue.indexOf("stdout") != -1) //$NON-NLS-1$
            console.setSelection(true);
        
        file = new Button(content, SWT.CHECK);
        file.setText(Messages.LoggerPreferencesPage_WriteToFile);
        file.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        if (rootValue != null && rootValue.indexOf("file") != -1) //$NON-NLS-1$
            file.setSelection(true);
        
        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.LoggerPreferencesPage_Format);
        label.setLayoutData(new GridData(107, SWT.DEFAULT));
        format = new Combo(content, SWT.READ_ONLY);
        format.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        format.add(Messages.LoggerPreferencesPage_Default);
        
        Group group = new Group(content, SWT.V_SCROLL);
        group.setText(Messages.LoggerPreferencesPage_Levels);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        ((GridData)group.getLayoutData()).heightHint = 200;
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.verticalSpacing = 3;
        group.setLayout(gridLayout);

        rootLogger = createLevelCombo(group, Messages.LoggerPreferencesPage_General, preferences.getString("log4j.rootLogger")); //$NON-NLS-2$

        List list = Arrays.asList(extensionPoint.getConfigurationElements());
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((IConfigurationElement)arg0).getAttribute("description").compareTo(((IConfigurationElement)arg1).getAttribute("description")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
        IConfigurationElement[] members = (IConfigurationElement[])list.toArray(new IConfigurationElement[list.size()]);
        for (int i = 0; i < members.length; i++)
        {
            IConfigurationElement element = members[i]; 
            if (element.getName().equals("logger") && loggers.get(element.getAttribute("name")) == null) //$NON-NLS-1$ //$NON-NLS-2$
            {
                String[] item = element.getAttribute("name").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
                Combo combo = createLevelCombo(group, element.getAttribute("description"), preferences.getString("log4j.logger." + item[0])); //$NON-NLS-1$ //$NON-NLS-2$
                combo.setData("logger", element.getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
                loggers.put(element.getAttribute("name"), combo); //$NON-NLS-1$
            }
            else if (element.getName().equals("layout")) //$NON-NLS-1$
            {
                format.setData(String.valueOf(format.getItemCount()), element.getAttribute("pattern")); //$NON-NLS-1$
                format.add(element.getAttribute("description")); //$NON-NLS-1$
                if (element.getAttribute("pattern").equals(currentPattern)) //$NON-NLS-1$
                    format.select(format.getItemCount() - 1);
            }
        }
        
        if (format.getSelectionIndex() == -1)
            format.select(0);

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        String pattern = (String)format.getData(String.valueOf(format.getSelectionIndex()));
        if (pattern != null)
        {
            preferences.setValue("log4j.appender.stdout.layout.ConversionPattern", pattern); //$NON-NLS-1$
            preferences.setValue("log4j.appender.file.layout.ConversionPattern", pattern); //$NON-NLS-1$
        }
        
        String root = (String)rootLogger.getData(String.valueOf(rootLogger.getSelectionIndex()));
        if (console.getSelection())
            root += ", stdout"; //$NON-NLS-1$
        if (file.getSelection())
            root += ", file"; //$NON-NLS-1$
        preferences.setValue("log4j.rootLogger", root); //$NON-NLS-1$

        for (Iterator iter = loggers.keySet().iterator(); iter.hasNext(); )
        {
            String logger = (String)iter.next();
            Combo combo = (Combo)loggers.get(logger);
            if (combo.getData(String.valueOf(combo.getSelectionIndex())) != null)
            {
                String[] item = logger.split(";"); //$NON-NLS-1$
                for (int x = 0; x < item.length; x++)
                    preferences.setValue("log4j.logger." + item[x], (String)combo.getData(String.valueOf(combo.getSelectionIndex()))); //$NON-NLS-1$
            }
        }
        
        try {
            preferences.save();
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        
        CorePlugin.getDefault().configureLogging();
        
        return super.performOk();
    }

    public static Combo createLevelCombo(Composite parent, String text, String value)
    {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Combo level = new Combo(parent, SWT.READ_ONLY);
        level.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        level.setData(String.valueOf(level.getItemCount()), "off"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Off);
        level.setData(String.valueOf(level.getItemCount()), "fatal"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Fatal);
        level.setData(String.valueOf(level.getItemCount()), "error"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Error);
        level.setData(String.valueOf(level.getItemCount()), "warn"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Warn);
        level.setData(String.valueOf(level.getItemCount()), "info"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Info);
        level.setData(String.valueOf(level.getItemCount()), "debug"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_Debug);
        level.setData(String.valueOf(level.getItemCount()), "all"); //$NON-NLS-1$
        level.add(Messages.LoggerPreferencesPage_All);

        if (value != null)
        {
            for (int i = 0; i < level.getItemCount(); i++)
            {
                if (value.indexOf((String)level.getData(String.valueOf(i))) != -1)
                    level.select(i);
            }
        }
        
        return level;
    }
}
