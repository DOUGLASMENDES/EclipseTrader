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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MappingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
  private List availableColumns;
  private List displayColumns;
  // Set column descriptions
  public static String[] columnDescriptions = {
      "Codice", "Ticker", "Descrizione", "Ultimo Prezzo", "Variazione %", "Denaro", "Q.tà Denaro",
      "Lettera", "Q.ta Lettera", "Volume", "Quantità minima", "Valore di Mercato", "Q.tà Posseduta",
      "Pagato", "Valore Posseduto", "Ricavo", "Prezzo Apertura", "Prezzo Massimo", "Prezzo Minimo", "Chiusura Prec.", "Orario"
      };
  
  public void init(IWorkbench workbench) {
    //Initialize the preference store we wish to use
  }

  /*
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent)
  {
    // Crea un composite grande come tutta la pagina
    Composite entryTable = new Composite(parent, SWT.NULL);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.grabExcessHorizontalSpace = true;
    entryTable.setLayoutData(data);
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    entryTable.setLayout(layout);     

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
    return super.performOk();
  }
}
