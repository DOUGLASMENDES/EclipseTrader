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

package net.sourceforge.eclipsetrader.charts.dialogs;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.ChartObject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class ObjectSettingsDialog extends PreferenceDialog
{
    private ChartObject object;

    public ObjectSettingsDialog(ChartObject indicator, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.object = indicator;

        Settings settings = new Settings();
        for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
        {
            String key = (String)iter.next();
            settings.set(key, (String)indicator.getParameters().get(key));
        }
        
        IConfigurationElement plugin = ChartsPlugin.getObjectPlugin(indicator.getPluginId());
        IConfigurationElement[] members = plugin.getChildren("preferencePage");
        if (members.length != 0)
        {
            IConfigurationElement item = members[0];
            try {
                Object obj = item.createExecutableExtension("class");
                if (obj instanceof ObjectPluginPreferencePage)
                {
                    ((ObjectPluginPreferencePage)obj).setSettings(settings);
                    ObjectPage page = new ObjectPage(indicator, (ObjectPluginPreferencePage)obj);
                    if (item.getAttribute("title") != null)
                        page.setTitle(item.getAttribute("title"));

                    getPreferenceManager().addToRoot(new PreferenceNode("indicator", page));
                }

                for (int p = 1; p < members.length; p++)
                {
                    obj = item.createExecutableExtension("class");
                    if (obj instanceof ObjectPluginPreferencePage)
                    {
                        ((ObjectPluginPreferencePage)obj).setSettings(settings);
                        ObjectPage page = new ObjectPage(indicator, (ObjectPluginPreferencePage)obj);
                        if (item.getAttribute("title") != null)
                            page.setTitle(item.getAttribute("title"));

                        getPreferenceManager().addToRoot(new PreferenceNode("prefs" + p, page));
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Object Settings");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#okPressed()
     */
    protected void okPressed()
    {
        super.okPressed();
        CorePlugin.getRepository().save(object.getParent().getParent().getParent());
    }
}
