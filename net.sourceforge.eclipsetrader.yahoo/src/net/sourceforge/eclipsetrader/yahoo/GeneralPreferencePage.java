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

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
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
 */
public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  private Combo newsSource;
  
  public void init(IWorkbench workbench) {
    //Initialize the preference store we wish to use
    setPreferenceStore(YahooPlugin.getDefault().getPreferenceStore());
  }

  /*
   * @see PreferencePage#createContents(Composite)
   */
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
    group.setText("Data Streamer");
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Composite composite = new Composite(group, SWT.NULL);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
    _v.add(new StringFieldEditor("yahoo.refresh", "Auto Refresh Interval", 3, composite));
    _v.add(new StringFieldEditor("yahoo.url", "CSV Download URL", composite));

    group = new Group(entryTable, SWT.NONE);
    group.setText("Charts");
    group.setLayout(new GridLayout());
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    composite = new Composite(group, SWT.NULL);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
    _v.add(new StringFieldEditor("yahoo.charts.url", "CSV Download URL", composite));

    group = new Group(entryTable, SWT.NONE);
    group.setText("News");
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Label label = new Label(group, SWT.NONE);
    label.setText("Source");
    label.setLayoutData(new GridData());
    newsSource = new Combo(group, SWT.BORDER|SWT.DROP_DOWN|SWT.READ_ONLY);
    newsSource.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));

    // Add the plugin names to the combo
    String id = getPreferenceStore().getString("net.sourceforge.eclipsetrader.yahoo.newsSource");
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.yahoo.newsSource");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        newsSource.add(members[i].getAttribute("label"), i);
        newsSource.setData(String.valueOf(i), members[i].getAttribute("id"));
        if (id.equalsIgnoreCase(members[i].getAttribute("id")) == true)
          newsSource.setText(members[i].getAttribute("label"));
      }
    }

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

  /** 
   * Method declared on IPreferencePage. Save the
   * author name to the preference store.
   */
  public boolean performOk() 
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    IPreferenceStore ps = getPreferenceStore(); 
    ps.setValue("net.sourceforge.eclipsetrader.yahoo.newsSource", getComboValue(newsSource));

    return super.performOk();
  }
  
  private String getComboValue(Combo combo)
  {
    String value = "";
    if (combo.getSelectionIndex() != -1)
      value = (String)combo.getData(String.valueOf(combo.getSelectionIndex()));
    return value;
  }
}
