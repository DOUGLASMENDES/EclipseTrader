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

import java.util.Vector;

import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.internal.Messages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for startup and shutdown preferences.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class StartupPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
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

    Composite composite = new Composite(parent, SWT.NULL);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.grabExcessHorizontalSpace = true;
    composite.setLayoutData(data);
    GridLayout layout = new GridLayout();
    composite.setLayout(layout);

    Composite entryTable = new Composite(composite, SWT.NONE);
    entryTable.setLayout(new GridLayout(2, false));
    entryTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _v.add(new BooleanFieldEditor("net.sourceforge.eclipsetrader.promptOnExit", Messages.getString("StartupPreferencePage.confirmExit"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);

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
  
  private String getComboValue(Combo combo)
  {
    String value = ""; //$NON-NLS-1$
    if (combo.getSelectionIndex() != -1)
      value = (String)combo.getData(String.valueOf(combo.getSelectionIndex()));
    return value;
  }
}
