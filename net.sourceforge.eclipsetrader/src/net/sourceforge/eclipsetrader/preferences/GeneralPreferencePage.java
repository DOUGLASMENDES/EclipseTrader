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
package net.sourceforge.eclipsetrader.preferences;

import java.util.Comparator;
import java.util.Vector;

import net.sourceforge.eclipsetrader.TraderPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for data providers plugins.
 * <p></p>
 */
public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  private Combo dataProvider;
  private Combo bookDataProvider;
  private Combo chartProvider;
  private Combo newsProvider;
  
  public void init(IWorkbench workbench) 
  {
    // Initialize the preference store we wish to use
    setPreferenceStore(TraderPlugin.getDefault().getPreferenceStore());
  }

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

    Label label = new Label(entryTable, SWT.NONE);
    label.setText("Stock Quotes");
    label.setLayoutData(new GridData());
    Combo combo = new Combo(entryTable, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.dataProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    dataProvider = combo;

    label = new Label(entryTable, SWT.NONE);
    label.setText("Level II / Market Depth");
    label.setLayoutData(new GridData());
    combo = new Combo(entryTable, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.bookDataProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    bookDataProvider = combo;

    label = new Label(entryTable, SWT.NONE);
    label.setText("Charts");
    label.setLayoutData(new GridData());
    combo = new Combo(entryTable, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.chartDataProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    chartProvider = combo;

    label = new Label(entryTable, SWT.NONE);
    label.setText("News");
    label.setLayoutData(new GridData());
    combo = new Combo(entryTable, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.newsProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    newsProvider = combo;

    // Perform operations common to all field editors
    editor = new FieldEditor[_v.size()];
    for (int i = 0; i < _v.size(); i++)
    {
      editor[i] = (FieldEditor)_v.elementAt(i);
      editor[i].setPreferencePage(this);
      editor[i].setPreferenceStore(getPreferenceStore());
      editor[i].load();
    }

    return entryTable;
  }
  
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  /** 
   * Method declared on IPreferencePage. Save the
   * author name to the preference store.
   */
  public boolean performOk() 
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    IPreferenceStore ps = getPreferenceStore(); 
    ps.setValue("net.sourceforge.eclipsetrader.dataProvider", getComboValue(dataProvider));
    ps.setValue("net.sourceforge.eclipsetrader.bookDataProvider", getComboValue(bookDataProvider));
    ps.setValue("net.sourceforge.eclipsetrader.chartDataProvider", getComboValue(chartProvider));
    ps.setValue("net.sourceforge.eclipsetrader.newsProvider", getComboValue(newsProvider));

    return super.performOk();
  }
  
  private String getComboValue(Combo combo)
  {
    String value = "";
    if (combo.getSelectionIndex() != -1)
      value = (String)combo.getData(String.valueOf(combo.getSelectionIndex()));
    return value;
  }

  private void addPluginList(String ep, Combo combo)
  {
    Vector v = new Vector();

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
        v.add(members[i]);
    }

    // Riordina alfabeticamente
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        IConfigurationElement item1 = (IConfigurationElement)o1;
        IConfigurationElement item2 = (IConfigurationElement)o2;
        return item1.getAttribute("label").compareTo(item2.getAttribute("label"));
      }
    });

    // Inserisce l'elenco nel combo impostando l'id nei dati del controllo
    String id = getPreferenceStore().getString(ep);
    for (int i = 0; i < v.size(); i++)
    {
      IConfigurationElement member = (IConfigurationElement)v.elementAt(i);
      combo.add(member.getAttribute("label"));
      combo.setData(String.valueOf(i), member.getAttribute("id"));
      if (id.equalsIgnoreCase(member.getAttribute("id")) == true)
        combo.setText(member.getAttribute("label"));
    }
  }
}
