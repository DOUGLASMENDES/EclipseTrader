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
import org.eclipse.core.runtime.IExtension;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  private Combo dataProvider;
  private Combo chartProvider;
  
  public void init(IWorkbench workbench) 
  {
    // Initialize the preference store we wish to use
    setPreferenceStore(TraderPlugin.getDefault().getPreferenceStore());
  }

  protected Control createContents(Composite parent)
  {
    Vector _v = new Vector();
    
    Composite entryTable = new Composite(parent, SWT.NULL);
    entryTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    entryTable.setLayout(gridLayout);

    Group group = new Group(entryTable, SWT.NONE);
    group.setText("Data Providers");
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label label = new Label(group, SWT.NONE);
    label.setText("Stock Quotes");
    label.setLayoutData(new GridData());
    Combo combo = new Combo(group, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.dataProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    dataProvider = combo;

    label = new Label(group, SWT.NONE);
    label.setText("Charts");
    label.setLayoutData(new GridData());
    combo = new Combo(group, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    addPluginList("net.sourceforge.eclipsetrader.chartDataProvider", combo);
    combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
    chartProvider = combo;

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
    ps.setValue("net.sourceforge.eclipsetrader.chartDataProvider", getComboValue(chartProvider));

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
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint(ep);
    if (extensionPoint == null)
    {
      combo.add("NULL");
      combo.setText("NULL");
      return;
    }

    // Elenca le estensioni presenti
    Vector v = new Vector();
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    for (int i = 0; i < members.length; i++)
      v.add(members[i]);

    // Riordina alfabeticamente
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) 
      {
        IConfigurationElement item1 = (IConfigurationElement)o1;
        IConfigurationElement item2 = (IConfigurationElement)o2;
        return item1.getDeclaringExtension().getLabel().compareTo(item2.getDeclaringExtension().getLabel());
      }
    });

    // Inserisce l'elenco nel combo impostando l'id nei dati del controllo
    String id = getPreferenceStore().getString(ep);
    for (int i = 0; i < v.size(); i++)
    {
      IConfigurationElement member = (IConfigurationElement)v.elementAt(i);
      IExtension extension = member.getDeclaringExtension();
      combo.add(extension.getLabel());
      combo.setData(String.valueOf(i), member.getAttribute("id"));
      if (id.equalsIgnoreCase(member.getAttribute("id")) == true)
        combo.setText(extension.getLabel());
    }
  }
}
