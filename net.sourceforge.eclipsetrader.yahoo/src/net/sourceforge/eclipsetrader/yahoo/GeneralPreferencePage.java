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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
  private Button button1, button2, button3, button4, button5, button6;
  
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

    Composite group = new Group(entryTable, SWT.NONE);
    ((Group)group).setText("Data Streamer");
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    _v.add(new StringFieldEditor("yahoo.refresh", "Auto Refresh Interval", 3, group));
    _v.add(new StringFieldEditor("yahoo.quote", "Quotes Download URL", group));
    Label label = new Label(group, SWT.NONE);
    label.setText("Use Field");
    label.setLayoutData(new GridData());
    Composite composite = new Composite(group, SWT.NULL);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    composite.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
    composite.setLayoutData(gridData);
    button1 = new Button(composite, SWT.RADIO);
    button1.setText("Symbol");
    button1.setLayoutData(new GridData());
    button2 = new Button(composite, SWT.RADIO);
    button2.setText("Ticker");
    button2.setLayoutData(new GridData());
    button3 = new Button(composite, SWT.CHECK);
    button3.setText("Use mapping");
    button3.setLayoutData(new GridData());

    group = new Group(entryTable, SWT.NONE);
    ((Group)group).setText("Charts");
    group.setLayout(new GridLayout());
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    _v.add(new StringFieldEditor("yahoo.charts.url", "CSV Download URL", group));
    _v.add(new StringFieldEditor("NEW_CHART_YEARS", "Years to Download", 3, group));
    label = new Label(group, SWT.NONE);
    label.setText("Use Field");
    label.setLayoutData(new GridData());
    composite = new Composite(group, SWT.NULL);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    composite.setLayout(gridLayout);
    gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
    composite.setLayoutData(gridData);
    button4 = new Button(composite, SWT.RADIO);
    button4.setText("Symbol");
    button4.setLayoutData(new GridData());
    button5 = new Button(composite, SWT.RADIO);
    button5.setText("Ticker");
    button5.setLayoutData(new GridData());
    button6 = new Button(composite, SWT.CHECK);
    button6.setText("Use mapping");
    button6.setLayoutData(new GridData());
    
    // Set the individual buttons state
    IPreferenceStore ps = getPreferenceStore();
    if (ps.getInt("yahoo.field") == 0)
      button1.setSelection(true);
    else if (ps.getInt("yahoo.field") == 1)
      button2.setSelection(true);
    if (ps.getBoolean("yahoo.mapping") == true)
      button3.setSelection(true);
    if (ps.getInt("yahoo.charts.field") == 0)
      button4.setSelection(true);
    else if (ps.getInt("yahoo.charts.field") == 1)
      button5.setSelection(true);
    if (ps.getBoolean("yahoo.charts.mapping") == true)
      button6.setSelection(true);

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

  /** 
   * Method declared on IPreferencePage. Save the
   * author name to the preference store.
   */
  public boolean performOk() 
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    IPreferenceStore ps = getPreferenceStore();
    if (button1.getSelection() == true)
      ps.setValue("yahoo.field", 0);
    else if (button2.getSelection() == true)
      ps.setValue("yahoo.field", 1);
    ps.setValue("yahoo.mapping", button3.getSelection());
    if (button4.getSelection() == true)
      ps.setValue("yahoo.charts.field", 0);
    else if (button5.getSelection() == true)
      ps.setValue("yahoo.charts.field", 1);
    ps.setValue("yahoo.charts.mapping", button6.getSelection());

    return super.performOk();
  }
}
