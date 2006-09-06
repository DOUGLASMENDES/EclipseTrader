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

package net.sourceforge.eclipsetrader.trading.internal.watchlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;

public class ColumnRegistry
{
    static Map map = new HashMap();

    public ColumnRegistry()
    {
    }
    
    public static String getName(String id)
    {
        String name = "";
        IConfigurationElement element = (IConfigurationElement)map.get(id);
        if (element != null)
        {
            if (element.getAttribute("name") != null)
                name = element.getAttribute("name");
        }
        return name;
    }
    
    public static int getOrientation(String id)
    {
        IConfigurationElement element = (IConfigurationElement)map.get(id);
        if (element != null)
        {
            if ("left".equals(element.getAttribute("orientation")))
                return SWT.LEFT;
            if ("right".equals(element.getAttribute("orientation")))
                return SWT.RIGHT;
            if ("center".equals(element.getAttribute("orientation")))
                return SWT.CENTER;
        }
        return SWT.LEFT;
    }
    
    public static IConfigurationElement[] getProviders()
    {
        List list = new ArrayList(map.values());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                String s1 = "";
                String s2 = "";
                if (((IConfigurationElement)o1).getAttribute("name") != null)
                    s1 = ((IConfigurationElement)o1).getAttribute("name");
                if (((IConfigurationElement)o2).getAttribute("name") != null)
                    s2 = ((IConfigurationElement)o2).getAttribute("name");
                return s1.compareTo(s2);
            }
        });
        return (IConfigurationElement[])list.toArray(new IConfigurationElement[list.size()]);
    }
    
    public static ILabelProvider createLabelProvider(String id)
    {
        IConfigurationElement element = (IConfigurationElement)map.get(id);
        if (element != null)
        {
            try {
                Object obj = element.createExecutableExtension("class");
                return (ILabelProvider)obj;
            } catch(Exception e) {
                Logger.getLogger(ColumnRegistry.class).error(e, e);
            }
        }
        
        return null;
    }
    
    static {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.LABEL_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("targetID").equals(WatchlistView.VIEW_ID))
                {
                    IConfigurationElement[] children = item.getChildren();
                    for (int ii = 0; ii < children.length; ii++)
                    {
                        String id = children[ii].getAttribute("id");
                        map.put(id, children[ii]);
                    }
                }
            }
        }
    }
}
