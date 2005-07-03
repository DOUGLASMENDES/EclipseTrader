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
package net.sourceforge.eclipsetrader.directa.ui.views;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.directa.DirectaPlugin;
import net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver;
import net.sourceforge.eclipsetrader.directa.internal.OrderData;
import net.sourceforge.eclipsetrader.directa.internal.Streamer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;


public class Orders extends ViewPart implements IStreamerEventReceiver, IPropertyChangeListener 
{
  private Table table;
  private Color background = new Color(null, 255, 255, 255);
  private Color foreground = new Color(null, 0, 0, 0);

  public Orders()
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    pref.addPropertyChangeListener(this);

    foreground = new Color(null, PreferenceConverter.getColor(pref, "orders.text_color")); //$NON-NLS-1$
    background = new Color(null, PreferenceConverter.getColor(pref, "orders.background_color")); //$NON-NLS-1$

    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    table.setLayoutData(gd);
    table.setHeaderVisible(true);
    table.setLinesVisible(false);
    table.setBackground(background);
    table.setForeground(foreground);

    TableColumn column = new TableColumn(table, SWT.LEFT, 0);
    column.setText("ID");
    column.setWidth(100);
    column = new TableColumn(table, SWT.CENTER, 1);
    column.setText("Tipo");
    column.setWidth(70);
    column = new TableColumn(table, SWT.CENTER, 2);
    column.setText("Quantità");
    column.setWidth(60);
    column = new TableColumn(table, SWT.CENTER, 3);
    column.setText("Titolo");
    column.setWidth(120);
    column = new TableColumn(table, SWT.RIGHT, 4);
    column.setText("Prezzo");
    column.setWidth(60);
    column = new TableColumn(table, SWT.LEFT, 5);
    column.setText("Stato");
    column.setWidth(100);

    column = new TableColumn(table, SWT.CENTER, 6);
    column.setText("Q. Eseg.");
    column.setWidth(60);
    column = new TableColumn(table, SWT.RIGHT, 7);
    column.setText("Pr. Eseg.");
    column.setWidth(60);
    
    orderStatusChanged();
    Streamer.getInstance().addEventReceiver(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    table.getParent().setFocus();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    pref.removePropertyChangeListener(this);

    Streamer.getInstance().removeEventReceiver(this);
    
    super.dispose();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated()
   */
  public void dataUpdated()
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#dataUpdated(net.sourceforge.eclipsetrader.IBasicData)
   */
  public void dataUpdated(IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.directa.internal.IStreamerEventReceiver#orderStatusChanged()
   */
  public void orderStatusChanged()
  {
    System.out.println("OrdersView: orderStatusChanged");
    OrderData[] data = Streamer.getInstance().getOrderData();
    if (data == null)
      return;
    
    table.setRedraw(false);
    table.setItemCount(data.length);

    for (int row = 0; row < data.length; row++)
    {
      TableItem item = table.getItem(row);
      item.setText(0, data[row].id);
      item.setText(1, data[row].type);
      item.setText(2, data[row].quantity);
      item.setText(3, data[row].code + " (" + data[row].symbol + ")");
      item.setText(4, data[row].price);
      item.setText(5, data[row].status);
      item.setText(6, data[row].executedQuantity);
      item.setText(7, data[row].executedPrice);
    }

    table.setRedraw(true);
  }
  
  public void cancelOrder()
  {
    if (table.getSelectionIndex() != -1)
    {
      OrderData[] data = Streamer.getInstance().getOrderData();
      if (data != null)
      {
        boolean ret = MessageDialog.openQuestion(
            table.getShell(),
            "Annullamento Ordine",
            "Vuoi veramente annullare l'ordine ?");
        if (ret == true)
          Streamer.getInstance().cancelOrder(data[table.getSelectionIndex()].id);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    IPreferenceStore pref = DirectaPlugin.getDefault().getPreferenceStore();
    if (property.equalsIgnoreCase("orders.text_color") == true)
    {
      foreground = new Color(null, PreferenceConverter.getColor(pref, "orders.text_color")); //$NON-NLS-1$
      table.setForeground(foreground);
    }
    else if (property.equalsIgnoreCase("orders.background_color") == true)
    {
      background = new Color(null, PreferenceConverter.getColor(pref, "orders.background_color")); //$NON-NLS-1$
      table.setBackground(background);
    }
  }
}
