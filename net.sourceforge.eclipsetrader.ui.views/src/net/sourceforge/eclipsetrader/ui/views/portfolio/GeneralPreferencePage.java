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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Stockwatch columns preference page.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener
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
    setPreferenceStore(ViewsPlugin.getDefault().getPreferenceStore());
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
    
    // Crea il gruppo per la definizione delle colonne
    Group columns = new Group(entryTable, SWT.NONE);
    columns.setText("Columns");
    columns.setLayoutData(data);
    layout = new GridLayout();
    layout.numColumns = 3;
    columns.setLayout(layout);
    
    String[] visibleItems = getPreferenceStore().getString("portfolio.display").split(",");
    Vector v = new Vector();
    for (int i = 0; i < columnDescriptions.length; i++)
    {
      if (! isListed(PortfolioView.columnNames[i], visibleItems))
        v.add(columnDescriptions[i]);
    }
    String[] availableItems = new String[v.size()];
    v.toArray(availableItems);

    availableColumns = new List(columns, SWT.BORDER);
    availableColumns.setToolTipText("Available columns");
    availableColumns.setItems(availableItems);
    availableColumns.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    Composite buttonTable = new Composite(columns, SWT.NULL);
    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
    rowLayout.fill = true;
    buttonTable.setLayout(rowLayout);
    createButton(buttonTable, "->", "add").addSelectionListener(this);
    createButton(buttonTable, "<-", "remove").addSelectionListener(this);
    createButton(buttonTable, "Up", "up").addSelectionListener(this);
    createButton(buttonTable, "Down", "down").addSelectionListener(this);
    
    displayColumns = new List(columns, SWT.BORDER);
    displayColumns.setToolTipText("Display columns");
    displayColumns.setItems(convert(getPreferenceStore().getString("portfolio.display")));
    displayColumns.setLayoutData(new GridData(GridData.FILL_BOTH));

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
  
  public void widgetSelected(SelectionEvent e)
  {
    if (e.getSource() instanceof Button)
    {
      Button b = (Button)e.getSource();
      String action = (String)b.getData();
      if (action.equalsIgnoreCase("add") == true && availableColumns.getSelectionIndex() >= 0)
      {
        String item = availableColumns.getItem(availableColumns.getSelectionIndex());
        displayColumns.add(item);
        availableColumns.remove(availableColumns.getSelectionIndex());
      }
      else if (action.equalsIgnoreCase("remove") == true && displayColumns.getSelectionIndex() >= 0)
      {
        displayColumns.remove(displayColumns.getSelectionIndex());
        setAvailableItems();
      }
      else if (action.equalsIgnoreCase("up") == true && displayColumns.getSelectionIndex() >= 1)
      {
        int index = displayColumns.getSelectionIndex();
        String item = displayColumns.getItem(index);
        displayColumns.remove(index);
        displayColumns.add(item, index - 1);
        displayColumns.setSelection(index - 1);
      }
      else if (action.equalsIgnoreCase("down") == true && displayColumns.getSelectionIndex() >= 0 && displayColumns.getSelectionIndex() < (displayColumns.getItemCount() - 1))
      {
        int index = displayColumns.getSelectionIndex();
        String item = displayColumns.getItem(index);
        displayColumns.remove(index);
        displayColumns.add(item, index + 1);
        displayColumns.setSelection(index + 1);
      }
    }
  }
  
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  /**
   * Convert the supplied PREFERENCE_DELIMITER delimited
   * String to a String array.
   * @return String[]
   */
  private String[] convert(String preferenceValue) 
  {
    StringTokenizer tokenizer = new StringTokenizer(preferenceValue, ",");
    int tokenCount = tokenizer.countTokens();
    String[] elements = new String[tokenCount];

    for (int i = 0; i < tokenCount; i++) {
      String token = tokenizer.nextToken();
      for (int m = 0; m < PortfolioView.columnNames.length; m++)
      {
        if (token.equalsIgnoreCase(PortfolioView.columnNames[m]) == true)
        {
          token = columnDescriptions[m];
          break;
        }
      }
      elements[i] = token;
    }

    return elements;
  }
  
  public void setAvailableItems()
  {
    String[] visibleItems = displayColumns.getItems();
    for (int i = 0; i < visibleItems.length; i++) 
    {
      for (int m = 0; m < columnDescriptions.length; m++)
      {
        if (visibleItems[i].equalsIgnoreCase(columnDescriptions[m]) == true)
          visibleItems[i] = PortfolioView.columnNames[m];
      }
    }
    Vector v = new Vector();
    for (int i = 0; i < columnDescriptions.length; i++)
    {
      if (! isListed(PortfolioView.columnNames[i], visibleItems))
        v.add(columnDescriptions[i]);
    }
    String[] availableItems = new String[v.size()];
    v.toArray(availableItems);
    availableColumns.setItems(availableItems);
  }

  /** 
   * Method declared on IPreferencePage. Save the
   * author name to the preference store.
   */
  public boolean performOk() 
  {
    String[] elements = displayColumns.getItems();
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < elements.length; i++) 
    {
      for (int m = 0; m < columnDescriptions.length; m++)
      {
        if (elements[i].equalsIgnoreCase(columnDescriptions[m]) == true)
        {
          elements[i] = PortfolioView.columnNames[m];
          break;
        }
      }
      buffer.append(elements[i]);
      buffer.append(",");
    }
    getPreferenceStore().setValue("portfolio.display", buffer.toString());
    
    return super.performOk();
  }
  
  public boolean isListed(String s, String[] elements)
  {
    for (int i = 0; i < elements.length; i++)
    {
      if (s.equalsIgnoreCase(elements[i]) == true)
        return true;
    }
    return false;
  }
}
