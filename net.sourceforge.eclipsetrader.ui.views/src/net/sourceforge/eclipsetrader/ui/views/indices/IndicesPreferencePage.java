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
package net.sourceforge.eclipsetrader.ui.views.indices;

import java.util.Vector;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for index view.
 */
public class IndicesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ICheckStateListener
{
  private FieldEditor[] editor;
  private Tree tree;
  private CheckboxTreeViewer viewer;
  
  class IndexProvider
  {
    private String id = ""; //$NON-NLS-1$
    private String label = ""; //$NON-NLS-1$
    private Vector children = new Vector();
    private IndexProvider parent = null;
    
    public IndexProvider(String label)
    {
      this.label = label;
    }
    public IndexProvider(String id, String label)
    {
      this.id = id;
      this.label = label;
    }
    public Vector getChildren()
    {
      return children;
    }
    public void add(IndexProvider item)
    {
      children.add(item);
      item.parent = this;
      item.id = id;
    }
    public void add(IndexItem item)
    {
      children.add(item);
      item.parent = this;
      item.id = id;
    }
    public String getId()
    {
      return id;
    }
    public IndexProvider getParent()
    {
      return parent;
    }
    public String toString()
    {
      return label;
    }
  };
  
  class IndexItem
  {
    private String id = ""; //$NON-NLS-1$
    private String label = ""; //$NON-NLS-1$
    private String symbol = ""; //$NON-NLS-1$
    private IndexProvider parent = null;
    
    public IndexItem(String label, String symbol)
    {
      this.label = label;
      this.symbol = symbol;
    }
    public String getId()
    {
      return id;
    }
    public String getSymbol()
    {
      return symbol;
    }
    public IndexProvider getParent()
    {
      return parent;
    }
    public String toString()
    {
      return label;
    }
  };

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench)
  {
    //Initialize the preference store we wish to use
    setPreferenceStore(ViewsPlugin.getDefault().getPreferenceStore());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent)
  {
    Vector _v = new Vector();

    Composite composite = new Composite(parent, SWT.NULL);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.grabExcessHorizontalSpace = true;
    composite.setLayoutData(data);
    GridLayout layout = new GridLayout();
    composite.setLayout(layout);

    Composite entryTable = new Composite(composite, SWT.NONE);
    entryTable.setLayout(new GridLayout(2, false));
    entryTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Standard color preferences
    _v.add(new ColorFieldEditor("index.text_color", Messages.getString("IndicesPreferencePage.textForeground"), entryTable)); //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("index.background", Messages.getString("IndicesPreferencePage.background"), entryTable)); //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("index.positive_value_color", Messages.getString("IndicesPreferencePage.positiveValueColor"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("index.negative_value_color", Messages.getString("IndicesPreferencePage.negativeValueColor"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$

    // Index table label
    Label label = new Label(entryTable, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);
    label.setText(Messages.getString("IndicesPreferencePage.availableIndices")); //$NON-NLS-1$

    // Index tree view
    tree = new Tree(entryTable, SWT.CHECK|SWT.BORDER);
    gridData = new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;
    tree.setLayoutData(gridData);
    viewer = new CheckboxTreeViewer(tree);
    viewer.setContentProvider(new ITreeContentProvider() {
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
      {
      }
      public void dispose()
      {
      }
      public Object[] getElements(Object inputElement)
      {
        Vector v = (Vector)inputElement;
        Object[] r = new Object[v.size()];
        v.toArray(r);
        return r;
      }
      public Object[] getChildren(Object parentElement)
      {
        if (parentElement instanceof IndexProvider)
        {
          Vector v = ((IndexProvider)parentElement).getChildren();
          Object[] r = new Object[v.size()];
          v.toArray(r);
          return r;
        }
        return null;
      }
      public Object getParent(Object element)
      {
        if (element instanceof IndexProvider)
          return ((IndexProvider)element).getParent();
        if (element instanceof IndexItem)
          return ((IndexItem)element).getParent();
        return null;
      }
      public boolean hasChildren(Object element)
      {
        if (element instanceof IndexProvider)
        {
          Vector v = ((IndexProvider)element).getChildren();
          return (v.size() == 0) ? false : true;
        }
        return false;
      }
    });
    viewer.setLabelProvider(new LabelProvider());
//    viewer.setAutoExpandLevel(CheckboxTreeViewer.ALL_LEVELS);

    // Insert the index providers
    Vector sources = new Vector();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IndexProvider provider = new IndexProvider(members[i].getAttribute("id"), members[i].getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
        sources.add(provider);

        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IndexProvider subProvider = new IndexProvider(members[i].getAttribute("id"), children[ii].getAttribute("name")); //$NON-NLS-1$ //$NON-NLS-2$
            provider.add(subProvider);
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
              subProvider.add(new IndexItem(items[iii].getAttribute("label"), items[iii].getAttribute("symbol"))); //$NON-NLS-1$ //$NON-NLS-2$
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
            provider.add(new IndexItem(children[ii].getAttribute("label"), children[ii].getAttribute("symbol"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    viewer.setInput(sources);
    
    // Sets the checked state of configured items
    for (int i = 0; i < sources.size(); i++)
    {
      if (sources.get(i) instanceof IndexProvider)
      {
        String symbols[] = getPreferenceStore().getString("index." + ((IndexProvider)sources.get(i)).getId()).split(","); //$NON-NLS-1$ //$NON-NLS-2$

        int totalItems = 0;
        int topChecked = 0;
        Vector childs = ((IndexProvider)sources.get(i)).getChildren();
        for (int ii = 0; ii < childs.size(); ii++)
        {
          if (childs.get(ii) instanceof IndexProvider)
          {
            int checked = 0;
            Vector items = ((IndexProvider)childs.get(ii)).getChildren();
            totalItems += items.size();
            for (int iii = 0; iii < items.size(); iii++)
            {
              String s = ((IndexItem)items.get(iii)).getSymbol();
              for (int x = 0; x < symbols.length; x++)
              {
                if (s.equalsIgnoreCase(symbols[x]) == true)
                {
                  viewer.setChecked(items.get(iii), true);
                  checked++;
                }
              }
            }
            if (checked == 0)
              viewer.setChecked(childs.get(ii), false);
            else
            {
              viewer.setChecked(childs.get(ii), true);
              if (checked == items.size())
                viewer.setGrayed(childs.get(ii), false);
              else
                viewer.setGrayed(childs.get(ii), true);
            }
            topChecked += checked;
          }
          else if (childs.get(ii) instanceof IndexItem)
          {
            totalItems++;
            String s = ((IndexItem)childs.get(ii)).getSymbol();
            for (int x = 0; x < symbols.length; x++)
            {
              if (s.equalsIgnoreCase(symbols[x]) == true)
              {
                viewer.setChecked(childs.get(ii), true);
                topChecked++;
              }
            }
          }
        }
        if (topChecked == 0)
          viewer.setChecked(sources.get(i), false);
        else
        {
          viewer.setChecked(sources.get(i), true);
          if (topChecked == totalItems)
            viewer.setGrayed(sources.get(i), false);
          else
            viewer.setGrayed(sources.get(i), true);
        }
      }
    }
    
    viewer.addCheckStateListener(this);

    // Sets the font of top-level items to bold
    TreeItem[] items = tree.getItems();
    for (int i = 0; i < items.length; i++)
    {
      if (items[i].getData() instanceof IndexProvider)
      {
        FontData[] fontData = items[i].getFont().getFontData();
        for (int ii = 0; ii < fontData.length; ii++)
          fontData[ii].setStyle(fontData[ii].getStyle() | SWT.BOLD);
        items[i].setFont(new Font(items[i].getDisplay(), fontData));
      }
    }

    // Perform operations common to all field editors
    editor = new FieldEditor[_v.size()];
    for (int i = 0; i < _v.size(); i++)
    {
      editor[i] = (FieldEditor)_v.elementAt(i);
      editor[i].setPreferenceStore(getPreferenceStore());
      editor[i].load();
    }
    
    return entryTable;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
    viewer.removeCheckStateListener(this);
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk()
  {
    StringBuffer providers = new StringBuffer();
    
    TreeItem[] top = tree.getItems();
    for (int i = 0; i < top.length; i++)
    {
      if (top[i].getData() instanceof IndexProvider)
      {
        IndexProvider provider = (IndexProvider)top[i].getData(); 
        StringBuffer symbols = new StringBuffer();
        TreeItem[] childs = top[i].getItems();
        for (int ii = 0; ii < childs.length; ii++)
        {
          if (childs[ii].getData() instanceof IndexProvider)
          {
            TreeItem[] items = childs[ii].getItems();
            for (int iii = 0; iii < items.length; iii++)
            {
              if (items[iii].getChecked() == true)
                symbols.append(((IndexItem)items[iii].getData()).getSymbol() + ","); //$NON-NLS-1$
            }
          }
          else if (childs[ii].getData() instanceof IndexItem)
          {
            if (childs[ii].getChecked() == true)
              symbols.append(((IndexItem)childs[ii].getData()).getSymbol() + ","); //$NON-NLS-1$
          }
        }
        if (symbols.length() != 0)
        {
          providers.append(provider.getId() + ","); //$NON-NLS-1$
          symbols.deleteCharAt(symbols.length() - 1);
          System.out.println(provider.getId());
          System.out.println("   " + symbols); //$NON-NLS-1$
        }
        getPreferenceStore().setValue("index." + provider.getId(), symbols.toString()); //$NON-NLS-1$
      }
    }
    if (providers.length() != 0)
    {
      providers.deleteCharAt(providers.length() - 1);
      getPreferenceStore().setValue("index.providers", providers.toString()); //$NON-NLS-1$
    }
    
    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    return super.performOk();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
   */
  public void checkStateChanged(CheckStateChangedEvent event)
  {
    tree.setRedraw(false);
    
    TreeItem[] top = tree.getItems();
    for (int i = 0; i < top.length; i++)
    {
      if (top[i].getData() instanceof IndexProvider)
      {
        int totalItems = 0;
        int topChecked = 0;
        TreeItem[] childs = top[i].getItems();
        for (int ii = 0; ii < childs.length; ii++)
        {
          if (childs[ii].getData() instanceof IndexProvider)
          {
            int checked = 0;
            TreeItem[] items = childs[ii].getItems();
            totalItems += items.length;
            for (int iii = 0; iii < items.length; iii++)
            {
              if (items[iii].getChecked() == true)
                checked++;
            }
            if (checked == 0)
              viewer.setChecked(childs[ii].getData(), false);
            else
            {
              viewer.setChecked(childs[ii].getData(), true);
              if (checked == items.length)
                viewer.setGrayed(childs[ii].getData(), false);
              else
                viewer.setGrayed(childs[ii].getData(), true);
            }
            topChecked += checked;
          }
          else if (childs[ii].getData() instanceof IndexItem)
          {
            totalItems++;
            if (childs[ii].getChecked() == true)
              topChecked++;
          }
        }
        if (topChecked == 0)
          viewer.setChecked(top[i].getData(), false);
        else
        {
          viewer.setChecked(top[i].getData(), true);
          if (topChecked == totalItems)
            viewer.setGrayed(top[i].getData(), false);
          else
            viewer.setGrayed(top[i].getData(), true);
        }
      }
    }
    
    tree.setRedraw(true);
  }
}
