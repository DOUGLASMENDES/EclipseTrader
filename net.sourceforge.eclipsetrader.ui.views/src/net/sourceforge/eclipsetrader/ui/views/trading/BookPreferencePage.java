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

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
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

    _v.add(new BooleanFieldEditor("book.group_prices", Messages.getString("BookPreferencePage.groupPrices"), entryTable)); //$NON-NLS-1$ //$NON-NLS-2$
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.text_color", Messages.getString("BookPreferencePage.textColor"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.background", Messages.getString("BookPreferencePage.rowBackground"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new BooleanFieldEditor("book.hilight_variations", Messages.getString("BookPreferencePage.hilightPriceVariations"), entryTable)); //$NON-NLS-1$ //$NON-NLS-2$
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.positive_value_color", Messages.getString("BookPreferencePage.positiveValueColor"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.negative_value_color", Messages.getString("BookPreferencePage.negativeValueColor"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new BooleanFieldEditor("book.colorize_levels", Messages.getString("BookPreferencePage.colorizePriceLevels"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    ((BooleanFieldEditor)_v.elementAt(_v.size() - 1)).fillIntoGrid(entryTable, 2);
    _v.add(new ColorFieldEditor("book.level1_color", Messages.getString("BookPreferencePage.level1"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.level2_color", Messages.getString("BookPreferencePage.level2"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.level3_color", Messages.getString("BookPreferencePage.level3"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.level4_color", Messages.getString("BookPreferencePage.level4"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$
    _v.add(new ColorFieldEditor("book.level5_color", Messages.getString("BookPreferencePage.level5"), entryTable));         //$NON-NLS-1$ //$NON-NLS-2$

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
