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
package net.sourceforge.eclipsetrader.ui.views.news;

import java.util.Timer;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBasicDataProvider;
import net.sourceforge.eclipsetrader.IDataUpdateListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class NewsView extends ViewPart implements IDataUpdateListener 
{
  private Table table;
  private Color background = new Color(Display.getCurrent(), 255, 255, 224);
  private Color foreground = new Color(Display.getCurrent(), 0, 0, 0);
  private Timer timer;
//  private Action refreshAction;
//  private Action autoRefreshAction;
//  private Action doubleClickAction;
  private NewsProvider newsProvider = new NewsProvider();

  public NewsView()
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    table = new Table(parent, SWT.SINGLE|SWT.FULL_SELECTION);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    table.setLayoutData(gd);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.setBackground(background);

    TableColumn column = new TableColumn(table, SWT.RIGHT, 0);
    column.setText("Data");
    column.setWidth(105);
    column = new TableColumn(table, SWT.LEFT, 1);
    column.setText("Titolo");
    column.setWidth(735);
    column = new TableColumn(table, SWT.LEFT, 2);
    column.setText("Agenzia");
    column.setWidth(145);

/*    IActionBars bars = getViewSite().getActionBars();
    
    refreshAction = new Action() {
      public void run() {
        refreshAction.setEnabled(false);
        startUpdate();
      }
    };
    refreshAction.setText("Refresh");
    refreshAction.setToolTipText("Refresh List");
    refreshAction.setImageDescriptor(Images.ICON_REFRESH);
    bars.getToolBarManager().add(refreshAction);
    
    autoRefreshAction = new Action() {
      public void run() {
        if (isChecked() == true)
        {
          timer.cancel();
          timer = new Timer();
          timer.schedule(new TimerTask() {
            public void run() {
              updateList();
            }
          }, 10 * 60000);
        }
        else
          timer.cancel();
      }
    };
    autoRefreshAction.setChecked(false);
    autoRefreshAction.setText("Auto-Refresh");
    autoRefreshAction.setToolTipText("Auto Refresh");
    autoRefreshAction.setImageDescriptor(Images.ICON_AUTOREFRESH);
    bars.getToolBarManager().add(autoRefreshAction);*/

    table.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        NewsData item = newsProvider.getData()[table.getSelectionIndex()];
        try {
          IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser");
          if (browser != null)
          {
            browser.setFocus();
            ((NewsBrowser)browser).setUrl(item.url);
          }
        } catch(PartInitException x) {};
      }
      public void mouseDown(MouseEvent e) {
      }
      public void mouseUp(MouseEvent e) {
      }
    });
/*
    timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        updateList();
      }
    }, 2 * 1000);*/
    update();
    
//    String s = "Location:         " + Platform.getLocation() + "\r\n";
//    s += "InstanceLocation: " + Platform.getInstanceLocation().getURL() + "\r\n";
//    s += "InstallLocation:  " + Platform.getInstallLocation().getURL() + "\r\n";
//    MessageDialog.openInformation(table.getShell(), "Sample View", s);

    System.out.println(this.getClass() + ": createPartControl");
  }
  
  public void startUpdate()
  {
    Thread t = new Thread(new Runnable() {
      public void run() {
        updateList();
      }
    });
    t.start();
  }
  
  public void next()
  {
    int index = table.getSelectionIndex() + 1;
    if (index >= table.getItemCount())
      index = 0;
    table.setSelection(index);

    NewsData item = newsProvider.getData()[table.getSelectionIndex()];
    try {
      IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser");
      if (browser != null)
        ((NewsBrowser)browser).setUrl(item.url);
    } catch(PartInitException x) {};
  }
  
  public void previous()
  {
    int index = table.getSelectionIndex();
    if (index == -1 || index == 0)
      index = table.getItemCount() - 1;
    else
      index--;
    table.setSelection(index);

    NewsData item = newsProvider.getData()[table.getSelectionIndex()];
    try {
      IViewPart browser = getSite().getPage().showView("net.sourceforge.eclipsetrader.ui.views.NewsBrowser");
      if (browser != null)
        ((NewsBrowser)browser).setUrl(item.url);
    } catch(PartInitException x) {};
  }
  
  private void updateList()
  {
    newsProvider.update();
    table.getDisplay().asyncExec(new Runnable() {
      public void run() {
        update();
      }
    });
  }
  
  /**
   * Updates the table contents
   */
  public void update()
  {
    NewsData[] data = newsProvider.getData();
    
    table.setRedraw(false);
    table.setItemCount(data.length);

    for (int row = 0; row < data.length; row++)
    {
      TableItem item = table.getItem(row);
      item.setText(0, data[row].getFormattedDate());
      item.setText(1, data[row].getTitle());
      item.setText(2, data[row].getSource());
    }

    table.setRedraw(true);
//    refreshAction.setEnabled(true);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    super.dispose();
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.IDataUpdateListener#dataUpdated(net.sourceforge.eclipsetrader.IDataStreamer)
   */
  public void dataUpdated(IBasicDataProvider ds)
  {
    table.getDisplay().asyncExec(new Runnable() {
      public void run() {
        table.setRedraw(false);
        update();
        table.setRedraw(true);
      }
    });
  }
  public void dataUpdated(IBasicDataProvider dataProvider, IBasicData data)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    table.setFocus();
  }
}
