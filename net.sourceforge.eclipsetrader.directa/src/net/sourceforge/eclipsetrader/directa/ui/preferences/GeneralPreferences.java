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
package net.sourceforge.eclipsetrader.directa.ui.preferences;

import java.util.Vector;

import net.sourceforge.eclipsetrader.directa.DirectaPlugin;

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
public class GeneralPreferences extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  private Button button;

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench)
  {
    // Initialize the preference store we wish to use
    setPreferenceStore(DirectaPlugin.getDefault().getPreferenceStore());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
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
    ((Group)group).setText("Impostazioni Server");
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _v.add(new StringFieldEditor("transact.server", "Server Transazioni", group));
    _v.add(new StringFieldEditor("streaming.server", "Server Quotazioni", group));

    group = new Group(entryTable, SWT.NONE);
    ((Group)group).setText("Grafici Intraday");
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    _v.add(new StringFieldEditor("backfill.server", "Server Backfill", group));
    _v.add(new StringFieldEditor("rtcharts.update", "Aggiornamento Grafici", 5, group));

    Label label = new Label(entryTable, SWT.NONE);
    label.setLayoutData(new GridData(GridData.FILL_VERTICAL));
    button = new Button(entryTable, SWT.CHECK);
    button.setText("Azzera i codici di login salvati");

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

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  protected void performDefaults()
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].loadDefault();
    
    super.performDefaults();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk()
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].store();

    if (button.getSelection() == true)
    {
      IPreferenceStore ps = getPreferenceStore();
      ps.setValue("user.name", "");
      ps.setValue("user.password", "");
    }
    
    return super.performOk();
  }
}
