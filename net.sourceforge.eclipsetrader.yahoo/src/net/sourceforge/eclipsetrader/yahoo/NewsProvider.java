/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.yahoo;


import java.util.Comparator;
import java.util.Vector;

import net.sourceforge.eclipsetrader.INewsData;
import net.sourceforge.eclipsetrader.INewsProvider;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 */
public class NewsProvider implements INewsProvider, IPropertyChangeListener
{
  private static final String SOURCE_PROPERTY = "net.sourceforge.eclipsetrader.yahoo.newsSource";
  private Vector _data = new Vector();
  private INewsData[] dataArray;
  
  public NewsProvider()
  {
    YahooPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
/*    String property = event.getProperty();
    if (property.equalsIgnoreCase(SOURCE_PROPERTY) == true)
      newsSource = (INewsSource)activatePlugin(SOURCE_PROPERTY);*/
  }

  /**
   * Load the specified plugin id from the given extension point.<br>
   * 
   * @return plugin Object or null if plugin cannot be instantiated or no
   * plugins are present.
   */
  public Object activatePlugin(String ep, String id)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
      return null;
    
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    for (int m = 0; m < members.length; m++)
    {
      IConfigurationElement member = members[m];
      if (id.equalsIgnoreCase(member.getAttribute("id")))
        try {
          return member.createExecutableExtension("class");
        } catch(Exception x) { x.printStackTrace(); };
    }
    
    // If we are here, then the configured plugin is no more available, so
    // reset the preference to avoid future problems.
    YahooPlugin.getDefault().getPreferenceStore().setValue(ep, "");
    
    return null;
  }

  public INewsData[] getData()
  {
    return dataArray;
  }

  public void update(IProgressMonitor monitor)
  {
    String sources = YahooPlugin.getDefault().getPreferenceStore().getString(SOURCE_PROPERTY);
    String[] id = sources.split(",");

    int total = 0;
    INewsSource[] ns = new INewsSource[id.length];
    for (int i = 0; i < id.length; i++)
    {
      ns[i] = (INewsSource)activatePlugin(SOURCE_PROPERTY, id[i]);
      total += ns[i].getTasks();
    }
    _data.removeAllElements();
    monitor.beginTask("News Update", total);
    for (int i = 0; i < ns.length; i++)
      _data.addAll(ns[i].update(monitor));
    monitor.done();
    
    java.util.Collections.sort(_data, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        INewsData d1 = (INewsData)o1;
        INewsData d2 = (INewsData)o2;
        if (d1.getDate().before(d2.getDate()) == true)
          return 1;
        else if (d1.getDate().after(d2.getDate()) == true)
          return -1;
        return 0;
      }
    });
    
    dataArray = new INewsData[_data.size()];
    _data.toArray(dataArray);
  }
}
