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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.sourceforge.eclipsetrader.ExtendedData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.IDataUpdateListener;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Marco Maccaferri
 */
public class PortfolioView extends ViewPart implements ControlListener, IDataUpdateListener, IPropertyChangeListener, ISelectionProvider 
{
  private static Table table;
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
  private long lastUpdate = System.currentTimeMillis();
  private Timer timerDaemon = new Timer();
  private int dragColumn = -1;
  private Composite parent;

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
    this.parent = parent;
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();

    // Colors
    textForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.text_color")); //$NON-NLS-1$
    evenBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.even_row_background")); //$NON-NLS-1$
    oddBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.odd_row_background")); //$NON-NLS-1$
    totalBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.total_row_background")); //$NON-NLS-1$
    negativeForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.negative_value_color")); //$NON-NLS-1$
    positiveForeground = new Color(Display.getCurrent(), PreferenceConverter.getColor(ViewsPlugin.getDefault().getPreferenceStore(), "portfolio.positive_value_color")); //$NON-NLS-1$

    // Columns width
    String[] w = pref.getString("portfolio.columnWidth").split(",");
    for (int i = 0; i < w.length && i < columnWidth.length; i++)
      columnWidth[i] = Integer.parseInt(w[i]);
    
    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    table.setLayoutData(data);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setBackground(parent.getBackground());
    
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
        if ((dragColumn == 12 || dragColumn == 13 || dragColumn == 15) && data.getQuantity() != 0)
          event.data = "S;" + data.getSymbol() + ";" + data.getTicker() + ";" + data.getQuantity() + ";" + data.getLastPrice(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        else
          event.data = "B;" + data.getSymbol() + ";" + data.getTicker() + ";" + data.getMinimumQuantity() + ";" + data.getLastPrice(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
      public void dragFinished(DragSourceEvent event) 
      {
      }
    });    

    table.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
      }
      public void mouseDown(MouseEvent e) {
        dragColumn = getColumn(e.x);
      }
      public void mouseUp(MouseEvent e) {
        dragColumn = -1;
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
      column.addControlListener(this);
    }
    updateView();
    
    createContextMenu();
    
    if (TraderPlugin.getDataProvider() != null)
      TraderPlugin.getDataProvider().addDataListener(this);
    ViewsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    
    // Schedula un timer ogni secondo per aggiornare la tabella anche in assenza
    // di dati dal data provider.
    timerDaemon.schedule(new TimerTask() {
      public void run()
      {
        if ((System.currentTimeMillis() - lastUpdate) >= 15000)
          asyncUpdateView();
      }
    }, 2000, 1000);
    System.out.println(this.getClass() + ": createPartControl"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    parent.setFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    if (TraderPlugin.getDataProvider() != null)
      TraderPlugin.getDataProvider().removeDataListener(this);

    // Remove the property change listeners
    ViewsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);

    // Save the columns width
    String value = "";
    for (int i = 0; i < columnWidth.length; i++)
      value += String.valueOf(columnWidth[i]) + ",";
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    pref.setValue("portfolio.columnWidth", value);
    
    super.dispose();
  }
  
  public void openHistoryChart() 
  {
    String CHART_ID = "net.sourceforge.eclipsetrader.ui.views.ChartView"; //$NON-NLS-1$

    IBasicData data = TraderPlugin.getData()[table.getSelectionIndex()];
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    for (int i = 1;; i++)
    {
      IViewReference ref = page.findViewReference(CHART_ID, String.valueOf(i));
      if (ref == null)
      {
        try {
          ViewsPlugin.getDefault().getPreferenceStore().setValue("chart." + String.valueOf(i), data.getSymbol());
          IViewPart view = page.showView(CHART_ID, String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE);
//          ((ChartView)view).setData(data);
        } catch (PartInitException e) {}
        break;
      }
    }
  }
  
  public void openRealtimeChart()
  {
    IExtendedData data = TraderPlugin.getData()[table.getSelectionIndex()];
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    for (int i = 1;; i++)
    {
      IViewReference ref = page.findViewReference("net.sourceforge.eclipsetrader.ui.views.RealtimeChart", String.valueOf(i)); //$NON-NLS-1$
      if (ref == null)
      {
        ViewsPlugin.getDefault().getPreferenceStore().setValue("rtchart." + String.valueOf(i), data.getSymbol()); //$NON-NLS-1$
        try {
          IViewPart view = page.showView("net.sourceforge.eclipsetrader.ui.views.RealtimeChart", String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
        } catch (PartInitException e) {}
        break;
      }
    }
  }
  
  public void openPriceBook()
  {
    IExtendedData data = TraderPlugin.getData()[table.getSelectionIndex()];
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    for (int i = 1;; i++)
    {
      IViewReference ref = page.findViewReference("net.sourceforge.eclipsetrader.ui.views.Book", String.valueOf(i)); //$NON-NLS-1$
      if (ref == null)
      {
        ViewsPlugin.getDefault().getPreferenceStore().setValue("book." + String.valueOf(i), data.getSymbol()); //$NON-NLS-1$
        try {
          IViewPart view = page.showView("net.sourceforge.eclipsetrader.ui.views.Book", String.valueOf(i), IWorkbenchPage.VIEW_ACTIVATE); //$NON-NLS-1$
        } catch (PartInitException e) {}
        break;
      }
    }
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
  
  public void addItem()
  {
    PortfolioDialog dlg = new PortfolioDialog();
    if (dlg.open() == PortfolioDialog.OK)
    {
      Vector v = new Vector(Arrays.asList(TraderPlugin.getData()));
      ExtendedData data = new ExtendedData();
      data.setSymbol(dlg.getSymbol());
      data.setTicker(dlg.getTicker());
      data.setDescription(dlg.getDescription());
      data.setMinimumQuantity(dlg.getMinimumQuantity());
      data.setQuantity(dlg.getQuantity());
      data.setPaid(dlg.getPaid());
      v.add(data);
      ExtendedData[] dataArray = new ExtendedData[v.size()];
      v.toArray(dataArray);
      TraderPlugin.getDataStore().update(dataArray);
      updateView();
    }
  }
  
  public void editItem()
  {
    int max = table.getItemCount() - 1;
    int index = table.getSelectionIndex();
    if (index == -1 || index >= max)
      return;

    IExtendedData data = TraderPlugin.getData()[index];
    PortfolioDialog dlg = new PortfolioDialog();
    dlg.setSymbol(data.getSymbol());
    dlg.setTicker(data.getTicker());
    dlg.setDescription(data.getDescription());
    dlg.setMinimumQuantity(data.getMinimumQuantity());
    dlg.setQuantity(data.getQuantity());
    dlg.setPaid(data.getPaid());
    if (dlg.open() == PortfolioDialog.OK)
    {
      data.setSymbol(dlg.getSymbol());
      data.setTicker(dlg.getTicker());
      data.setDescription(dlg.getDescription());
      data.setMinimumQuantity(dlg.getMinimumQuantity());
      data.setQuantity(dlg.getQuantity());
      data.setPaid(dlg.getPaid());
      updateView();
    }
  }
  
  public void deleteItem()
  {
    int max = table.getItemCount() - 1;
    int index = table.getSelectionIndex();
    if (index == -1 || index >= max)
      return;

    Vector v = new Vector(Arrays.asList(TraderPlugin.getData()));
    v.removeElementAt(index);
    ExtendedData[] dataArray = new ExtendedData[v.size()];
    v.toArray(dataArray);
    TraderPlugin.getDataStore().update(dataArray);
    updateView();
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
    int totalStock = 0;
    double totalPaid = 0, totalSell = 0;

    lastUpdate = System.currentTimeMillis();
    if (table.isDisposed() == true)
      return;
    table.setRedraw(false);

    IExtendedData[] data = TraderPlugin.getData();
    table.setItemCount(data.length + 1);
    
    int row;
    for (row = 0; row < data.length; row++)
    {
      IExtendedData pd = data[row];
      TableItem item = table.getItem(row);
      if ((row & 1) == 1)
        item.setBackground(oddBackground);
      else
        item.setBackground(evenBackground);
      for (int column = 0; column < table.getColumnCount(); column++)
      {
        item.setForeground(column, textForeground);
        int columnData = getColumnDataIndex(column);
        switch(columnData)
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
            if (pd.getLastPriceVariance() < 0)
              item.setForeground(column, negativeForeground);
            else if (pd.getLastPriceVariance() > 0)
              item.setForeground(column, positiveForeground);
            if (pd.getLastPrice() > 200)
              item.setText(column, nf.format(pd.getLastPrice()));
            else
              item.setText(column, pf.format(pd.getLastPrice()));
            break;
          case 4:
            if (pd.getLastPrice() != 0 && pd.getClosePrice() != 0)
            {
              double gain = (pd.getLastPrice() - pd.getClosePrice()) / pd.getClosePrice() * 100;
              pd.setChange(pcf.format(gain) + "%"); //$NON-NLS-1$
              if (gain < 0)
                item.setForeground(column, negativeForeground);
              else if (gain > 0)
                item.setForeground(column, positiveForeground);
            }
            item.setText(column, pd.getChange());
            break;
          case 5:
            if (pd.getBidPriceVariance() < 0)
              item.setForeground(column, negativeForeground);
            else if (pd.getBidPriceVariance() > 0)
              item.setForeground(column, positiveForeground);
            if (pd.getBidPrice() > 200)
              item.setText(column, nf.format(pd.getBidPrice()));
            else
              item.setText(column, pf.format(pd.getBidPrice()));
            break;
          case 6:
            if (pd.getBidSizeVariance() < 0)
              item.setForeground(column, negativeForeground);
            else if (pd.getBidSizeVariance() > 0)
              item.setForeground(column, positiveForeground);
            item.setText(column, nf.format(pd.getBidSize()));
            break;
          case 7:
            if (pd.getAskPriceVariance() < 0)
              item.setForeground(column, negativeForeground);
            else if (pd.getAskPriceVariance() > 0)
              item.setForeground(column, positiveForeground);
            if (pd.getAskPrice() > 200)
              item.setText(column, nf.format(pd.getAskPrice()));
            else
              item.setText(column, pf.format(pd.getAskPrice()));
            break;
          case 8:
            if (pd.getAskSizeVariance() < 0)
              item.setForeground(column, negativeForeground);
            else if (pd.getAskSizeVariance() > 0)
              item.setForeground(column, positiveForeground);
            item.setText(column, nf.format(pd.getAskSize()));
            break;
          case 9:
            item.setText(column, nf.format(pd.getVolume()));
            break;
          case 10:
            item.setText(column, nf.format(pd.getMinimumQuantity()));
            break;
          case 11:
            pd.setMarketValue(pd.getMinimumQuantity() * pd.getLastPrice());
            if (pd.getMarketValue() > 2000)
              item.setText(column, nf.format(pd.getMarketValue()));
            else
              item.setText(column, bpf.format(pd.getMarketValue()));
            break;
          case 12:
            if (pd.getQuantity() == 0)
              item.setText(column, ""); //$NON-NLS-1$
            else
              item.setText(column, nf.format(pd.getQuantity()));
            break;
          case 13:
            if (pd.getPaid() == 0)
              item.setText(column, ""); //$NON-NLS-1$
            else
              item.setText(column, pf.format(pd.getPaid()));
            break;
          case 14:
            pd.setValuePaid(pd.getPaid() * pd.getQuantity());
            if (pd.getPaid() != 0 && pd.getQuantity() != 0)
              item.setText(column, bpf.format(pd.getValuePaid()));
            else
              item.setText(column, ""); //$NON-NLS-1$
            break;
          case 15:
            if (pd.getPaid() != 0 && pd.getQuantity() != 0)
            {
              pd.setGain((pd.getLastPrice() - pd.getPaid()) / pd.getPaid() * 100);
              if (pd.getGain() < 0)
                item.setForeground(column, negativeForeground);
              else if (pd.getGain() > 0)
                item.setForeground(column, positiveForeground);
              item.setText(column, bpf.format((pd.getLastPrice() * pd.getQuantity()) - (pd.getPaid() * pd.getQuantity())) + " (" + pcf.format(pd.getGain()) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
              item.setText(column, ""); //$NON-NLS-1$
            break;
          case 16:
            if (pd.getOpenPrice() > 200)
              item.setText(column, nf.format(pd.getOpenPrice()));
            else
              item.setText(column, pf.format(pd.getOpenPrice()));
            break;
          case 17:
            if (pd.getHighPrice() > 200)
              item.setText(column, nf.format(pd.getHighPrice()));
            else
              item.setText(column, pf.format(pd.getHighPrice()));
            break;
          case 18:
            if (pd.getLowPrice() > 200)
              item.setText(column, nf.format(pd.getLowPrice()));
            else
              item.setText(column, pf.format(pd.getLowPrice()));
            break;
          case 19:
            if (pd.getClosePrice() > 200)
              item.setText(column, nf.format(pd.getClosePrice()));
            else
              item.setText(column, pf.format(pd.getClosePrice()));
            break;
          case 20:
            item.setText(column, pd.getTime());
            break;
        }
      }
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

    table.setRedraw(true);
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
    
    // Register menu for extension.
    getSite().registerContextMenu(menuMgr, this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection()
  {
    return null;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IDataStreamer)
   */
  public void dataUpdated(IBasicDataProvider ds)
  {
    asyncUpdateView();
  }

  public void dataUpdated(IBasicDataProvider dataProvider, IBasicData data)
  {
  }

  private int getColumnDataIndex(int index)
  {
    String data = (String)table.getColumn(index).getData();
    for (int i = 0; i < columnNames.length; i++)
    {
      if (data.equalsIgnoreCase(columnNames[i]) == true)
        return i;
    }
    return -1;
  }

  private int getDataColumnIndex(int index)
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
    else if (property.equalsIgnoreCase("portfolio") == true) //$NON-NLS-1$
      updateView();
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
      table.setRedraw(true);
    }
    if (property.startsWith("portfolio.") == true) //$NON-NLS-1$
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
}
