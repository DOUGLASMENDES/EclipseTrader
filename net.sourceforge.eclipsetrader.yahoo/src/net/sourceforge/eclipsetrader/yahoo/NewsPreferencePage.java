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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Marco
 */
public class NewsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private CheckboxTableViewer viewer;
  private FieldEditor[] editor; 
  
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
    
    Label label = new Label(entryTable, SWT.NONE);
    label.setText("Available news sources:");
    label.setLayoutData(new GridData());

    viewer = CheckboxTableViewer.newCheckList(entryTable, SWT.BORDER);
    viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
    String id = getPreferenceStore().getString("net.sourceforge.eclipsetrader.yahoo.newsSource");
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.yahoo.newsSource");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        String s = members[i].getAttribute("label");
        viewer.add(s);
        viewer.setData(String.valueOf(i), members[i].getAttribute("id"));
        if (id.indexOf(members[i].getAttribute("id")) != -1)
          viewer.setChecked(s, true);
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
    String id = "";
    for(int i = 0; i < viewer.getTable().getItemCount(); i++)
    {
      if (viewer.getChecked(viewer.getElementAt(i)) == true)
      {
        if (id.length() > 0)
          id += ",";
        id += viewer.getData(String.valueOf(i));
      }
    }
    getPreferenceStore().setValue("net.sourceforge.eclipsetrader.yahoo.newsSource", id);

    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    return super.performOk();
  }
}
