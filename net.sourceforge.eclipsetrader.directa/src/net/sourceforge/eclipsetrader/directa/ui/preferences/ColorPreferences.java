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

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 */
public class ColorPreferences extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;

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
    ((Group)group).setText("Modulo Ordini");
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    _v.add(new ColorFieldEditor("trading.background_color", "Sfondo", group));
    _v.add(new ColorFieldEditor("trading.text_color", "Colore Testo", group));
    _v.add(new ColorFieldEditor("trading.values_color", "Colore Valori", group));

    group = new Group(entryTable, SWT.NONE);
    ((Group)group).setText("Stato Ordini");
    group.setLayout(new GridLayout(1, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group = new Composite(group, SWT.NULL);
    group.setLayout(new GridLayout(2, false));
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    _v.add(new ColorFieldEditor("orders.background_color", "Sfondo", group));
    _v.add(new ColorFieldEditor("orders.text_color", "Colore Test", group));

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
    
    return super.performOk();
  }
}
