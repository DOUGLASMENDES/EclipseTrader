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
package net.sourceforge.eclipsetrader.ui.views.news;

import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * News View colors preference page.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class ColorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private ColorFieldEditor[] colorEditor = new ColorFieldEditor[2];
  
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
    Composite entryTable = new Composite(parent, SWT.NULL);

    // Create a data that takes up the extra space in the dialog .
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.grabExcessHorizontalSpace = true;
    entryTable.setLayoutData(data);

    GridLayout layout = new GridLayout();
    entryTable.setLayout(layout);
    
    Composite colorComposite = new Composite(entryTable,SWT.NONE);
    colorComposite.setLayout(new GridLayout());
    colorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    colorEditor[0] = new ColorFieldEditor("news.color", "Text Color", colorComposite);        
    colorEditor[1] = new ColorFieldEditor("news.background", "Background Color", colorComposite);        
    for(int i = 0; i < colorEditor.length; i++)
    {
      colorEditor[i].setPreferencePage(this);
      colorEditor[i].setPreferenceStore(getPreferenceStore());
      colorEditor[i].load();
    }
    
    return entryTable;
  }
  
  /** 
   * Method declared on IPreferencePage. Save the
   * author name to the preference store.
   */
  public boolean performOk() 
  {
    for(int i = 0; i < colorEditor.length; i++)
      colorEditor[i].store();
    return super.performOk();
  }
}
