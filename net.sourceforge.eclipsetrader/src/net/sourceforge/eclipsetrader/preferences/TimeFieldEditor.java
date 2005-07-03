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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Customer FieldEditor for setting a time preference.<br>
 * <p></p>
 * 
 * TODO: Better implementation (with up/down arrows, am/pm indication, etc.)
 */
public class TimeFieldEditor extends FieldEditor
{
  private Composite textField;
  private Text hourField;
  private Text minuteField;
  
  public TimeFieldEditor(String name, String labelText, Composite parent)
  {
    init(name, labelText);
    createControl(parent);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
   */
  protected void adjustForNumColumns(int numColumns)
  {
    GridData gd = (GridData)textField.getLayoutData();
    gd.horizontalSpan = numColumns - 1;
    // We only grab excess space if we have to
    // If another field editor has more columns then
    // we assume it is setting the width.
    gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1; 
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
   */
  protected void doFillIntoGrid(Composite parent, int numColumns)
  {
    getLabelControl(parent);

    textField = getTextControl(parent);
    GridData gd = new GridData();
    gd.horizontalSpan = numColumns - 1;
    textField.setLayoutData(gd); 
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#doLoad()
   */
  protected void doLoad()
  {
    if (textField != null) 
    {
      int value = getPreferenceStore().getInt(getPreferenceName());
      hourField.setText(String.valueOf(value / 60));
      if ((value % 60) < 10)
        minuteField.setText("0" + String.valueOf(value % 60));
      else
        minuteField.setText(String.valueOf(value % 60));
    } 
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
   */
  protected void doLoadDefault()
  {
    if (textField != null) 
    {
      int value = getPreferenceStore().getDefaultInt(getPreferenceName());
      hourField.setText(String.valueOf(value / 60));
      if ((value % 60) < 10)
        minuteField.setText("0" + String.valueOf(value % 60));
      else
        minuteField.setText(String.valueOf(value % 60));
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#doStore()
   */
  protected void doStore()
  {
    int value = Integer.parseInt(hourField.getText()) * 60 + Integer.parseInt(minuteField.getText()); 
    getPreferenceStore().setValue(getPreferenceName(), value);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
   */
  public int getNumberOfControls()
  {
    return 2;
  }

  /**
   * Returns this field editor's text control.
   * <p>
   * The control is created if it does not yet exist
   * </p>
   *
   * @param parent the parent
   * @return the text control
   */
  public Composite getTextControl(Composite parent) 
  {
    if (textField == null) 
    {
      textField = new Composite(parent, SWT.NONE);
      textField.setLayout(new GridLayout(3, false));
      textField.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent event) 
        {
          textField = null;
          hourField = null;
          minuteField = null;
        }
      });
      
      hourField = new Text(textField, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
      hourField.setFont(parent.getFont());
      hourField.addKeyListener(new KeyAdapter() {
        public void keyReleased(org.eclipse.swt.events.KeyEvent e)
        {
          int value = Integer.parseInt(hourField.getText());
          if (value > 23) hourField.setText("23");
          else if (value < 0) hourField.setText("0");
        }
      });
      hourField.setTextLimit(2);
      GridData gd = new GridData();
      GC gc = new GC(hourField);
      try {
        Point extent = gc.textExtent("X");//$NON-NLS-1$
        gd.widthHint = 2 * extent.x;
      } finally {
        gc.dispose();
      } 
      hourField.setLayoutData(gd); 
      
      Label label = new Label(textField, SWT.NONE);
      label.setFont(parent.getFont());
      label.setText(":");
      
      minuteField = new Text(textField, SWT.SINGLE | SWT.BORDER);
      minuteField.setFont(parent.getFont());
      minuteField.addKeyListener(new KeyAdapter() {
        public void keyReleased(org.eclipse.swt.events.KeyEvent e)
        {
          int value = Integer.parseInt(minuteField.getText());
          if (value > 59) minuteField.setText("59");
          else if (value < 0) minuteField.setText("00");
        }
      });
      textField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) 
        {
          int value = Integer.parseInt(minuteField.getText());
          if (value < 10)
            minuteField.setText("0" + String.valueOf(value));
          else
            minuteField.setText(String.valueOf(value));
        }
      }); 
      minuteField.setTextLimit(2);
      gd = new GridData();
      gc = new GC(minuteField);
      try {
        Point extent = gc.textExtent("X");//$NON-NLS-1$
        gd.widthHint = 2 * extent.x;
      } finally {
        gc.dispose();
      } 
      minuteField.setLayoutData(gd); 
    } 
    else
      checkParent(textField, parent);

    return textField;
  } 
}
