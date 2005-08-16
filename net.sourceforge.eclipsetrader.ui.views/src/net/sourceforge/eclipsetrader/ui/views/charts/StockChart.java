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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.Messages;
import net.sourceforge.eclipsetrader.ui.internal.views.StockList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Price chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class StockChart extends ChartPlotter implements IChartConfigurer
{
  public final static int CLOSE = 0;
  public final static int OPEN = 1;
  public final static int MAX = 2;
  public final static int MIN = 3;
  public final static int LINE = 0;
  public final static int CANDLE = 1;
  public final static int BAR = 2;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.stock"; //$NON-NLS-1$
  private NumberFormat nf = NumberFormat.getInstance();
  private String symbol = "";
  private int dataSource = CLOSE;
  private IBasicData stockItem;
  private IChartData[] stockChartData;
  protected Color gridColor = new Color(null, 192, 192, 192);
  protected Color separatorColor = new Color(null, 255, 0, 0);
  protected Color textColor = new Color(null, 0, 0, 0);
  protected Color positiveColor = new Color(null, 0, 192, 0);
  protected Color negativeColor = new Color(null, 192, 0, 0);
  protected Color neutralColor = new Color(null, 128, 128, 128);
  private List list = new ArrayList();
  
  public StockChart()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
    setName(Messages.getString("StockChart.label")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return PLUGIN_ID;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return getName();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setData(net.sourceforge.eclipsetrader.IChartData[])
   */
  public void setData(IChartData[] data)
  {
    if (stockChartData.length > data.length)
    {
      IChartData[] newChartData = new IChartData[data.length];
      System.arraycopy(stockChartData, stockChartData.length - data.length, newChartData, 0, data.length);
      super.setData(newChartData);
    }
    else
      super.setData(stockChartData);

    // Set the values to draw
    list = new ArrayList();
    switch(dataSource)
    {
      case OPEN:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(chartData[i].getOpenPrice()));
        break;
      case CLOSE:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(chartData[i].getClosePrice()));
        break;
      case MAX:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(chartData[i].getMaxPrice()));
        break;
      case MIN:
        for (int i = 0; i < data.length; i++)
          list.add(new Double(chartData[i].getMinPrice()));
        break;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    drawLine(list, gc, height);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("dataSource") == true)
      dataSource = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("stockItem") == true)
    {
      stockChartData = null;
      StockList sl = new StockList();
      IBasicData[] data = sl.getData();
      stockItem = data[Integer.parseInt(value)];
      symbol = stockItem.getSymbol();
      stockChartData = TraderPlugin.getDataStore().getHistoricalData(stockItem);
      super.setParameter("symbol", symbol);
      return;
    }
    else if (name.equalsIgnoreCase("symbol") == true)
    {
      stockChartData = null;
      StockList sl = new StockList();
      stockItem = sl.getData(value);
      symbol = stockItem.getSymbol();
      stockChartData = TraderPlugin.getDataStore().getHistoricalData(stockItem);
    }
    super.setParameter(name, value);
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Stock Item");
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setVisibleItemCount(20);
    combo.setData("stockItem");
    combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    StockList sl = new StockList();
    IBasicData[] data = sl.getData();
    for (int i = 0; i < data.length; i++)
    {
      combo.add(data[i].getDescription());
      if (symbol.equals(data[i].getSymbol()) == true)
        combo.setText(data[i].getDescription());
    }

    label = new Label(parent, SWT.NONE);
    label.setText("Chart Data");
    combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData("dataSource");
    combo.add("Close Price");
    combo.add("Open Price");
    combo.add("Highest Price");
    combo.add("Lowest Price");
    combo.setText(combo.getItem(dataSource));

    return parent;
  }

  /**
   * Rounds the given price to the nearest tick.<br>
   * 
   * @param price The price value
   * @return The rounded price
   */
  public double roundToTick(double price)
  {
    double tick = getPriceTick(price);
    return ((int)(price / tick)) * tick;
  }
  
  /**
   * Get the price tick related to the passed as argument.<br>
   * 
   * @param price The price value
   * @return The price tick 
   */
  public double getPriceTick(double price) 
  {
    if (price <= 0.3)
      return 0.0005;
    else if (price <= 1.5)
      return 0.001;
    else if (price <= 3)
      return 0.005;
    else if (price <= 30)
      return 0.01;
    else
      return 0.05;
  }
}
