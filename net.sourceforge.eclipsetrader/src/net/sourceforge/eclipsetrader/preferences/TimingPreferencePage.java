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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for chart data timings.
 * <p></p>
 * 
 * TODO: Better page design.
 * 
 * @author Marco Maccaferri
 */
public class TimingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private FieldEditor[] editor;
  
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
    composite.setLayout(new GridLayout());

    Composite entryTable = new Composite(composite, SWT.NONE);
    entryTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _v.add(new BooleanFieldEditor("net.sourceforge.eclipsetrader.timing.session1", "Regular Session Time", entryTable));
    _v.add(new TimeFieldEditor("net.sourceforge.eclipsetrader.timing.startTime1", "Start", entryTable));
    _v.add(new TimeFieldEditor("net.sourceforge.eclipsetrader.timing.stopTime1", "End", entryTable));

    _v.add(new BooleanFieldEditor("net.sourceforge.eclipsetrader.timing.session2", "AfterHours Session Time", entryTable));
    _v.add(new TimeFieldEditor("net.sourceforge.eclipsetrader.timing.startTime2", "Start", entryTable));
    _v.add(new TimeFieldEditor("net.sourceforge.eclipsetrader.timing.stopTime2", "End", entryTable));

    _v.add(new StringFieldEditor("net.sourceforge.eclipsetrader.rtchart.period", "Intraday Chart Period", 3, entryTable));
    ((StringFieldEditor)_v.lastElement()).fillIntoGrid(entryTable, 5);

    // Must be here, otherwise the layout will be replaced by FieldEditors.
    GridLayout gd = new GridLayout(5, false);
    gd.marginWidth = 0;
    gd.marginHeight = 0;
    entryTable.setLayout(gd);
    
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
