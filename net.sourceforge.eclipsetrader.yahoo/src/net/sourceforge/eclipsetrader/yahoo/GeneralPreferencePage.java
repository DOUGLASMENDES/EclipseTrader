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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
  FieldEditor[] editor; 
  
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

    Group group1 = new Group(entryTable, SWT.NONE);
    group1.setText("Data Streamer");
    group1.setLayout(new GridLayout());
    group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Composite composite1 = new Composite(group1, SWT.NULL);
    composite1.setLayout(new GridLayout(2, false));
    composite1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
    _v.add(new StringFieldEditor("yahoo.refresh", "Auto Refresh Interval", 3, composite1));
    _v.add(new StringFieldEditor("yahoo.url", "CSV Download URL", composite1));

    Group group2 = new Group(entryTable, SWT.NONE);
    group2.setText("Charts");
    group2.setLayout(new GridLayout());
    group2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Composite composite2 = new Composite(group2, SWT.NULL);
    composite2.setLayout(new GridLayout());
    composite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
    _v.add(new StringFieldEditor("yahoo.charts.url", "CSV Download URL", composite2));

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

  public Button createButton(Composite parent, String text, String action)
  {
    Button b = new Button(parent, SWT.PUSH);
    b.setText(text);
    b.setData(action);
    return b;
  }

  public Button createButton(Composite parent, String text, String action, String tooltipText)
  {
    Button b = new Button(parent, SWT.PUSH);
    b.setText(text);
    b.setData(action);
    b.setToolTipText(tooltipText);
    return b;
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

    return super.performOk();
  }
}
