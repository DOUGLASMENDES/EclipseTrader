/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.indices;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.IIndexUpdateListener;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Images;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class IndexView extends ViewPart implements IPropertyChangeListener, IIndexUpdateListener, ISelectionProvider
{
  private static String EXTENSION_POINT_ID = "net.sourceforge.eclipsetrader.indexProvider";
  private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private SimpleDateFormat df_us = new SimpleDateFormat("MM/dd/yyyy h:mma");
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private Composite parent, composite;
  private Image up = Images.ICON_UP.createImage();
  private Image down = Images.ICON_DOWN.createImage();
  private Image equal = Images.ICON_EQUAL.createImage();
  private Vector widgets = new Vector();
  private HashMap map = new HashMap();
  private String selectedSymbol = "";
  
  public IndexView()
  {
    pf.setGroupingUsed(true);
    pf.setMaximumFractionDigits(2);
    pf.setMinimumFractionDigits(2);

    pcf.setGroupingUsed(true);
    pcf.setMaximumFractionDigits(2);
    pcf.setMinimumFractionDigits(2);

    nf.setGroupingUsed(true);
    nf.setMaximumFractionDigits(0);
    nf.setMinimumFractionDigits(0);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    this.parent = parent;
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    
    composite = new Composite(parent, SWT.NONE);
    RowLayout rowLayout = new RowLayout();
    rowLayout.wrap = true;
    rowLayout.pack = pref.getBoolean("index.equal_size") == true ? false : true;
    rowLayout.fill = true;
    rowLayout.justify = false;
    rowLayout.type = SWT.HORIZONTAL;
    rowLayout.marginLeft = 2;
    rowLayout.marginTop = 2;
    rowLayout.marginRight = 2;
    rowLayout.marginBottom = 2;
    rowLayout.spacing = 3;
    composite.setLayout(rowLayout);
    
    String[] providers = pref.getString("index.providers").split(",");
    for (int i = 0; i < providers.length; i++)
    {
      String[] symbols = pref.getString("index." + providers[i]).split(",");
      IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, providers[i]);
      if (ip != null)
      {
        for (int ii = 0; ii < symbols.length; ii++)
        {
          IndexWidget w = new IndexWidget(composite, SWT.NONE);
          w.setSymbol(symbols[ii]);
          w.setLayoutData(new RowData());
          widgets.add(w);
          createContextMenu(w);
        }
        ip.setSymbols(symbols);
        ip.addUpdateListener(this);
      }
    }
    
    // Sets the default data from the preference store
    restoreSavedData();

    // Listener for changes in property settings
    pref.addPropertyChangeListener(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    if (widgets.size() != 0)
      ((Composite)widgets.firstElement()).setFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.removePropertyChangeListener(this);

    String[] providers = pref.getString("index.providers").split(",");
    for (int i = 0; i < providers.length; i++)
    {
      String[] symbols = pref.getString("index." + providers[i]).split(",");
      IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, providers[i]);
      if (ip != null)
        ip.removeUpdateListener(this);
    }

    // Saves the latest received data
    if (map.values().size() != 0)
    {
      IExtendedData[] data = new IExtendedData[map.values().size()];
      map.values().toArray(data);
      TraderPlugin.getDataStore().storeIndexData(data);
    }

    up.dispose();
    down.dispose();
    
    super.dispose();
  }
  
  private void createContextMenu(IndexWidget parent) 
  {
    // Create menu manager.
    MenuManager menuMgr = new MenuManager("#popupMenu", parent.getSymbol()); //$NON-NLS-1$ //$NON-NLS-2$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager mgr) {
        selectedSymbol = mgr.getId();
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
    
    // Create menu.
    Menu menu = menuMgr.createContextMenu(parent);
    parent.setMenu(menu);
    
    // Register menu for extension.
    getSite().registerContextMenu(menuMgr, this);
  }
  
  private void restoreSavedData()
  {
    IExtendedData[] data = TraderPlugin.getDataStore().loadIndexData();
    for (int i = 0; i < data.length; i++)
    {
      for (int ii = 0; ii < widgets.size(); ii++)
      {
        IndexWidget widget = (IndexWidget)widgets.get(ii);
        if (widget.getSymbol().equalsIgnoreCase(data[i].getSymbol()) == true)
        {
          widget.setData(data[i]);
          map.put(data[i].getSymbol(), data[i]);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.indices.IIndexUpdateListener#indexUpdate(net.sourceforge.eclipsetrader.ui.views.indices.IIndexProvider)
   */
  public void indexUpdate(IIndexDataProvider provider)
  {
    final IExtendedData[] data = provider.getIndexData();
    composite.getDisplay().asyncExec(new Runnable() {
      public void run() {
        composite.setRedraw(false);
        for (int i = 0; i < data.length; i++)
        {
          map.put(data[i].getSymbol(), data[i]);
          for (int ii = 0; ii < widgets.size(); ii++)
          {
            IndexWidget widget = (IndexWidget)widgets.get(ii);
            if (widget.getSymbol().equalsIgnoreCase(data[i].getSymbol()) == true)
              widget.setData(data[i]);
          }
        }
        composite.setRedraw(true);
        composite.layout();
      }
    });
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    boolean updateWidgets = false;
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    
    if (event.getProperty().equalsIgnoreCase("net.sourceforge.eclipsetrader.streaming") == true)
    {
      if (TraderPlugin.isStreaming() == true)
        map.clear();
    }
    else if (event.getProperty().equalsIgnoreCase("index.providers") == true)
    {
      // Remove this listener from old providers
      String[] providers = ((String)event.getOldValue()).split(",");
      for (int i = 0; i < providers.length; i++)
      {
        IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, providers[i]);
        if (ip != null)
          ip.removeUpdateListener(this);
      }
      updateWidgets = true;
    }
    else if (event.getProperty().equalsIgnoreCase("index.equal_size") == true)
    {
      composite.dispose();
      parent.layout();
      composite = new Composite(parent, SWT.NONE);
      RowLayout rowLayout = new RowLayout();
      rowLayout.wrap = true;
      rowLayout.pack = pref.getBoolean("index.equal_size") == true ? false : true;
      rowLayout.fill = true;
      rowLayout.justify = false;
      rowLayout.type = SWT.HORIZONTAL;
      rowLayout.marginLeft = 2;
      rowLayout.marginTop = 2;
      rowLayout.marginRight = 2;
      rowLayout.marginBottom = 2;
      rowLayout.spacing = 3;
      composite.setLayout(rowLayout);
      widgets.removeAllElements();
      updateWidgets = true;
    }
    else if (event.getProperty().startsWith("index.") == true)
    {
      String[] providers = pref.getString("index.providers").split(",");
      for (int i = 0; i < providers.length; i++)
      {
        if (event.getProperty().equalsIgnoreCase("index." + providers[i]) == true)
          updateWidgets = true;
      }
    }
    
    if (updateWidgets == true)
    {
      // Saves the latest received data
      if (map.values().size() != 0)
      {
        IExtendedData[] data = new IExtendedData[map.values().size()];
        map.values().toArray(data);
        TraderPlugin.getDataStore().storeIndexData(data);
      }

      // Get a count of new symbols
      int totalWidgets = 0;
      String[] providers = pref.getString("index.providers").split(",");
      for (int i = 0; i < providers.length; i++)
      {
        String[] symbols = pref.getString("index." + providers[i]).split(",");
        totalWidgets += symbols.length;
      }
      // Remove widgets that are no longer needed
      while (totalWidgets < widgets.size())
      {
        ((IndexWidget)widgets.lastElement()).dispose();
        widgets.removeElement(widgets.lastElement());
      }
      // Creates new widgets for new symbols
      while (totalWidgets > widgets.size())
      {
        IndexWidget w = new IndexWidget(composite, SWT.NONE);
        w.setLayoutData(new RowData());
        widgets.add(w);
        createContextMenu(w);
      }
      
      // Updates the widget symbols and listeners
      int index = 0;
      for (int i = 0; i < providers.length; i++)
      {
        String[] symbols = pref.getString("index." + providers[i]).split(",");
        IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, providers[i]);
        if (ip != null)
        {
          for (int ii = 0; ii < symbols.length; ii++)
          {
            IndexWidget w = (IndexWidget)widgets.get(index);
            w.setSymbol(symbols[ii]);
            w.clear();
            index++;
          }
          ip.setSymbols(symbols);
          ip.addUpdateListener(this);
        }
      }
      
      // Updates the parent container
      restoreSavedData();
      parent.layout(true);
    }
  }
  
  public void updateIndexData(IBasicData d)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              String ticker = items[iii].getAttribute("ticker"); //$NON-NLS-1$
              String label = items[iii].getAttribute("label"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              {
                if (ticker.length() != 0)
                  d.setTicker(ticker);
                d.setDescription(label);
                return;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            String ticker = children[ii].getAttribute("ticker"); //$NON-NLS-1$
            String label = children[ii].getAttribute("label"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
            {
              if (ticker.length() != 0)
                d.setTicker(ticker);
              d.setDescription(label);
              return;
            }
          }
        }
      }
    }
  }
  
  public void openHistoryChart() 
  {
    String CHART_ID = "net.sourceforge.eclipsetrader.ui.views.ChartView"; //$NON-NLS-1$

    Enumeration enumeration = widgets.elements();
    while(enumeration.hasMoreElements() == true)
    {
      IndexWidget w = (IndexWidget)enumeration.nextElement();
      if (w.getSymbol().equalsIgnoreCase(selectedSymbol) == true)
      {
        IExtendedData data = (IExtendedData)w.getData();
        updateIndexData(data);
        
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 1;; i++)
        {
          IViewReference ref = page.findViewReference(CHART_ID, String.valueOf(i));
          if (ref == null)
          {
            try {
              ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + String.valueOf(i), data.getSymbol());
              IViewPart view = page.showView(CHART_ID, String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException e) {}
            break;
          }
        }
      }
    }
  }
  
  public void openIntradayChart()
  {
    Enumeration enumeration = widgets.elements();
    while(enumeration.hasMoreElements() == true)
    {
      IndexWidget w = (IndexWidget)enumeration.nextElement();
      if (w.getSymbol().equalsIgnoreCase(selectedSymbol) == true)
      {
        IExtendedData data = (IExtendedData)w.getData();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 1;; i++)
        {
          IViewReference ref = page.findViewReference("net.sourceforge.eclipsetrader.ui.views.RealtimeChart", String.valueOf(i)); //$NON-NLS-1$
          if (ref == null)
          {
            ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + String.valueOf(i), data.getSymbol()); //$NON-NLS-1$
            try {
              IViewPart view = page.showView("net.sourceforge.eclipsetrader.ui.views.RealtimeChart", String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
            } catch (PartInitException e) {}
            break;
          }
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection()
  {
    return null;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
  }
}
