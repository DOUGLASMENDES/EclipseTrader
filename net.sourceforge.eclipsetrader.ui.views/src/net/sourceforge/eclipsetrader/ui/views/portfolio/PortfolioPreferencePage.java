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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Preference page for the portfolio definition
 */
public class PortfolioPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener
{
  private static String FILE_NAME = "stocklist.xml";
  private Table table;
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private int editableColumn;
  private TableEditor editor;
  
  public PortfolioPreferencePage()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);
  }
  
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
    
    table = new Table(entryTable, SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = 300;
    data.widthHint = 200;
    table.setLayoutData(data);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setToolTipText("Double click on an item to edit");
    table.setBackground(entryTable.getBackground());

    TableColumn column = new TableColumn(table, SWT.LEFT, 0);
    column.setText("Codice");
    column.setWidth(80);
    column = new TableColumn(table, SWT.LEFT, 1);
    column.setText("Ticker");
    column.setWidth(48);
    column = new TableColumn(table, SWT.LEFT, 2);
    column.setText("Descrizione");
    column.setWidth(188);
    column = new TableColumn(table, SWT.RIGHT, 3);
    column.setText("Min.");
    column.setWidth(48);
    column = new TableColumn(table, SWT.RIGHT, 4);
    column.setText("Q.tà");
    column.setWidth(48);
    column = new TableColumn(table, SWT.RIGHT, 5);
    column.setText("Prezzo");
    column.setWidth(58);

    editor = new TableEditor(table);
    editor.horizontalAlignment = SWT.LEFT;
    editor.grabHorizontal = true;
    editor.minimumWidth = 50;
    
    table.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        Control oldEditor = editor.getEditor();
        if (oldEditor != null) oldEditor.dispose();

        editableColumn = -1;
        for (int i = 0, left = 0; i < table.getColumnCount(); i++)
        {
          TableColumn tc = table.getColumn(i);
          if (e.x >= left && e.x < (left + tc.getWidth()))
            editableColumn = i;
          left += tc.getWidth();
        }
        if (editableColumn != -1 && table.getSelectionIndex() != -1)
        {
          // The control that will be the editor must be a child of the Table
          if (editableColumn == 2)
          {
            Combo newEditor = new Combo(table, SWT.NONE);
            newEditor.setItems(loadStocklist());

            TableItem item = table.getItem(table.getSelectionIndex());
            newEditor.setText(item.getText(editableColumn));
            newEditor.addModifyListener(new ModifyListener() {
              public void modifyText(ModifyEvent e) {
                Combo text = (Combo)editor.getEditor();
                editor.getItem().setText(editableColumn, text.getText());
                setStockData(editor, text.getText());
              }
            });
            newEditor.setFocus();
            editor.setEditor(newEditor, item, editableColumn);
          }
          else
          {
            Text newEditor = new Text(table, SWT.BORDER);
            TableItem item = table.getItem(table.getSelectionIndex());
            newEditor.setText(item.getText(editableColumn));
            newEditor.addModifyListener(new ModifyListener() {
              public void modifyText(ModifyEvent e) {
                Text text = (Text)editor.getEditor();
                editor.getItem().setText(editableColumn, text.getText());
              }
            });
            newEditor.selectAll();
            newEditor.setFocus();
            editor.setEditor(newEditor, item, editableColumn);
          }
        }
      }

      public void mouseDown(MouseEvent e) {
        Control oldEditor = editor.getEditor();
        if (oldEditor != null) oldEditor.dispose();
      }

      public void mouseUp(MouseEvent e) {
      }
    });

    Composite buttonRow = new Composite(entryTable, SWT.NULL);
    layout = new GridLayout();
    layout.makeColumnsEqualWidth = true;
    layout.numColumns = 2;
    buttonRow.setLayout(layout);
    Button btn = createButton(buttonRow, "Add", "add");
    btn.setLayoutData(new GridData(GridData.FILL_BOTH));
    btn.addSelectionListener(this);
    btn = createButton(buttonRow, "Remove", "remove");
    btn.setLayoutData(new GridData(GridData.FILL_BOTH));
    btn.addSelectionListener(this);

    update();

    return entryTable;
  }
  
  public void update()
  {
    table.setItemCount(TraderPlugin.getData().length);
    for (int row = 0; row < TraderPlugin.getData().length; row++)
    {
      TableItem item = table.getItem(row);
      IExtendedData pd = TraderPlugin.getData()[row];
      for (int col = 0; col < table.getColumnCount(); col++)
        setTableText(pd, item, row, col);
    }
  }
  
  public void setTableText(IExtendedData pd, TableItem item, int row, int column)
  {
    item.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    switch(column)
    {
      case 0:
        item.setText(column, pd.getSymbol());
        break;
      case 1:
        item.setText(column, pd.getTicker());
        break;
      case 2:
        item.setText(column, pd.getDescription());
        break;
      case 3:
        if (pd.getMinimumQuantity() == 0)
          item.setText(column, "");
        else
          item.setText(column, nf.format(pd.getMinimumQuantity()));
        break;
      case 4:
        if (pd.getQuantity() == 0)
          item.setText(column, "");
        else
          item.setText(column, nf.format(pd.getQuantity()));
        break;
      case 5:
        if (pd.getPaid() == 0)
          item.setText(column, "");
        else
          item.setText(column, pf.format(pd.getPaid()));
        break;
    }
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
      
      if (action.equalsIgnoreCase("remove") == true)
      {
        Control oldEditor = editor.getEditor();
        if (oldEditor != null) oldEditor.dispose();
        int index = table.getSelectionIndex(); 
        if (index >= 0)
        {
          table.remove(index);
          if (index < table.getItemCount()) 
            table.setSelection(index);
          else
            table.setSelection(table.getItemCount() - 1);
          table.setFocus();
        }
      }
      else if (action.equalsIgnoreCase("add") == true)
      {
        Control oldEditor = editor.getEditor();
        if (oldEditor != null) oldEditor.dispose();
        table.setItemCount(table.getItemCount() + 1);
        table.setSelection(table.getItemCount() - 1);
        table.setFocus();
      }
    }
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
    Control oldEditor = editor.getEditor();
    if (oldEditor != null) oldEditor.dispose();

    Vector v = new Vector();
    for (int row = 0; row < table.getItemCount(); row++)
    {
      IExtendedData pd = TraderPlugin.createExtendedData();
      pd.setSymbol(table.getItem(row).getText(0));
      pd.setTicker(table.getItem(row).getText(1));
      pd.setDescription(table.getItem(row).getText(2));
      try {
        pd.setMinimumQuantity(nf.parse(table.getItem(row).getText(3)).intValue());
      } catch (Exception e) {}
      try {
        pd.setQuantity(nf.parse(table.getItem(row).getText(4)).intValue());
      } catch (Exception e) {}
      try {
        pd.setPaid(pf.parse(table.getItem(row).getText(5)).doubleValue());
      } catch (Exception e) {}
      v.add(pd);
    }
    IExtendedData[] arr = new IExtendedData[v.size()];
    v.toArray(arr);
    TraderPlugin.getDataStore().update(arr);
    update();
    ViewsPlugin.getDefault().getPreferenceStore().firePropertyChangeEvent("portfolio", null, null);
    
    return super.performOk();
  }


  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#performApply()
   */
  protected void performApply()
  {
    super.performApply();
    performOk();
  }

  private String[] loadStocklist()
  {
    Vector v = new Vector();
    InputStream is = null;
    
    // Attempt to read the map file from the workspace location
    File f = new File(Platform.getLocation().toFile(), FILE_NAME);
    if (f.exists() == true)
    {
      try {
        is = new FileInputStream(f);
      } catch (FileNotFoundException e) {}
    }
    // Attempt to read the default map file from the plugin's install location
    if (is == null)
    {
      try {
        is = ViewsPlugin.getDefault().openStream(new Path(FILE_NAME));
      } catch (IOException e) {}
    }
    
    if (is != null)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
          {
            NodeList parent = n.getChildNodes();
            for (int x = 0; x < parent.getLength(); x++)
            {
              Node node = parent.item(x);
              Node value = node.getFirstChild();
              if (value != null)
              {
                if (node.getNodeName().equalsIgnoreCase("description") == true)
                  v.addElement(value.getNodeValue());
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    // Sort the items
    java.util.Collections.sort(v, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((String)o1).compareTo((String)o2);
      }
    });

    String[] items = new String[v.size()];
    v.toArray(items);
    return items;
  }
  
  private void setStockData(TableEditor editor, String text)
  {
    InputStream is = null;
    
    // Attempt to read the map file from the workspace location
    File f = new File(Platform.getLocation().toFile(), FILE_NAME);
    if (f.exists() == true)
    {
      try {
        is = new FileInputStream(f);
      } catch (FileNotFoundException e) {}
    }
    // Attempt to read the default map file from the plugin's install location
    if (is == null)
    {
      try {
        is = ViewsPlugin.getDefault().openStream(new Path(FILE_NAME));
      } catch (IOException e) {}
    }
    
    if (is != null)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("data"))
          {
            NodeList parent = (NodeList)n;
            for (int x = 0; x < parent.getLength(); x++)
            {
              Node node = parent.item(x);
              Node value = node.getFirstChild();
              if (value != null)
              {
                if (node.getNodeName().equalsIgnoreCase("description") == true && value.getNodeValue().equalsIgnoreCase(text) == true)
                {
                  for (x = 0; x < parent.getLength(); x++)
                  {
                    node = parent.item(x);
                    value = node.getFirstChild();
                    if (value != null)
                    {
                      if (node.getNodeName().equalsIgnoreCase("symbol") == true)
                        editor.getItem().setText(0, value.getNodeValue());
                      if (node.getNodeName().equalsIgnoreCase("ticker") == true)
                        editor.getItem().setText(1, value.getNodeValue());
                      if (node.getNodeName().equalsIgnoreCase("min_quantity") == true)
                        editor.getItem().setText(3, value.getNodeValue());
                    }
                  }
                  return;
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
