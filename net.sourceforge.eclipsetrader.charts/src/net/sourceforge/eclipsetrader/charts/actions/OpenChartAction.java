/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts.actions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.dialogs.ChartSettingsDialog;
import net.sourceforge.eclipsetrader.charts.views.ChartView;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 */
public class OpenChartAction implements IViewActionDelegate, IMenuCreator, SelectionListener, Observer
{
    private Security security;
    private Menu menu;
    private ICollectionObserver collectionObserver = new ICollectionObserver() {
        public void itemAdded(Object o)
        {
            if (menu != null && !menu.isDisposed())
                fillMenu(menu);
        }

        public void itemRemoved(Object o)
        {
            if (menu != null && !menu.isDisposed())
                fillMenu(menu);
        }
    };
    private DisposeListener disposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
            ((Chart)e.widget.getData()).deleteObserver(OpenChartAction.this);
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        this.security = null;
        if (selection instanceof SecuritySelection)
            this.security = ((SecuritySelection)selection).getSecurity();
        
        action.setMenuCreator(this);
        action.setEnabled(this.security != null);
        
        if (menu != null)
            fillMenu(menu);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose()
    {
        CorePlugin.getRepository().allCharts().removeCollectionObserver(collectionObserver);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent)
    {
        if (menu != null)
            menu.dispose();
        menu = new Menu(parent);
        
        MenuItem item = new MenuItem(menu, SWT.NONE);
        item.setText("New...");
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (security != null)
                {
                    Chart chart = ChartsPlugin.createDefaultChart();
                    chart.setSecurity(security);

                    StringBuffer sb = new StringBuffer(security.getCode());
                    sb.append(" middle ");
                    String intervalTitle = "";
                    switch (chart.getCompression())
                    {
                        case BarData.INTERVAL_MINUTE1:
                            intervalTitle = "1m";
                            break;
                        case BarData.INTERVAL_MINUTE2:
                            intervalTitle = "2m";
                            break;
                        case BarData.INTERVAL_MINUTE5:
                            intervalTitle = "5m";
                            break;
                        case BarData.INTERVAL_MINUTE10:
                            intervalTitle = "10m";
                            break;
                        case BarData.INTERVAL_MINUTE15:
                            intervalTitle = "15m";
                            break;
                        case BarData.INTERVAL_MINUTE30:
                            intervalTitle = "30m";
                            break;
                        case BarData.INTERVAL_MINUTE60:
                            intervalTitle = "1h";
                            break;
                        case BarData.INTERVAL_DAILY:
                            intervalTitle = "Daily";
                            break;
                        case BarData.INTERVAL_WEEKLY:
                            intervalTitle = "Weekly";
                            break;
                        case BarData.INTERVAL_MONTHLY:
                            intervalTitle = "Monthly";
                            break;
                    }
                    sb.append(intervalTitle);
                    chart.setTitle(sb.toString());

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    ChartSettingsDialog dlg = new ChartSettingsDialog(chart, window.getShell());
                    if (dlg.open() == ChartSettingsDialog.OK)
                    {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        try {
                            page.showView(ChartView.VIEW_ID, String.valueOf(chart.getId()), IWorkbenchPage.VIEW_ACTIVATE);
                        } catch (PartInitException e1) {
                            CorePlugin.logException(e1);
                        }
                        fillMenu(menu);
                    }
                }
            }
        });

        fillMenu(menu);
        CorePlugin.getRepository().allCharts().addCollectionObserver(collectionObserver);
        
        return menu;
    }
    
    protected void fillMenu(Menu menu)
    {
        MenuItem[] items = menu.getItems();
        for (int i = 1; i < items.length; i++)
            items[i].dispose();

        List list = CorePlugin.getRepository().allCharts(security);
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                return ((Chart)o1).getTitle().compareTo(((Chart)o2).getTitle());
            }
        });

        Chart[] charts = (Chart[])list.toArray(new Chart[0]);
        if (charts.length != 0)
        {
            new MenuItem(menu, SWT.SEPARATOR);
            
            for (int i = 0; i < charts.length; i++)
            {
                MenuItem item = new MenuItem(menu, SWT.NONE);
                item.setText(charts[i].getTitle());
                item.setData(charts[i]);
                item.addSelectionListener(this);
                item.addDisposeListener(disposeListener);
                charts[i].addObserver(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        Chart chart = (Chart) ((MenuItem) e.getSource()).getData();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(ChartView.VIEW_ID, String.valueOf(chart.getId()), IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e1) {
            CorePlugin.logException(e1);
        }
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(final Observable o, Object arg)
    {
        if (!menu.isDisposed())
        {
            menu.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!menu.isDisposed())
                    {
                        MenuItem[] items = menu.getItems();
                        for (int i = 0; i < items.length; i++)
                        {
                            if (o.equals(items[i].getData()))
                                items[i].setText(((Chart)o).getTitle());
                        }
                    }
                }
            });
        }
    }
}
