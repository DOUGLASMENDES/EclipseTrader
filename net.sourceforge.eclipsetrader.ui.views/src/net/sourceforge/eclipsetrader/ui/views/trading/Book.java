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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookDataProvider;
import net.sourceforge.eclipsetrader.IBookUpdateListener;
import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

/**
 * Book / Level II data view.
 * <p></p>
 */
public class Book extends ViewPart implements IBookUpdateListener, ControlListener, IPropertyChangeListener 
{
  private Table table;
  private TrendBar trendBar;
  private Color background;
  private Color foreground;
  private Color negativeForeground;
  private Color positiveForeground;
  private boolean groupPrices = false;
  private boolean hilightVariations = false;
  private IBasicData data;
  private IBookData[] bid;
  private IBookData[] ask;
  private boolean colorizeLevels = true;
  private Color[] levelColor = new Color[5];
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
  private long lastUpdate = System.currentTimeMillis();
  private Timer timerDaemon;
  private int dragColumn = -1;
  private Composite info;
  private Label label1;
  private Label label2;
  private Label label3;
  private Label label4;
  private Label label5;
  private Label label6;
  private Composite parent;

  public Book()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

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
    
    // Read the preferences
    IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
    groupPrices = pref.getBoolean("book.group_prices");
    foreground = new Color(null, PreferenceConverter.getColor(pref, "book.text_color"));
    background = new Color(null, PreferenceConverter.getColor(pref, "book.background"));
    negativeForeground = new Color(null, PreferenceConverter.getColor(pref, "book.negative_value_color"));
    positiveForeground = new Color(null, PreferenceConverter.getColor(pref, "book.positive_value_color"));
    levelColor[0] = new Color(null, PreferenceConverter.getColor(pref, "book.level1_color"));
    levelColor[1] = new Color(null, PreferenceConverter.getColor(pref, "book.level2_color"));
    levelColor[2] = new Color(null, PreferenceConverter.getColor(pref, "book.level3_color"));
    levelColor[3] = new Color(null, PreferenceConverter.getColor(pref, "book.level4_color"));
    levelColor[4] = new Color(null, PreferenceConverter.getColor(pref, "book.level5_color"));
    hilightVariations = pref.getBoolean("book.hilight_variations");
    colorizeLevels = pref.getBoolean("book.colorize_levels");
    pref.addPropertyChangeListener(this);

    Composite entryTable = new Composite(parent, SWT.NULL);
    entryTable.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.verticalSpacing = 0;
    entryTable.setLayout(gridLayout);
    
    DropTarget target = new DropTarget(entryTable, DND.DROP_COPY);
    final TextTransfer textTransfer = TextTransfer.getInstance();
    Transfer[] types = new Transfer[] { textTransfer };
    target.setTransfer(types);
    target.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event) 
      {
        event.detail = DND.DROP_COPY;
      }
      public void dragOver(DropTargetEvent event) 
      {
        event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
      }
      public void dragOperationChanged(DropTargetEvent event) 
      {
      }
      public void dragLeave(DropTargetEvent event) 
      {
      }
      public void dropAccept(DropTargetEvent event) 
      {
      }
      public void drop(DropTargetEvent event) 
      {
        String[] item = ((String)event.data).split(";");
        String id = getViewSite().getSecondaryId();
        ViewsPlugin.getDefault().getPreferenceStore().setValue("book." + id, item[1]);
        setData();
      }
    });

    // Box informazioni sul titolo rappresentato
    info = new Composite(entryTable, SWT.NONE);
    gridLayout = new GridLayout(4, false);
    gridLayout.marginWidth = 3;
    gridLayout.marginHeight = 3;
    gridLayout.verticalSpacing = 2;
    gridLayout.horizontalSpacing = 10;
    info.setLayout(gridLayout);
    info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
    info.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e)
      {
        // Disegna il bordo inferiore
        Rectangle r = info.getClientArea();
        e.gc.setForeground(info.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        e.gc.drawLine(0, r.height - 1, r.width, r.height - 1);
      }
    });
    Label label = new Label(info, SWT.NONE);
    label.setText("Time:");
    label.setLayoutData(new GridData());
    label1 = new Label(info, SWT.RIGHT);
    label1.setText("00:00:00");
    label1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL));
    label = new Label(info, SWT.NONE);
    label.setText("Vol:");
    label.setLayoutData(new GridData());
    label2 = new Label(info, SWT.RIGHT);
    label2.setText("0");
    label2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.GRAB_HORIZONTAL));
    label = new Label(info, SWT.NONE);
    label.setText("Last:");
    label.setLayoutData(new GridData());
    label3 = new Label(info, SWT.RIGHT);
    label3.setText("0,0000");
    label3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("High:");
    label.setLayoutData(new GridData());
    label4 = new Label(info, SWT.RIGHT);
    label4.setText("0,0000");
    label4.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Chng:");
    label.setLayoutData(new GridData());
    label5 = new Label(info, SWT.RIGHT);
    label5.setText("0,00%");
    label5.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label = new Label(info, SWT.NONE);
    label.setText("Low:");
    label.setLayoutData(new GridData());
    label6 = new Label(info, SWT.RIGHT);
    label6.setText("0,0000");
    label6.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    // Trendbar
    trendBar = new TrendBar(entryTable, SWT.NONE);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 15;
    trendBar.setLayoutData(gd);

    // Tabella bid/ask
    table = new Table(entryTable, SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
    table.setLayoutData(new GridData(GridData.FILL_BOTH));
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setBackground(parent.getBackground());
    table.addControlListener(this);

    // Colonne della tabella
    TableColumn column = new TableColumn(table, SWT.RIGHT, 0);
    column.setWidth(0);
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 1);
    column.setText("#");
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 2);
    column.setText("Q.ty");
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 3);
    column.setText("Bid");
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 4);
    column.setText("Ask");
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 5);
    column.setText("Q.ty");
    column.setResizable(false);
    column = new TableColumn(table, SWT.CENTER, 6);
    column.setText("#");
    column.setResizable(false);

    // Listener per determinare la colonna in cui si e' premuto il 
    // pulsante del mouse
    table.addMouseListener(new MouseListener() 
    {
      public void mouseDoubleClick(MouseEvent e) {
      }
      public void mouseDown(MouseEvent e) 
      {
        dragColumn = -1;
        for (int i = 0, left = 0; i < table.getColumnCount(); i++)
        {
          TableColumn tc = table.getColumn(i);
          if (e.x >= left && e.x < (left + tc.getWidth()))
            dragColumn = i;
          left += tc.getWidth();
        }
      }
      public void mouseUp(MouseEvent e) {
      }
    });

    // Il book e' sorgente per il drag & drop
    DragSource dragSource = new DragSource(table, DND.DROP_COPY);
    dragSource.setTransfer(types);
    dragSource.addDragListener(new DragSourceListener() 
    {
      public void dragStart(DragSourceEvent event)
      {
        if (table.getSelectionIndex() == -1) 
          event.doit = false;
      }
      public void dragSetData(DragSourceEvent event) 
      {
        if (data instanceof IExtendedData)
        {
          IExtendedData ed = (IExtendedData)data;
          if (dragColumn > 3)
          {
            IBookData bd = (IBookData)ask[table.getSelectionIndex()];
            event.data = "S;" + data.getSymbol() + ";" + data.getTicker() + ";" + ed.getMinimumQuantity() + ";" + bd.getPrice();
          }
          else
          {
            IBookData bd = (IBookData)bid[table.getSelectionIndex()];
            event.data = "B;" + data.getSymbol() + ";" + data.getTicker() + ";" + ed.getMinimumQuantity() + ";" + bd.getPrice();
          }
        }
      }
      public void dragFinished(DragSourceEvent event) 
      {
      }
    });  

    TraderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    
    restoreData();
    System.out.println(this.getClass() + ": createPartControl");
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
    if (timerDaemon != null)
      timerDaemon.cancel();
    if (data != null && TraderPlugin.getBookDataProvider() != null)
    {
      IBookDataProvider _dp = TraderPlugin.getBookDataProvider();
      _dp.removeBookListener(data, this);
      _dp.stopBook(data);
    }
    TraderPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    ViewsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);

    super.dispose();
  }

  /**
   * Ripristina i dati memorizzati e aggiorna la visualizzazione
   */
  private void restoreData()
  {
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("book." + id);

    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(symbol) == true)
      {
        data = _tpData[i];
        break;
      }
    }
    
    if (data != null)
    {
      setPartName(data.getTicker() + " - Book");

      if (TraderPlugin.getBookDataProvider() != null)
      {
        IBookDataProvider _dp = TraderPlugin.getBookDataProvider();
        _dp.addBookListener(data, this);
        _dp.startBook(data);
      }
      
      updateView();
    }
  }

  public void setData()
  {
    String id = getViewSite().getSecondaryId();
    String symbol = ViewsPlugin.getDefault().getPreferenceStore().getString("book." + id);

    if (data != null && TraderPlugin.getBookDataProvider() != null)
    {
      IBookDataProvider _dp = TraderPlugin.getBookDataProvider();
      _dp.removeBookListener(data, this);
    }

    IBasicData[] _tpData = TraderPlugin.getData();
    for (int i = 0; i < _tpData.length; i++)
    {
      if (_tpData[i].getSymbol().equalsIgnoreCase(symbol) == true)
      {
        data = _tpData[i];
        break;
      }
    }
    
    if (data != null)
    {
      getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
        public void run() {
          setPartName(data.getTicker() + " - Book");
        }
      });
      if (TraderPlugin.getBookDataProvider() != null)
      {
        IBookDataProvider _dp = TraderPlugin.getBookDataProvider();
        _dp.addBookListener(data, this);
        _dp.startBook(data);
      }
    }
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
    int width = table.getClientArea().width;
    int c1 = (int)((width / 2.0) * .18);
    int c2 = (int)((width / 2.0) * .45);
    int c3 = (width / 2) - c1 - c2;
    table.getColumn(0).setWidth(0);
    table.getColumn(1).setWidth(c1);
    table.getColumn(2).setWidth(c2);
    table.getColumn(3).setWidth(c3);
    table.getColumn(4).setWidth(c3);
    table.getColumn(5).setWidth(c2);
    table.getColumn(6).setWidth(c1);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IDataStreamer)
   */
  public void dataUpdated(IBasicDataProvider ds)
  {
  }

  /**
   * Notify the view that the data observed by this view was updated.
   */
  public void dataUpdated(IBasicDataProvider dataProvider, IBasicData data)
  {
    asyncUpdateView();
  }

  /**
   * Notify the view that the book was updated.
   */
  public void bookUpdated(IBasicData _data, IBookData[] _bid, IBookData[] _ask)
  {
    this.bid = _bid;
    this.ask = _ask;
    asyncUpdateView();

    // Schedula un timer ogni secondo per aggiornare il book anche in assenza
    // di dati dal data provider.
    if (timerDaemon == null)
    {
      timerDaemon = new Timer();
      timerDaemon.schedule(new TimerTask() {
        public void run()
        {
          if ((System.currentTimeMillis() - lastUpdate) >= 15000)
            asyncUpdateView();
        }
      }, 2000, 1000);
    }
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
    lastUpdate = System.currentTimeMillis();
    if (table.isDisposed() == true || trendBar.isDisposed() == true)
      return;
    if (label1.isDisposed() == true || label2.isDisposed() == true || label3.isDisposed() == true || label4.isDisposed() == true || label5.isDisposed() == true || label6.isDisposed() == true)
      return;
    if (data instanceof IExtendedData)
    {
      IExtendedData xd = (IExtendedData)data;
      label1.setText(xd.getTime());
      label2.setText(nf.format(xd.getVolume()));
      label3.setText(pf.format(xd.getLastPrice()));
      label4.setText(pf.format(xd.getHighPrice()));
      double gain = (xd.getLastPrice() - xd.getClosePrice()) / xd.getClosePrice() * 100;
      if (gain > 0)
      {
        label5.setForeground(positiveForeground);
        label5.setText("+" + pcf.format(gain) + "%");
      }
      else
      {
        label5.setText(pcf.format(gain) + "%");
        if (gain < 0)
          label5.setForeground(negativeForeground);
        else
          label5.setForeground(foreground);
      }
      label6.setText(pf.format(xd.getLowPrice()));
//      info.layout();
    }
    
    trendBar.bookUpdated(data, bid, ask);

    if (bid == null || ask == null || table.isDisposed() == true)
      return;
    
    table.setRedraw(false);
    int items = (bid.length > ask.length) ? bid.length : ask.length;
    if (table.getItemCount() != items)
      table.setItemCount(items);

    int level = 0, number = 0, quantity = 0;
    double levelPrice = 0;
    for (int i = 0; i < bid.length; i++)
    {
      // Update the price level
      if (levelPrice == 0)
        levelPrice = bid[i].getPrice();
      if (levelPrice != bid[i].getPrice())
      {
        level++;
        levelPrice = bid[i].getPrice();
        number = 0;
        quantity = 0;
      }
      number++;
      quantity += bid[i].getQuantity();

      // Get the table row
      TableItem item = table.getItem(i);
      if (groupPrices == true && level != i)
        item = table.getItem(level);

      // Set the background of each price level
      if (colorizeLevels == true)
      {
        if (level < levelColor.length)
        {
          item.setBackground(1, levelColor[level]);
          item.setBackground(2, levelColor[level]);
          item.setBackground(3, levelColor[level]);
        }
        else
        {
          item.setBackground(1, background);
          item.setBackground(2, background);
          item.setBackground(3, background);
        }
      }
      else
      {
        item.setBackground(1, background);
        item.setBackground(2, background);
        item.setBackground(3, background);
      }

      if (hilightVariations == true)
      {
        if (bid[i].getNumberVariance() > 0)
          item.setForeground(1, positiveForeground);
        else if (bid[i].getNumberVariance() < 0)
          item.setForeground(1, negativeForeground);
        else
          item.setForeground(1, foreground);
        if (bid[i].getQuantityVariance() > 0)
          item.setForeground(2, positiveForeground);
        else if (bid[i].getQuantityVariance() < 0)
          item.setForeground(2, negativeForeground);
        else
          item.setForeground(2, foreground);
        if (bid[i].getPriceVariance() > 0)
          item.setForeground(3, positiveForeground);
        else if (bid[i].getPriceVariance() < 0)
          item.setForeground(3, negativeForeground);
        else
          item.setForeground(3, foreground);
      }
      else
      {
        item.setForeground(1, foreground);
        item.setForeground(2, foreground);
        item.setForeground(3, foreground);
      }

      if (groupPrices == true)
      {
        item.setText(1, nf.format(number));
        item.setText(2, nf.format(quantity));
        item.setText(3, pf.format(levelPrice));
      }
      else
      {
        item.setText(1, nf.format(bid[i].getNumber()));
        item.setText(2, nf.format(bid[i].getQuantity()));
        item.setText(3, pf.format(bid[i].getPrice()));
      }
    }

    level = 0;
    levelPrice = 0;
    for (int i = 0; i < ask.length; i++)
    {
      TableItem item = table.getItem(i);
      
      // Update the price level
      if (levelPrice == 0)
        levelPrice = ask[i].getPrice();
      if (levelPrice != ask[i].getPrice())
      {
        level++;
        levelPrice = ask[i].getPrice();
      }

      // Set the background of each price level
      if (colorizeLevels == true)
      {
        if (level < levelColor.length)
        {
          item.setBackground(4, levelColor[level]);
          item.setBackground(5, levelColor[level]);
          item.setBackground(6, levelColor[level]);
        }
        else
        {
          item.setBackground(4, background);
          item.setBackground(5, background);
          item.setBackground(6, background);
        }
      }
      else
      {
        item.setBackground(4, background);
        item.setBackground(5, background);
        item.setBackground(6, background);
      }

      if (hilightVariations == true)
      {
        if (ask[i].getPriceVariance() > 0)
          item.setForeground(4, positiveForeground);
        else if (ask[i].getPriceVariance() < 0)
          item.setForeground(4, negativeForeground);
        else
          item.setForeground(4, foreground);
        if (ask[i].getQuantityVariance() > 0)
          item.setForeground(5, positiveForeground);
        else if (ask[i].getQuantityVariance() < 0)
          item.setForeground(5, negativeForeground);
        else
          item.setForeground(5, foreground);
        if (ask[i].getNumberVariance() > 0)
          item.setForeground(6, positiveForeground);
        else if (ask[i].getNumberVariance() < 0)
          item.setForeground(6, negativeForeground);
        else
          item.setForeground(6, foreground);
      }
      else
      {
        item.setForeground(4, foreground);
        item.setForeground(5, foreground);
        item.setForeground(6, foreground);
      }
      
      item.setText(4, pf.format(ask[i].getPrice()));
      item.setText(5, nf.format(ask[i].getQuantity()));
      item.setText(6, nf.format(ask[i].getNumber()));
    }
    
    table.setRedraw(true);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();

    // Property changed for the Trader plugin
    if (property.equalsIgnoreCase("net.sourceforge.eclipsetrader.bookDataProvider") == true)
    {
      if (TraderPlugin.getBookDataProvider() != null)
      {
        IBookDataProvider _dp = TraderPlugin.getBookDataProvider();
        _dp.addBookListener(data, this);
        _dp.startBook(data);
      }
    }
    else if (property.startsWith("book.") == true)
    {
      IPreferenceStore pref = ViewsPlugin.getDefault().getPreferenceStore();
      groupPrices = pref.getBoolean("book.group_prices");
      foreground = new Color(null, PreferenceConverter.getColor(pref, "book.text_color"));
      background = new Color(null, PreferenceConverter.getColor(pref, "book.background"));
      negativeForeground = new Color(null, PreferenceConverter.getColor(pref, "book.negative_value_color"));
      positiveForeground = new Color(null, PreferenceConverter.getColor(pref, "book.positive_value_color"));
      levelColor[0] = new Color(null, PreferenceConverter.getColor(pref, "book.level1_color"));
      levelColor[1] = new Color(null, PreferenceConverter.getColor(pref, "book.level2_color"));
      levelColor[2] = new Color(null, PreferenceConverter.getColor(pref, "book.level3_color"));
      levelColor[3] = new Color(null, PreferenceConverter.getColor(pref, "book.level4_color"));
      levelColor[4] = new Color(null, PreferenceConverter.getColor(pref, "book.level5_color"));
      hilightVariations = pref.getBoolean("book.hilight_variations");
      colorizeLevels = pref.getBoolean("book.colorize_levels");
      if (property.startsWith("book.level") == true)
        trendBar.reloadPreferences();
      updateView();
    }
  }
}
