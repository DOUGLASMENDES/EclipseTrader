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

package net.sourceforge.eclipsetrader.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;

/**
 * Helper class for using viewLabelProviders extension point
 */
public class LabelProvidersRegistry
{
    Map map = new HashMap();

    /**
     * Builds the registry with the entries related to the given targetId.
     * 
     * @param targetId the viewContribution target id
     */
    public LabelProvidersRegistry(String targetId)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.LABEL_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                IConfigurationElement item = members[i];
                if (item.getAttribute("targetID").equals(targetId))
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
    
    /**
     * Get the name of the contributed item as it is specified in the plugins registry.
     * 
     * @param id the contribution id
     */
    public String getName(String id)
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
    
    /**
     * Get the orientation for the contributed item.
     * 
     * @param id the contribution id
     * @return the SWT style
     */
    public int getOrientation(String id)
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

    /**
     * Get a list of all registered contributions.
     * 
     * @return the registered contributions
     */
    public IConfigurationElement[] getProviders()
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

    /**
     * Create an instance of the label provider with the given id.
     * <p>Callers are responsible for disposing the instance when no longer
     * needed.</p>
     * 
     * @param id the contribution id
     * @return the new instance or null
     */
    public ILabelProvider createLabelProvider(String id)
    {
        IConfigurationElement element = (IConfigurationElement)map.get(id);
        if (element != null)
        {
            try {
                Object obj = element.createExecutableExtension("class");
                return (ILabelProvider)obj;
            } catch(Exception e) {
                LogFactory.getLog(LabelProvidersRegistry.class).error(e, e);
            }
        }
        
        return null;
    }
}
