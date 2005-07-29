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
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.IExtendedData;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class PortfolioTableItem implements DisposeListener, Observer
{
  private PortfolioView parent;
  private TableItem tableItem;
  private Color negativeForeground;
  private Color positiveForeground;
  private NumberFormat nf = NumberFormat.getInstance();
  private NumberFormat pf = NumberFormat.getInstance();
  private NumberFormat bpf = NumberFormat.getInstance();
  private NumberFormat pcf = NumberFormat.getInstance();
  private SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
  private IExtendedData data;
  private PortfolioTableData tableData;
  private boolean scheduled = false;

  public PortfolioTableItem(PortfolioView view, Table parent, int style)
  {
    tableItem = new TableItem(parent, style);
    tableItem.setData(this);
    tableItem.addDisposeListener(this);
    this.parent = view;
    initialize();
  }

  public PortfolioTableItem(PortfolioView view, Table parent, int style, int index)
  {
    tableItem = new TableItem(parent, style, index);
    tableItem.setData(this);
    tableItem.addDisposeListener(this);
    this.parent = view;
    initialize();
  }
  
  private void initialize()
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
  
  public IExtendedData getData()
  {
    return data;
  }

  /**
   * Set the IExtendedData instance connected to this table item.
   * 
   * @param data - the connected IExtendedData instance
   */
  public void setData(IExtendedData data)
  {
    if (this.data != null && this.data instanceof Observable) 
      ((Observable)this.data).deleteObserver(this);

    this.data = data;
    
    if (this.data != null && this.data instanceof Observable) 
      ((Observable)this.data).addObserver(this);

    update();
  }
  
  private void setText(int column, String text)
  {
    if (tableItem.getText(column).length() == 0 || !tableItem.getText(column).equals(text))
      tableItem.setText(column, text);
  }

  public void update()
  {
    if (tableData == null)
      tableData = new PortfolioTableData();
    
    Table table = tableItem.getParent();
    for (int column = 0; column < table.getColumnCount(); column++)
    {
      int columnData = parent.getColumnDataIndex(column);
      switch(columnData)
      {
        case 0:
          setText(column, data.getSymbol());
          break;
        case 1:
          setText(column, data.getTicker());
          break;
        case 2:
          setText(column, data.getDescription());
          break;
        case 3:
          setText(column, pf.format(data.getLastPrice()));
          tableData.setLastPrice(data.getLastPrice());
          if (tableData.getLastPriceVariance() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (tableData.getLastPriceVariance() > 0)
            tableItem.setForeground(column, positiveForeground);
          break;
        case 4:
          setText(column, pcf.format(data.getChange()) + "%"); //$NON-NLS-1$
          if (data.getChange() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (data.getChange() > 0)
            tableItem.setForeground(column, positiveForeground);
          else
            tableItem.setForeground(column, null);
          break;
        case 5:
          setText(column, pf.format(data.getBidPrice()));
          tableData.setBidPrice(data.getBidPrice());
          if (tableData.getBidPriceVariance() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (tableData.getBidPriceVariance() > 0)
            tableItem.setForeground(column, positiveForeground);
          break;
        case 6:
          setText(column, nf.format(data.getBidSize()));
          tableData.setBidSize(data.getBidSize());
          if (tableData.getBidSizeVariance() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (tableData.getBidSizeVariance() > 0)
            tableItem.setForeground(column, positiveForeground);
          break;
        case 7:
          setText(column, pf.format(data.getAskPrice()));
          tableData.setAskPrice(data.getAskPrice());
          if (tableData.getAskPriceVariance() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (tableData.getAskPriceVariance() > 0)
            tableItem.setForeground(column, positiveForeground);
          break;
        case 8:
          setText(column, nf.format(data.getAskSize()));
          tableData.setAskSize(data.getAskSize());
          if (tableData.getAskSizeVariance() < 0)
            tableItem.setForeground(column, negativeForeground);
          else if (tableData.getAskSizeVariance() > 0)
            tableItem.setForeground(column, positiveForeground);
          break;
        case 9:
          setText(column, nf.format(data.getVolume()));
          break;
        case 10:
          setText(column, nf.format(data.getMinimumQuantity()));
          break;
        case 11:
        {
          double marketValue = data.getMinimumQuantity() * data.getLastPrice();
          setText(column, bpf.format(marketValue));
          break;
        }
        case 12:
          if (data.getOwnedQuantity() == 0)
            setText(column, ""); //$NON-NLS-1$
          else
            setText(column, nf.format(data.getOwnedQuantity()));
          break;
        case 13:
          if (data.getPaid() == 0)
            setText(column, ""); //$NON-NLS-1$
          else
            setText(column, pf.format(data.getPaid()));
          break;
        case 14:
          if (data.getPaid() != 0 && data.getOwnedQuantity() != 0)
          {
            double valuePaid = data.getPaid() * data.getOwnedQuantity();
            setText(column, bpf.format(valuePaid));
          }
          else
            setText(column, ""); //$NON-NLS-1$
          break;
        case 15:
          if (data.getPaid() != 0 && data.getOwnedQuantity() != 0)
          {
            double gain = (data.getLastPrice() * data.getOwnedQuantity()) - (data.getPaid() * data.getOwnedQuantity());
            double gainPercentage = (data.getLastPrice() - data.getPaid()) / data.getPaid() * 100; 
            
            setText(column, bpf.format(gain) + " (" + pcf.format(gainPercentage) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$
            if (gain < 0)
              tableItem.setForeground(column, negativeForeground);
            else if (gain > 0)
              tableItem.setForeground(column, positiveForeground);
            else
              tableItem.setForeground(column, null);
          }
          else
            setText(column, ""); //$NON-NLS-1$
          break;
        case 16:
          if (data.getOpenPrice() > 200)
            setText(column, nf.format(data.getOpenPrice()));
          else
            setText(column, pf.format(data.getOpenPrice()));
          break;
        case 17:
          if (data.getHighPrice() > 200)
            setText(column, nf.format(data.getHighPrice()));
          else
            setText(column, pf.format(data.getHighPrice()));
          break;
        case 18:
          if (data.getLowPrice() > 200)
            setText(column, nf.format(data.getLowPrice()));
          else
            setText(column, pf.format(data.getLowPrice()));
          break;
        case 19:
          if (data.getClosePrice() > 200)
            setText(column, nf.format(data.getClosePrice()));
          else
            setText(column, pf.format(data.getClosePrice()));
          break;
        case 20:
          setText(column, tf.format(data.getDate()));
          break;
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    if (scheduled == true)
      return;
    scheduled = true;
    tableItem.getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        scheduled = false;
        if (!tableItem.isDisposed())
          update();
      }
    });
  }

  public void resetHilight()
  {
    if (tableData != null && tableItem.isDisposed() == false)
    {
      Table table = tableItem.getParent();
      for (int column = 0; column < table.getColumnCount(); column++)
      {
        int columnData = parent.getColumnDataIndex(column);
        switch(columnData)
        {
          case 3:
            if (tableData.getLastPriceVariance() == 0)
              tableItem.setForeground(column, null);
            break;
          case 5:
            if (tableData.getBidPriceVariance() == 0)
              tableItem.setForeground(column, null);
            break;
          case 6:
            if (tableData.getBidSizeVariance() == 0)
              tableItem.setForeground(column, null);
            break;
          case 7:
            if (tableData.getAskPriceVariance() == 0)
              tableItem.setForeground(column, null);
            break;
          case 8:
            if (tableData.getAskSizeVariance() == 0)
              tableItem.setForeground(column, null);
            break;
        }
      }
    }
  }

  public void setNegativeForeground(Color negativeForeground)
  {
    this.negativeForeground = negativeForeground;
  }

  public void setParent(PortfolioView parent)
  {
    this.parent = parent;
  }

  public void setPositiveForeground(Color positiveForeground)
  {
    this.positiveForeground = positiveForeground;
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
   */
  public void widgetDisposed(DisposeEvent e)
  {
    if (this.data != null && this.data instanceof Observable) 
      ((Observable)this.data).deleteObserver(this);
  }
}
