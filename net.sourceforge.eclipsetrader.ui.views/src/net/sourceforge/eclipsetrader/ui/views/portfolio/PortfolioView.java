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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sourceforge.eclipsetrader.IAlertData;
import net.sourceforge.eclipsetrader.IAlertSource;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.ICollectionObserver;
import net.sourceforge.eclipsetrader.IDataUpdateListener;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.AlertsDialog;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class PortfolioView extends ViewPart implements ControlListener, IDataUpdateListener, IPropertyChangeListener, ICollectionObserver 
{
  public static final String VIEW_ID = "net.sourceforge.eclipsetrader.ui.views.Portfolio";
  private Table table;
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat bpf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private Color evenBackground;
  private Color oddBackground;
  private Color totalBackground;
  private Color negativeForeground;
  private Color positiveForeground;
  private Color textForeground;
  private int dragColumn = -1;
  private TableItem totalsTableItem;
  private Runnable itemHilighter = new Runnable() {
    public void run()
    {
      if (!table.isDisposed())
      {
        for (int i = 0; i < table.getItemCount() - 1; i++)
        {
          PortfolioTableItem item = (PortfolioTableItem)table.getItem(i).getData();
          item.resetHilight();
        }
        table.getDisplay().timerExec(1000, itemHilighter);
      }
    }
  };

  // Set column names
  public static String[] columnNames = new String[] {
      "code", "ticker", "description", "price", "variance", "bid_price", "bid_qty", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
      "ask_price", "ask_qty", "volume", "min_quantity", "value", "qty", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
      "paid", "value2", "gain", "open_price", "high_price", "low_price", "close_price", "time" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
      };
  // Set column labels
  public static String[] columnLabels = {
      Messages.getString("PortfolioView.Code"), Messages.getString("PortfolioView.Ticker"), Messages.getString("PortfolioView.Description"), Messages.getString("PortfolioView.Price"), Messages.getString("PortfolioView.Variance"), Messages.getString("PortfolioView.Bid"), Messages.getString("PortfolioView.BidQty"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
      Messages.getString("PortfolioView.Ask"), Messages.getString("PortfolioView.AskQty"), Messages.getString("PortfolioView.Volume"), Messages.getString("PortfolioView.MinQty"), Messages.getString("PortfolioView.Value"), Messages.getString("PortfolioView.OwnedQty"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
      Messages.getString("PortfolioView.PaidPrice"), Messages.getString("PortfolioView.OwnedValue"), Messages.getString("PortfolioView.Gain"), Messages.getString("PortfolioView.Open"), Messages.getString("PortfolioView.Maximum"), Messages.getString("PortfolioView.Minimum"), Messages.getString("PortfolioView.Close"), Messages.getString("PortfolioView.Time") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
      };
  // Set column widths
  public static int columnWidth[] = { 80, 48, 148, 62, 52, 62, 62, 62, 62, 78, 45, 61, 45, 61, 61, 111, 62, 62, 62, 62, 60 };
  
  public PortfolioView()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

    bpf.setGroupingUsed(true);
    bpf.setMinimumIntegerDigits(1);
    bpf.setMinimumFractionDigits(2);
    bpf.setMaximumFractionDigits(2);

    pcf.setGroupingUsed(false);
    pcf.setMinimumIntegerDigits(1);
    pcf.setMinimumFractionDigits(2);
    pcf.setMaximumFractionDigits(2);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();

    // Colors
    textForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.text_color")); //$NON-NLS-1$
    evenBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.even_row_background")); //$NON-NLS-1$
    oddBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.odd_row_background")); //$NON-NLS-1$
    totalBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.total_row_background")); //$NON-NLS-1$
    negativeForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.negative_value_color")); //$NON-NLS-1$
    positiveForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.positive_value_color")); //$NON-NLS-1$

    // Columns width
    String[] w = pref.getString("portfolio.columnWidth").split(","); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < w.length && i < columnWidth.length; i++)
      columnWidth[i] = Integer.parseInt(w[i]);
    
    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    table.setLayoutData(data);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setBackground(parent.getBackground());
    table.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        TableItem[] items = table.getItems();
        if (e.item.equals(items[items.length - 1]) == false)
          getSite().getSelectionProvider().setSelection(new PortfolioSelection(((PortfolioTableItem)e.item.getData()).getData()));
        else
          getSite().getSelectionProvider().setSelection(new PortfolioSelection());
      }
    });
    
    DragSource dragSource = new DragSource(table, DND.DROP_COPY);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
    dragSource.setTransfer(types);
    dragSource.addDragListener(new DragSourceListener() {
      public void dragStart(DragSourceEvent event)
      {
        if (table.getSelectionIndex() == -1) 
          event.doit = false;
      }
      public void dragSetData(DragSourceEvent event) 
      {
        IExtendedData data = TraderPlugin.getData()[table.getSelectionIndex()];
        if ((dragColumn == 12 || dragColumn == 13 || dragColumn == 15) && data.getOwnedQuantity() != 0)
          event.data = "S;" + data.getSymbol() + ";" + data.getTicker() + ";" + data.getOwnedQuantity() + ";" + data.getLastPrice(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        else
          event.data = "B;" + data.getSymbol() + ";" + data.getTicker() + ";" + data.getMinimumQuantity() + ";" + data.getLastPrice(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
      public void dragFinished(DragSourceEvent event) 
      {
      }
    });    

    table.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        dragColumn = getColumn(e.x);
      }
    });

    // Add the columns to the table from the preference store
    String preferenceValue = ViewsPlugin.getDefault().getPreferenceStore().getString("portfolio.display"); //$NON-NLS-1$
    StringTokenizer tokenizer = new StringTokenizer(preferenceValue, ","); //$NON-NLS-1$
    int tokenCount = tokenizer.countTokens();
    String[] elements = new String[tokenCount];
    for (int i = 0; i < tokenCount; i++)
    {
      elements[i] = tokenizer.nextToken();
      TableColumn column = new TableColumn(table, (getColumnDataIndex(elements[i]) <= 2) ? SWT.LEFT : SWT.RIGHT, i);
      for (int m = 0; m < columnNames.length; m++)
      {
        if (elements[i].equalsIgnoreCase(columnNames[m]) == true)
        {
          column.setText(columnLabels[m]);
          column.setWidth(columnWidth[m]);
          break;
        }
      }
      column.setData(elements[i]);
      column.addControlListener(this);
    }
    
    createContextMenu();
    
    // Initial update
    for (Iterator iter = TraderPlugin.getDataStore().getStockwatchData().iterator(); iter.hasNext(); )
    {
      IExtendedData obj = (IExtendedData)iter.next();
      PortfolioTableItem item = new PortfolioTableItem(this, table, SWT.NONE);
      item.setNegativeForeground(negativeForeground);
      item.setPositiveForeground(positiveForeground);
      item.setData((IExtendedData)obj);
    }
    for (int i = 0; i < table.getItemCount(); i++)
      table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
    // Totals tow
    totalsTableItem = new TableItem(table, SWT.NONE);
    totalsTableItem.setBackground(totalBackground);
    updateTotals();
    
    // Item hilighter
    table.getDisplay().timerExec(1000, itemHilighter);
    
    // Add the property change listeners
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    ViewsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    TraderPlugin.getDataStore().getStockwatchData().addObserver(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    table.setFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    // Remove the property change listeners
    TraderPlugin.getDataStore().getStockwatchData().removeObserver(this);
    ViewsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);

    // Save the columns width
    String value = ""; //$NON-NLS-1$
    for (int i = 0; i < columnWidth.length; i++)
      value += String.valueOf(columnWidth[i]) + ","; //$NON-NLS-1$
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.setValue("portfolio.columnWidth", value); //$NON-NLS-1$
    
    super.dispose();
  }

  /**
   * Get the selected item in the portfolio table.
   * 
   * @return the selected item
   */
  public IExtendedData getSelectedItem()
  {
    if (table.getSelectionCount() != 0)
    {
      if (table.getSelection()[0].getData() instanceof PortfolioTableItem)
      {
        PortfolioTableItem item = (PortfolioTableItem)table.getSelection()[0].getData();
        return item.getData();
      }
    }
        
    return null;
  }
  
  public Table getTable()
  {
    return table;
  }
  
  public void moveUp()
  {
    int index = table.getSelectionIndex();
    if (index == -1 || index == 0)
      return;

    IExtendedData[] dataArray = TraderPlugin.getData();
    IExtendedData data = dataArray[index];
    dataArray[index] = dataArray[index - 1];
    dataArray[index - 1] = data;
    TraderPlugin.getDataStore().update(dataArray);
    updateView();
    table.setSelection(index - 1);
  }
  
  public void moveDown()
  {
    int max = table.getItemCount() - 1;
    int index = table.getSelectionIndex();
    if (index == -1 || index >= max - 1)
      return;

    IExtendedData[] dataArray = TraderPlugin.getData();
    IExtendedData data = dataArray[index];
    dataArray[index] = dataArray[index + 1];
    dataArray[index + 1] = data;
    TraderPlugin.getDataStore().update(dataArray);
    updateView();
    table.setSelection(index + 1);
  }
  
  public void editAlerts()
  {
    int max = table.getItemCount() - 1;
    int index = table.getSelectionIndex();
    if (index == -1 || index >= max)
      return;

    IExtendedData data = TraderPlugin.getData()[index];
    
    AlertsDialog dlg = new AlertsDialog();
    dlg.setData(data);
    if (dlg.open() == Dialog.OK)
      TraderPlugin.getDataStore().getStockwatchData().set(index, data);
  }
  
  public void clearAlerts()
  {
    IExtendedData[] data = TraderPlugin.getData();
    for (int i = 0; i < data.length; i++)
    {
      IAlertData[] ad = ((IAlertSource)data[i]).getAlerts();
      for (int n = 0; n < ad.length; n++)
      {
        if (ad[n].isTrigger() == true)
          ad[n].setAcknowledge(true);
      }
    }
    asyncUpdateView();
  }
  
  /**
   * Thread-safe view update.
   */
  public void asyncUpdateView()
  {
    if (table.isDisposed() == true)
      return;
    table.getDisplay().asyncExec(new Runnable() {
      public void run()  {
        updateView();
      }
    });
  }
  
  /**
   * Updates the view contents.
   * <p>This call is not thread-safe. Use asyncUpdateView if updating from a
   * thread or timer runnable.</p>
   */
  public void updateView()
  {
/*    int totalStock = 0;
    double totalPaid = 0, totalSell = 0;
    IExtendedData[] data = TraderPlugin.getData();
    if (data == null)
      return;

    lastUpdate = System.currentTimeMillis();
    if (table.isDisposed() == true)
      return;
    table.setRedraw(false);

    if (table.getItemCount() != (data.length + 1))
      table.setItemCount(data.length + 1);
    
    int row;
    for (row = 0; row < data.length; row++)
    {
      updateRow(row, data[row]);
      totalStock += data[row].getQuantity();
      totalPaid += data[row].getQuantity() * data[row].getPaid();
      totalSell += data[row].getQuantity() * data[row].getLastPrice();
    }

    // Riga contenente i totali
    double totalGain = totalSell - totalPaid;
    TableItem item = table.getItem(row);
    item.setBackground(totalBackground);
    for (int column = 0; column < table.getColumnCount(); column++)
      item.setText(column, ""); //$NON-NLS-1$

    int columnData = getDataColumnIndex(2);
    if (columnData != -1)
      item.setText(columnData, Messages.getString("PortfolioView.Total")); //$NON-NLS-1$
    columnData = getDataColumnIndex(12);
    if (columnData != -1)
      item.setText(columnData, nf.format(totalStock));
    columnData = getDataColumnIndex(15);
    if (columnData != -1)
    {
      if (totalGain < 0)
        item.setForeground(columnData, negativeForeground);
      else if (totalGain > 0)
        item.setForeground(columnData, positiveForeground);
      else
        item.setForeground(columnData, textForeground);
      item.setText(columnData, bpf.format(totalGain) + " (" + pcf.format(totalGain / totalPaid * 100) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    columnData = getDataColumnIndex(14);
    if (columnData != -1)
      item.setText(columnData, bpf.format(totalPaid));

    table.setRedraw(true);*/
  }
  
  /**
   * Updates the contents of a single row.
   * <p>This call is not thread-safe.</p>
   */
  private void updateRow(int row, IExtendedData data)
  {
    if (table.isDisposed() == true)
      return;

    TableItem tableItem = table.getItem(row);
    Color color = tableItem.getBackground();
    if (color != null && color != oddBackground && color != evenBackground)
      color.dispose();
    
    if ((row & 1) == 1)
      tableItem.setBackground(oddBackground);
    else
      tableItem.setBackground(evenBackground);
    
    if (data instanceof IAlertSource)
    {
      IAlertData[] alerts = ((IAlertSource)data).getAlerts();
      for (int i = 0; i < alerts.length; i++)
      {
        if (alerts[i].isTrigger() == true && alerts[i].isAcknowledge() == false)
        {
          if (alerts[i].isHilight())
            tableItem.setBackground(new Color(null, alerts[i].getHilightColor()));
        }
      }
    }
    
  }
 
  private void createContextMenu() 
  {
    // Create menu manager.
    MenuManager menuMgr = new MenuManager("#popupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager mgr) {
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
    
    // Create menu.
    Menu menu = menuMgr.createContextMenu(table);
    table.setMenu(menu);
    
    // Register the selection provider and context menu
    getSite().setSelectionProvider(new PortfolioSelectionProvider());
    getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IDataStreamer)
   */
  public void dataUpdated(IBasicDataProvider ds)
  {
    asyncUpdateView();
  }

  public void dataUpdated(IBasicDataProvider dataProvider, final IBasicData data)
  {
    if (table.isDisposed() == true)
      return;
    table.getDisplay().asyncExec(new Runnable() {
      public void run()  {
        IExtendedData[] d = TraderPlugin.getData();
        for (int i = 0; i < d.length; i++)
        {
          if (d[i].getSymbol().equalsIgnoreCase(data.getSymbol()) == true)
            updateRow(i, d[i]);
        }
      }
    });
  }

  public int getColumnDataIndex(int index)
  {
    String data = (String)table.getColumn(index).getData();
    for (int i = 0; i < columnNames.length; i++)
    {
      if (data.equalsIgnoreCase(columnNames[i]) == true)
        return i;
    }
    return -1;
  }

  public int getColumnDataIndex(String data)
  {
    for (int i = 0; i < columnNames.length; i++)
    {
      if (data.equalsIgnoreCase(columnNames[i]) == true)
        return i;
    }
    return -1;
  }

  public int getDataColumnIndex(int index)
  {
    TableColumn[] column = table.getColumns();
    for (int i = 0; i < column.length; i++)
    {
      String data = (String)column[i].getData();
      if (data.equalsIgnoreCase(columnNames[index]) == true)
        return i;
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();

    // Property changed for the Trader plugin
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.dataProvider") == true) //$NON-NLS-1$
    {
      if (TraderPlugin.getDataProvider() != null)
        TraderPlugin.getDataProvider().addDataListener(this);
    }

    // Property changed for the Views plugin
    if (property.equalsIgnoreCase("portfolio.text_color") == true) //$NON-NLS-1$
      textForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.text_color")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.even_row_background") == true) //$NON-NLS-1$
      evenBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.even_row_background")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.odd_row_background") == true) //$NON-NLS-1$
      oddBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.odd_row_background")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.total_row_background") == true) //$NON-NLS-1$
      totalBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.total_row_background")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.negative_value_color") == true) //$NON-NLS-1$
      negativeForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.negative_value_color")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.positive_value_color") == true) //$NON-NLS-1$
      positiveForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.positive_value_color")); //$NON-NLS-1$
    else if (property.equalsIgnoreCase("portfolio.display") == true) //$NON-NLS-1$
    {
      table.setRedraw(false);
      for (int column = table.getColumnCount() - 1; column >= 0; column--)
        table.getColumn(column).dispose();
      
      String preferenceValue = ViewsPlugin.getDefault().getPreferenceStore().getString("portfolio.display"); //$NON-NLS-1$
      StringTokenizer tokenizer = new StringTokenizer(preferenceValue, ","); //$NON-NLS-1$
      int tokenCount = tokenizer.countTokens();
      String[] elements = new String[tokenCount];
      for (int i = 0; i < tokenCount; i++)
      {
        elements[i] = tokenizer.nextToken();
        TableColumn column = new TableColumn(table, SWT.RIGHT, i);
        for (int m = 0; m < columnNames.length; m++)
        {
          if (elements[i].equalsIgnoreCase(columnNames[m]) == true)
          {
            column.setText(columnLabels[m]);
            column.setWidth(columnWidth[m]);
            break;
          }
        }
        column.setData(elements[i]);
        if (getColumnDataIndex(i) <= 2)
          column.setAlignment(SWT.LEFT);
      }
      for (int i = 0; i < table.getItemCount(); i++)
      {
        PortfolioTableItem tableItem = (PortfolioTableItem)table.getItem(i).getData();
        tableItem.update();
      }
      table.setRedraw(true);
    }
    if (property.equalsIgnoreCase("portfolio") == true || property.startsWith("portfolio.") == true) //$NON-NLS-1$
      updateView();
  }
  
  private int getColumn(int x)
  {
    int column = -1;
    
    ScrollBar bar = table.getHorizontalBar();
    if (bar != null)
      x += bar.getSelection();
    
    for (int i = 0, left = 0; i < table.getColumnCount(); i++)
    {
      TableColumn tc = table.getColumn(i);
      if (x >= left && x < (left + tc.getWidth()))
        column = i;
      left += tc.getWidth();
    }
    
    return getColumnDataIndex(column);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
   */
  public void controlMoved(ControlEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  public void controlResized(ControlEvent e)
  {
    if (e.getSource() instanceof TableColumn)
    {
      for (int i = 0; i < table.getColumnCount(); i++)
      {
        TableColumn column = table.getColumn(i);
        if (column == e.getSource())
        {
          columnWidth[getColumnDataIndex(i)] = column.getWidth();
          break;
        }
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ICollectionObserver#itemAdded(java.lang.Object)
   */
  public void itemAdded(Object obj)
  {
    if (obj instanceof IExtendedData)
    {
      int index = TraderPlugin.getDataStore().getStockwatchData().indexOf(obj);
      PortfolioTableItem item = new PortfolioTableItem(this, table, SWT.NONE, index);
      item.setNegativeForeground(negativeForeground);
      item.setPositiveForeground(positiveForeground);
      item.setData((IExtendedData)obj);

      for (int i = 0; i < table.getItemCount() - 1; i++)
        table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
      
      updateTotals();
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ICollectionObserver#itemRemoved(java.lang.Object)
   */
  public void itemRemoved(Object obj)
  {
    for (int i = 0; i < table.getItemCount() - 1; i++)
    {
      PortfolioTableItem item = (PortfolioTableItem)table.getItem(i).getData();
      if (item.getData() == obj)
      {
        table.getItem(i).dispose();
        if (i > 0)
          table.select(i - 1);
        break;
      }
    }
    for (int i = 0; i < table.getItemCount() - 1; i++)
      table.getItem(i).setBackground(((i & 1) == 1) ? oddBackground : evenBackground);
    updateTotals();
  }

  private void updateTotals()
  {
    int totalStock = 0;
    double totalPaid = 0, totalSell = 0;
    for (Iterator iter = TraderPlugin.getDataStore().getStockwatchData().iterator(); iter.hasNext(); )
    {
      IExtendedData obj = (IExtendedData)iter.next();
      totalStock += obj.getOwnedQuantity();
      totalPaid += obj.getOwnedQuantity() * obj.getPaid();
      totalSell += obj.getOwnedQuantity() * obj.getLastPrice();
    }

    int columnData = getDataColumnIndex(2);
    if (columnData != -1)
      totalsTableItem.setText(columnData, Messages.getString("PortfolioView.Total")); //$NON-NLS-1$
    columnData = getDataColumnIndex(12);
    if (columnData != -1)
      totalsTableItem.setText(columnData, nf.format(totalStock));
    columnData = getDataColumnIndex(15);
    if (columnData != -1)
    {
      double totalGain = totalSell - totalPaid;
      if (totalGain < 0)
        totalsTableItem.setForeground(columnData, negativeForeground);
      else if (totalGain > 0)
        totalsTableItem.setForeground(columnData, positiveForeground);
      else
        totalsTableItem.setForeground(columnData, textForeground);
      totalsTableItem.setText(columnData, bpf.format(totalGain) + " (" + pcf.format(totalGain / totalPaid * 100) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    columnData = getDataColumnIndex(14);
    if (columnData != -1)
      totalsTableItem.setText(columnData, bpf.format(totalPaid));
  }
}
