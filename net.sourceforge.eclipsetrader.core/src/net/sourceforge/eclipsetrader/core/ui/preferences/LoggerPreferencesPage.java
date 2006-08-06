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

package net.sourceforge.eclipsetrader.core.ui.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
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
    Properties properties;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        registry = Platform.getExtensionRegistry();
        extensionPoint = registry.getExtensionPoint(CorePlugin.LOGGER_PREFERENCES_EXTENSION_POINT);

        properties = new Properties();
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();
        for (int i = 0; i < members.length; i++)
        {
            IConfigurationElement element = members[i]; 
            if (element.getName().equals("logger"))
            {
                if (element.getAttribute("defaultValue") != null)
                    properties.put("log4j.logger." + element.getAttribute("logger"), element.getAttribute("defaultValue"));
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
        
        String rootValue = (String)properties.get("log4j.rootLogger");
        String currentPattern = (String)properties.get("log4j.appender.stdout.layout.ConversionPattern");

        console = new Button(content, SWT.CHECK);
        console.setText("Write to console");
        console.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        if (rootValue != null && rootValue.indexOf("stdout") != -1)
            console.setSelection(true);
        
        file = new Button(content, SWT.CHECK);
        file.setText("Write to file");
        file.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
        if (rootValue != null && rootValue.indexOf("file") != -1)
            file.setSelection(true);
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Format");
        label.setLayoutData(new GridData(107, SWT.DEFAULT));
        format = new Combo(content, SWT.READ_ONLY);
        format.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        format.add("Default");
        
        Group group = new Group(content, SWT.NONE);
        group.setText("Levels");
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.verticalSpacing = 3;
        group.setLayout(gridLayout);

        rootLogger = createLevelCombo(group, "General", (String)properties.get("log4j.rootLogger"), false);

        List list = Arrays.asList(extensionPoint.getConfigurationElements());
        Collections.sort(list, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((IConfigurationElement)arg0).getAttribute("description").compareTo(((IConfigurationElement)arg1).getAttribute("description"));
            }
        });
        IConfigurationElement[] members = (IConfigurationElement[])list.toArray(new IConfigurationElement[list.size()]);
        for (int i = 0; i < members.length; i++)
        {
            IConfigurationElement element = members[i]; 
            if (element.getName().equals("logger") && loggers.get(element.getAttribute("name")) == null)
            {
                Combo combo = createLevelCombo(group, element.getAttribute("description"), (String)properties.get("log4j.logger." + element.getAttribute("name")));
                combo.setData("logger", element.getAttribute("name"));
                loggers.put(element.getAttribute("name"), combo);
            }
            else if (element.getName().equals("layout"))
            {
                format.setData(String.valueOf(format.getItemCount()), element.getAttribute("pattern"));
                format.add(element.getAttribute("description"));
                if (element.getAttribute("pattern").equals(currentPattern))
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
        Properties properties = new Properties();

        String pattern = (String)format.getData(String.valueOf(format.getSelectionIndex()));
        if (pattern != null)
        {
            properties.put("log4j.appender.stdout.layout.ConversionPattern", pattern);
            properties.put("log4j.appender.file.layout.ConversionPattern", pattern);
        }
        
        String root = (String)rootLogger.getData(String.valueOf(rootLogger.getSelectionIndex()));
        if (console.getSelection())
            root += ", stdout";
        if (file.getSelection())
            root += ", file";
        properties.put("log4j.rootLogger", root);

        for (Iterator iter = loggers.keySet().iterator(); iter.hasNext(); )
        {
            String logger = (String)iter.next();
            Combo combo = (Combo)loggers.get(logger);
            if (combo.getData(String.valueOf(combo.getSelectionIndex())) != null)
                properties.put("log4j.logger." + logger, combo.getData(String.valueOf(combo.getSelectionIndex())));
        }
        
        try {
            FileOutputStream os = new FileOutputStream(CorePlugin.getDefault().getStateLocation().append("log4j.properties").toFile());
            properties.store(os, null);
            os.flush();
            os.close();
        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        
        CorePlugin.getDefault().configureLogging();
        
        return super.performOk();
    }

    public static Combo createLevelCombo(Composite parent, String text, String value)
    {
        return createLevelCombo(parent, text, value, true);
    }

    public static Combo createLevelCombo(Composite parent, String text, String value, boolean hasDefault)
    {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Combo level = new Combo(parent, SWT.READ_ONLY);
        level.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        if (hasDefault)
            level.add("Default");
        level.setData(String.valueOf(level.getItemCount()), "off");
        level.add("Off");
        level.setData(String.valueOf(level.getItemCount()), "fatal");
        level.add("Fatal");
        level.setData(String.valueOf(level.getItemCount()), "error");
        level.add("Error");
        level.setData(String.valueOf(level.getItemCount()), "warn");
        level.add("Warn");
        level.setData(String.valueOf(level.getItemCount()), "info");
        level.add("Info");
        level.setData(String.valueOf(level.getItemCount()), "debug");
        level.add("Debug");
        level.setData(String.valueOf(level.getItemCount()), "all");
        level.add("All");

        if (value != null)
        {
            for (int i = 1; i < level.getItemCount(); i++)
            {
                if (value.indexOf((String)level.getData(String.valueOf(i))) != -1)
                    level.select(i);
            }
        }
        if (hasDefault && level.getSelectionIndex() == -1)
            level.select(0);
        
        return level;
    }
}
