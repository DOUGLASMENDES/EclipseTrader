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
package net.sourceforge.eclipsetrader.ui.views.trading;

import java.util.Vector;

import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for Book / Level II data view.
 * <p></p>
 */
public class BookPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  
  public void init(IWorkbench workbench) 
  {
    //Initialize the preference store we wish to use
    setPreferenceStore(ViewsPlugin.getDefault().getPreferenceStore());
  }

  /*
   * @see PreferencePage#createContents(Composite)
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

    _v.add(new BooleanFieldEditor("book.group_prices", "Group Prices", entryTable));
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.text_color", "Text Color", entryTable));        
    _v.add(new ColorFieldEditor("book.background", "Row Background", entryTable));        
    _v.add(new BooleanFieldEditor("book.hilight_variations", "Hilight Price Variations", entryTable));
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.positive_value_color", "Positive Value Color", entryTable));        
    _v.add(new ColorFieldEditor("book.negative_value_color", "Negative Value Color", entryTable));        
    _v.add(new BooleanFieldEditor("book.colorize_levels", "Colorize Price Levels", entryTable));        
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.level1_color", "Price Level 1 Color", entryTable));        
    _v.add(new ColorFieldEditor("book.level2_color", "Price Level 2 Color", entryTable));        
    _v.add(new ColorFieldEditor("book.level3_color", "Price Level 3 Color", entryTable));        
    _v.add(new ColorFieldEditor("book.level4_color", "Price Level 4 Color", entryTable));        
    _v.add(new ColorFieldEditor("book.level5_color", "Price Level 5 Color", entryTable));        

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

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  public boolean performOk() 
  {
    for (int i = 0; i < editor.length; i++)
      editor[i].store();
    return super.performOk();
  }
}
