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

package net.sourceforge.eclipsetrader.charts.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.charts.BarData;
import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.DatePlot;
import net.sourceforge.eclipsetrader.charts.IndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Plot;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.charts.events.ChartSelectionProvider;
import net.sourceforge.eclipsetrader.charts.events.IndicatorSelection;
import net.sourceforge.eclipsetrader.charts.events.ObjectSelection;
import net.sourceforge.eclipsetrader.charts.events.PlotEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotListener;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseListener;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionListener;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class ChartView extends ViewPart implements PlotMouseListener, CTabFolder2Listener, ICollectionObserver, Observer
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.chart";
    public static final String PERIOD_PREFS = "PERIOD_";
    public static final int PERIOD_ALL = 0;
    public static final int PERIOD_LAST6MONTHS = 1;
    public static final int PERIOD_LASTYEAR = 2;
    public static final int PERIOD_LAST2YEARS = 3;
    public static final int PERIOD_CUSTOM = -1;
    public static final String PERIOD_BEGIN = "BEGIN_";
    public static final String PERIOD_END = "END_";
    private Chart chart;
    private SashForm sashForm;
    private DatePlot datePlot;
    private ScrollBar hBar;
    private List tabGroups = new ArrayList();
    private Security security;
    private int oldMouseX = -1, oldMouseY = -1;
    private ChartObject newChartObject;
    private SimpleDateFormat dateParse = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator("group1")); //$NON-NLS-1$
        menuManager.add(new Separator("group2")); //$NON-NLS-1$
        menuManager.add(new Separator("group3")); //$NON-NLS-1$
        menuManager.add(new Separator("group4")); //$NON-NLS-1$
        menuManager.add(new Separator("group5")); //$NON-NLS-1$
        menuManager.add(new Separator("group6")); //$NON-NLS-1$
        menuManager.add(new Separator("additions")); //$NON-NLS-1$
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IMenuManager newObjectMenu = new MenuManager("New Object", "newObject");
        menuManager.appendToGroup("group2", newObjectMenu);

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ChartsPlugin.OBJECTS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            java.util.List plugins = Arrays.asList(members);
            Collections.sort(plugins, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    if ((arg0 instanceof IConfigurationElement) && (arg1 instanceof IConfigurationElement))
                    {
                        String s0 = ((IConfigurationElement) arg0).getAttribute("name"); //$NON-NLS-1$
                        String s1 = ((IConfigurationElement) arg1).getAttribute("name"); //$NON-NLS-1$
                        return s0.compareTo(s1);
                    }
                    return 0;
                }
            });

            for (Iterator iter = plugins.iterator(); iter.hasNext(); )
            {
                IConfigurationElement element = (IConfigurationElement)iter.next();
                Action action = new Action(element.getAttribute("name")) { //$NON-NLS-1$
                    public void run()
                    {
                        ChartObject object = new ChartObject();
                        object.setPluginId(getActionDefinitionId());
                        setNewChartObject(object);
                    }
                };
                action.setActionDefinitionId(element.getAttribute("id")); //$NON-NLS-1$
                newObjectMenu.add(action);
            }
        }
        
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("refresh")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("objects")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$
        
        site.getActionBars().updateActionBars();
        
        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.H_SCROLL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 2;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        sashForm = new SashForm(content, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        datePlot = new DatePlot(content, SWT.NONE);
        GridData gridData = new GridData();
        gridData.heightHint = 24;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        datePlot.setLayoutData(gridData);
        
        datePlot.getIndicatorPlot().addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                updateScrollbar();
            }
        });
        datePlot.getIndicatorPlot().addPlotListener(new PlotListener() {
            public void plotResized(PlotEvent e)
            {
                updateScrollbar();
            }
        });

        hBar = content.getHorizontalBar();
        hBar.setVisible(false);
        hBar.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Rectangle plotBounds = datePlot.getIndicatorPlot().getPlotBounds();
                int hSelection = hBar.getSelection();
                plotBounds.x = -hSelection;
                datePlot.getIndicatorPlot().setPlotBounds(plotBounds);
                setPlotBounds(plotBounds);
            }
        });
        
        getSite().setSelectionProvider(new ChartSelectionProvider());

        try {
            security = (Security)CorePlugin.getRepository().load(Security.class, new Integer(Integer.parseInt(getViewSite().getSecondaryId())));
            chart = (Chart)CorePlugin.getRepository().load(Chart.class, security.getId());
            setPartName(chart.getTitle());

            sashForm.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    int period = getPeriod();
                    int size = security.getHistory().size();
                    if (period != PERIOD_ALL && size != 0)
                    {
                        Date end = ((Bar)security.getHistory().get(size - 1)).getDate();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(end);
                        switch(period)
                        {
                            case PERIOD_LAST6MONTHS:
                                calendar.add(Calendar.MONTH, -6);
                                break;
                            case PERIOD_LASTYEAR:
                                calendar.add(Calendar.MONTH, -12);
                                break;
                            case PERIOD_LAST2YEARS:
                                calendar.add(Calendar.MONTH, -24);
                                break;
                            case PERIOD_CUSTOM:
                                try
                                {
                                    calendar.setTime(dateParse.parse(ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_BEGIN + getViewSite().getSecondaryId())));
                                    end = dateParse.parse(ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_END + getViewSite().getSecondaryId())); 
                                }
                                catch (ParseException e) {
                                    CorePlugin.logException(e);
                                    return;
                                }
                                break;
                        }
                        datePlot.setBarData(new BarData(security.getHistory()).getPeriod(calendar.getTime(), end));
                    }
                    else
                        datePlot.setBarData(new BarData(security.getHistory()));

                    try {
                        for (int r = 0; r < chart.getRows().size(); r++)
                            itemAdded(chart.getRows().get(r));
                        chart.getRows().addCollectionObserver(ChartView.this);
                        chart.addObserver(ChartView.this);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
        
/*
        Job job = new Job("Loading chart") {
            protected IStatus run(IProgressMonitor monitor)
            {
                monitor.beginTask("Loading", 2);
                
                security = (Security)CorePlugin.getRepository().load(Security.class, new Integer(Integer.parseInt(getViewSite().getSecondaryId())));
                monitor.worked(1);

                sashForm.getDisplay().syncExec(new Runnable() {
                    public void run()
                    {
                        datePlot.setBarData(new BarData(security.getHistory()));
                        try {
                            chart = (Chart)CorePlugin.getRepository().load(Chart.class, security.getId());
                            setPartName(chart.getTitle());
                            for (int r = 0; r < chart.getRows().size(); r++)
                                itemAdded(chart.getRows().get(r));
                            chart.getRows().addCollectionObserver(ChartView.this);
                            chart.addObserver(ChartView.this);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                monitor.worked(1);

                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();*/
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        sashForm.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (chart != null)
        {
            chart.deleteObserver(ChartView.this);
            chart.getRows().removeCollectionObserver(ChartView.this);
        }
        super.dispose();
    }
    
    public Chart getChart()
    {
        return chart;
    }
    
    public int getInterval()
    {
        return datePlot.getInterval();
    }
    
    public void setInterval(int interval)
    {
        datePlot.setInterval(interval);
        updateView();
    }

    public int getPeriod()
    {
        return ChartsPlugin.getDefault().getPreferenceStore().getInt(PERIOD_PREFS + getViewSite().getSecondaryId());
    }

    public void setPeriod(int period)
    {
        ChartsPlugin.getDefault().getPreferenceStore().setValue(PERIOD_PREFS + getViewSite().getSecondaryId(), period);
        updateView();
    }

    public void setPeriod(String beginDate, String endDate)
    {
        ChartsPlugin.getDefault().getPreferenceStore().setValue(PERIOD_PREFS + getViewSite().getSecondaryId(), PERIOD_CUSTOM);
        ChartsPlugin.getDefault().getPreferenceStore().setValue(PERIOD_BEGIN + getViewSite().getSecondaryId(), beginDate);
        ChartsPlugin.getDefault().getPreferenceStore().setValue(PERIOD_END + getViewSite().getSecondaryId(), endDate);
        updateView();
    }

    public void setNewChartObject(ChartObject object)
    {
        this.newChartObject = object;
        sashForm.setCursor(new Cursor(null, SWT.CURSOR_CROSS));
    }
    
/*    public void addObjectPlugin(ObjectPlugin plugin, int row)
    {
        Plot plot = null;
        CTabItem item = null;
        
        while(tabGroups.size() < row)
            tabGroups.add(new CTabFolder(sashForm, SWT.TOP|SWT.FLAT));
        
        CTabFolder folder = (CTabFolder)tabGroups.get(row - 1); 
        if (folder.getItemCount() == 0)
        {
            item = new CTabItem(folder, SWT.NONE);
            item.setText(plugin.getName());
            item.setControl(new Plot(folder, SWT.NONE));
        }
        else
            item = folder.getItem(folder.getItemCount() - 1);
        plot = (Plot)item.getControl();
        
        plugin.setDatePlot(datePlot);

        plot.setDatePlot(datePlot);
        plot.addObject(plugin);
        plot.getIndicatorPlot().setPlotBounds(datePlot.getIndicatorPlot().getPlotBounds());
        plot.addPlotMouseListener(this);
        
        int weights[] = new int[tabGroups.size()];
        int w = 100 / (weights.length + 2);
        weights[0] = 100 - w * (weights.length - 1);
        for (int i = 1; i < weights.length; i++)
            weights[i] = w;
        sashForm.setWeights(weights);
        sashForm.layout(true);
        
        if (folder.getItemCount() == 1)
            folder.setSelection(0);
    }*/

    private void updateScrollbar()
    {
        Rectangle plotBounds = datePlot.getIndicatorPlot().getPlotBounds();
        if (plotBounds.width != 0)
        {
            Rectangle controlRect = datePlot.getIndicatorPlot().getBounds();
            if (plotBounds.width < controlRect.width)
                plotBounds.x = controlRect.width - plotBounds.width;
            else
            {
                if (!hBar.getVisible())
                {
                    hBar.setMaximum (plotBounds.width);
                    hBar.setThumb(Math.min(plotBounds.width, controlRect.width));
                    int hPage = plotBounds.width - controlRect.width;
                    hBar.setSelection(hPage);
                    plotBounds.x = -hPage;
                }
                else
                {
                    int hPage = hBar.getSelection() + (plotBounds.width - hBar.getMaximum());
                    if ((hPage + controlRect.width) > plotBounds.width)
                        hPage = plotBounds.width - controlRect.width; 
                    hBar.setMaximum (plotBounds.width);
                    hBar.setThumb(Math.min(plotBounds.width, controlRect.width));
//                    plotBounds.x = -hBar.getSelection();
                    hBar.setSelection(hPage);
                    plotBounds.x = -hPage;
                }
            }
            
            hBar.setVisible(plotBounds.width > controlRect.width);

            datePlot.getIndicatorPlot().setPlotBounds(plotBounds);
            setPlotBounds(plotBounds);
        }
        else if (hBar.getVisible())
            hBar.setVisible(false);
    }
    
    private void setPlotBounds(Rectangle bounds)
    {
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            CTabItem[] items = folder.getItems();
            for (int i = 0; i < items.length; i++)
                ((Plot)items[i].getControl()).getIndicatorPlot().setPlotBounds(bounds);
        }
    }
    
    private void updateView()
    {
        if (datePlot.getInterval() < BarData.INTERVAL_DAILY)
            datePlot.setBarData(new BarData(security.getIntradayHistory()).getCompressed(datePlot.getInterval()));
        else
        {
            int period = getPeriod();
            int size = security.getHistory().size();
            if (period != PERIOD_ALL && size != 0)
            {
                Date end = ((Bar)security.getHistory().get(size - 1)).getDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(end);
                switch(period)
                {
                    case PERIOD_LAST6MONTHS:
                        calendar.add(Calendar.MONTH, -6);
                        break;
                    case PERIOD_LASTYEAR:
                        calendar.add(Calendar.MONTH, -12);
                        break;
                    case PERIOD_LAST2YEARS:
                        calendar.add(Calendar.MONTH, -24);
                        break;
                    case PERIOD_CUSTOM:
                        try
                        {
                            calendar.setTime(dateParse.parse(ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_BEGIN + getViewSite().getSecondaryId())));
                            end = dateParse.parse(ChartsPlugin.getDefault().getPreferenceStore().getString(ChartView.PERIOD_END + getViewSite().getSecondaryId())); 
                        }
                        catch (ParseException e) {
                            CorePlugin.logException(e);
                            return;
                        }
                        break;
                }
                datePlot.setBarData(new BarData(security.getHistory()).getPeriod(calendar.getTime(), end));
            }
            else
                datePlot.setBarData(new BarData(security.getHistory()));
        }
        
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            CTabItem[] items = folder.getItems();
            for (int i = 0; i < items.length; i++)
                ((ChartTabItem)items[i]).update();
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        sashForm.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (!datePlot.isDisposed())
                    updateView();
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        final ChartTabFolder folder = new ChartTabFolder(sashForm, SWT.TOP|SWT.FLAT|SWT.BORDER, (ChartRow)o);
        tabGroups.add(folder);
        ((ChartRow)o).clearChanged();

        int weights[] = new int[tabGroups.size()];
        int w = 100 / (weights.length + 2);
        weights[0] = 100 - w * (weights.length - 1);
        for (int i = 1; i < weights.length; i++)
            weights[i] = w;
        sashForm.setWeights(weights);
        sashForm.layout(true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        for (int i = tabGroups.size() - 1; i >= 0; i--)
        {
            if (((ChartTabFolder)tabGroups.get(i)).getChartRow().equals(o))
            {
                ((ChartTabFolder)tabGroups.get(i)).dispose();
                tabGroups.remove(i);
                sashForm.layout(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.CTabFolder2Listener#close(org.eclipse.swt.custom.CTabFolderEvent)
     */
    public void close(CTabFolderEvent event)
    {
        ChartTabFolder folder = (ChartTabFolder)event.widget;
        ChartTabItem item = (ChartTabItem)event.item;
        
        if (MessageDialog.openConfirm(folder.getShell(), "Chart", "Do you really want to remove the " + item.getText() + " tab ?"))
        {
            final ChartTab tab = item.getChartTab();
            sashForm.getDisplay().asyncExec(new Runnable() {
               public void run()
               {
                   tab.getParent().getTabs().remove(tab);
                   if (tab.getParent().getTabs().size() == 0)
                   {
                       ChartRow row = tab.getParent();
                       row.getParent().getRows().remove(row);
                   }
                   CorePlugin.getRepository().save(chart);
               }
            });
        }
        
        event.doit = false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.CTabFolder2Listener#maximize(org.eclipse.swt.custom.CTabFolderEvent)
     */
    public void maximize(CTabFolderEvent event)
    {
        sashForm.setMaximizedControl((ChartTabFolder)event.widget);
        ((ChartTabFolder)event.widget).setMaximized(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.CTabFolder2Listener#minimize(org.eclipse.swt.custom.CTabFolderEvent)
     */
    public void minimize(CTabFolderEvent event)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.CTabFolder2Listener#restore(org.eclipse.swt.custom.CTabFolderEvent)
     */
    public void restore(CTabFolderEvent event)
    {
        sashForm.setMaximizedControl(null);
        ((ChartTabFolder)event.widget).setMaximized(false);
        ((ChartTabFolder)event.widget).setMinimized(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.custom.CTabFolder2Listener#showList(org.eclipse.swt.custom.CTabFolderEvent)
     */
    public void showList(CTabFolderEvent event)
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.events.PlotMouseListener#mouseDown(net.sourceforge.eclipsetrader.events.PlotMouseEvent)
     */
    public void mouseDown(PlotMouseEvent e)
    {
        if (!e.plot.getSelection())
        {
            for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
            {
                CTabFolder folder = (CTabFolder)iter.next();
                Plot plot = (Plot)folder.getSelection().getControl();
                if (plot == e.plot)
                {
                    if (!plot.getSelection())
                    {
                        plot.setSelection(true);
                        plot.getScalePlot().redraw();
                        plot.getScalePlot().update();
                    }
                }
                else
                {
                    if (plot.getIndicatorPlot().getSelection() != null || plot.getIndicatorPlot().getObjectSelection() != null)
                    {
                        plot.getIndicatorPlot().deselectAll();
                        plot.getIndicatorPlot().redrawAll();
                        plot.getIndicatorPlot().update();
                        getSite().getSelectionProvider().setSelection(new NullSelection());
                    }
                    if (plot.getSelection())
                    {
                        plot.setSelection(false);
                        plot.getScalePlot().redraw();
                        plot.getScalePlot().update();
                    }
                }
            }
            
            if (newChartObject == null)
                return;
        }
        
        if (newChartObject != null)
        {
            sashForm.setCursor(null);

            Plot plot = e.plot;
            ChartTabFolder folder = (ChartTabFolder)plot.getParent();
            ChartTabItem item = (ChartTabItem)folder.getSelection();
            item.createNewObject(newChartObject, e);
            
            newChartObject = null;
            return;
        }
        
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            Plot plot = (Plot)folder.getSelection().getControl();
            GC gc = new GC(plot.getIndicatorPlot());
            gc.drawLine(e.mouse.x, 0, e.mouse.x, plot.getSize().y);
            if (plot == e.plot)
            {
                gc.drawLine(0, e.mouse.y, plot.getSize().x, e.mouse.y);
                plot.getScalePlot().setLabel(e.mouse.y);
            }
            gc.dispose();
            plot.updateSummary(e.mouse.x);
        }
        datePlot.setLabel(e.mouse.x);
        oldMouseX = e.mouse.x;
        oldMouseY = e.mouse.y;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.events.PlotMouseListener#mouseMove(net.sourceforge.eclipsetrader.events.PlotMouseEvent)
     */
    public void mouseMove(PlotMouseEvent e)
    {
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            Plot plot = (Plot)folder.getSelection().getControl();
            plot.getIndicatorPlot().redraw(oldMouseX, 0, 1, plot.getSize().y, false);
            plot.getIndicatorPlot().update();
            if (plot == e.plot)
            {
                plot.getIndicatorPlot().redraw(0, oldMouseY, plot.getSize().x, 1, false);
                plot.getIndicatorPlot().update();
            }

            GC gc = new GC(plot.getIndicatorPlot());
            gc.drawLine(e.mouse.x, 0, e.mouse.x, plot.getSize().y);
            if (plot == e.plot)
            {
                gc.drawLine(0, e.mouse.y, plot.getSize().x, e.mouse.y);
                plot.getScalePlot().setLabel(e.mouse.y);
            }
            gc.dispose();
            plot.updateSummary(e.mouse.x);
        }
        datePlot.setLabel(e.mouse.x);
        oldMouseX = e.mouse.x;
        oldMouseY = e.mouse.y;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.events.PlotMouseListener#mouseUp(net.sourceforge.eclipsetrader.events.PlotMouseEvent)
     */
    public void mouseUp(PlotMouseEvent e)
    {
        Plot plot = (Plot)e.plot;
        CTabFolder folder = (CTabFolder)plot.getParent();
        ChartTabItem item = (ChartTabItem)folder.getSelection();
        for(Iterator iter = item.getObjects().iterator(); iter.hasNext(); )
        {
            ChartObject object = (ChartObject)iter.next();
            if (plot.getIndicatorPlot().getObjectSelection() == object.getData())
            {
                ObjectPlugin plugin = (ObjectPlugin)object.getData();
                object.setParameters(plugin.getSettings().getMap());
                CorePlugin.getRepository().save(chart);
            }
        }
        
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            folder = (CTabFolder)iter.next();
            plot = (Plot)folder.getSelection().getControl();
            plot.getIndicatorPlot().redraw(oldMouseX, 0, 1, plot.getSize().y, false);
            plot.getIndicatorPlot().update();
            if (plot == e.plot)
            {
                plot.getIndicatorPlot().redraw(0, oldMouseY, plot.getSize().x, 1, false);
                plot.getIndicatorPlot().update();
                plot.getScalePlot().hideLabel();
            }
            plot.updateSummary();
        }
        datePlot.hideLabel();
    }

    public class ChartTabFolder extends CTabFolder implements ICollectionObserver
    {
        private ChartRow chartRow;

        public ChartTabFolder(Composite parent, int style, ChartRow row)
        {
            super(parent, style|SWT.CLOSE);
            this.chartRow = row;
            this.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    ChartTabFolder.this.chartRow.getTabs().removeCollectionObserver(ChartTabFolder.this);
                }
            });

            this.chartRow.getTabs().addCollectionObserver(this);
            addCTabFolder2Listener(ChartView.this);
            setMaximizeVisible(true);
            setMinimizeVisible(false);
            setSimple(PlatformUI.getPreferenceStore().getBoolean("SHOW_TRADITIONAL_STYLE_TABS"));

            for (int t = 0; t < this.chartRow.getTabs().size(); t++)
                itemAdded(row.getTabs().get(t));
        }
        
        public ChartRow getChartRow()
        {
            return chartRow;
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Composite#checkSubclass()
         */
        protected void checkSubclass()
        {
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            new ChartTabItem(this, SWT.NONE, (ChartTab)o);
            ((ChartTab)o).clearChanged();
            
            if (getItemCount() == 1)
                setSelection(getItem(0));
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            CTabItem[] items = getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (((ChartTabItem)items[i]).getChartTab().equals(o))
                    ((ChartTabItem)items[i]).dispose();
            }
        }
    }
    
    public class ChartTabItem extends CTabItem implements Observer, ICollectionObserver, PlotSelectionListener
    {
        private ChartTab chartTab;
        private Plot plot;
        private List indicators = new ArrayList();
        private List objects = new ArrayList();
        
        public ChartTabItem(ChartTabFolder parent, int style, ChartTab tab)
        {
            super(parent, style);
            this.chartTab = tab;

            this.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    plot.removePlotMouseListener(ChartView.this);
                    plot.removePlotSelectionListener(ChartTabItem.this);
                    chartTab.getIndicators().removeCollectionObserver(ChartTabItem.this);
                    chartTab.getObjects().removeCollectionObserver(ChartTabItem.this);
                    chartTab.deleteObserver(ChartTabItem.this);
                }
            });

            this.chartTab.addObserver(this);
            this.chartTab.getIndicators().addCollectionObserver(this);
            this.chartTab.getObjects().addCollectionObserver(this);
            setText(this.chartTab.getLabel());

            plot = new Plot(getParent(), SWT.NONE);
            plot.setDatePlot(datePlot);
            plot.getIndicatorPlot().setPlotBounds(datePlot.getIndicatorPlot().getPlotBounds());
            plot.addPlotMouseListener(ChartView.this);
            plot.addPlotSelectionListener(ChartTabItem.this);
            setControl(plot);

            // Create popup menu
            MenuManager menuMgr = new MenuManager("#popupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
            menuMgr.setRemoveAllWhenShown(true);
            menuMgr.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager mgr)
                {
                    mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
                }
            });
            Menu menu = menuMgr.createContextMenu(plot.getIndicatorPlot());
            plot.getIndicatorPlot().setMenu(menu);
            getSite().registerContextMenu(menuMgr, getSite().getSelectionProvider());

            // Add indicators
            for (int i = 0; i < this.chartTab.getIndicators().size(); i++)
                itemAdded(this.chartTab.getIndicators().get(i));
            // Add objects
            for (int i = 0; i < this.chartTab.getObjects().size(); i++)
                itemAdded(this.chartTab.getObjects().get(i));
        }
        
        public ChartTab getChartTab()
        {
            return chartTab;
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Composite#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        public List getObjects()
        {
            return objects;
        }
        
        public void createNewObject(ChartObject object, PlotMouseEvent e)
        {
            object.setParent(chartTab);
            chartTab.getObjects().add(object);

            ObjectPlugin plugin = (ObjectPlugin)object.getData();
            if (plugin != null)
                plot.createNewObject(plugin, e);
        }

        public void update()
        {
            setText(this.chartTab.getLabel());

            plot.setRedraw(false);
            plot.clearIndicators();
            if (plot.getIndicatorPlot().getSelection() != null)
                plot.getIndicatorPlot().setSelection(null);
            
            for (int i = 0; i < this.chartTab.getIndicators().size(); i++)
                itemAdded(this.chartTab.getIndicators().get(i));

            for (int i = 0; i < this.chartTab.getObjects().size(); i++)
            {
                ChartObject object = (ChartObject)this.chartTab.getObjects().get(i);
                ObjectPlugin plugin = (ObjectPlugin)object.getData();
                if (plugin != null)
                {
                    Settings settings = new Settings();
                    for (Iterator iter = object.getParameters().keySet().iterator(); iter.hasNext(); )
                    {
                        String key = (String)iter.next();
                        settings.set(key, (String)object.getParameters().get(key));
                    }
                    plugin.setSettings(settings);
                }
            }
            
            plot.setRedraw(true);
            if (plot.getIndicatorPlot().getObjectSelection() != null)
                getSite().getSelectionProvider().setSelection(new NullSelection());
        }
        
        /* (non-Javadoc)
         * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
         */
        public void update(Observable o, Object arg)
        {
            update();
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
         */
        public void itemAdded(Object o)
        {
            if (o instanceof ChartIndicator)
            {
                ChartIndicator indicator = (ChartIndicator)o;
                indicator.clearChanged();
                
                IndicatorPlugin plugin = ChartsPlugin.createIndicatorPlugin(indicator.getPluginId());
                indicator.setData(plugin);
                if (plugin != null)
                {
                    Settings settings = new Settings();
                    for (Iterator iter = indicator.getParameters().keySet().iterator(); iter.hasNext(); )
                    {
                        String key = (String)iter.next();
                        settings.set(key, (String)indicator.getParameters().get(key));
                    }
                    plugin.setParameters(settings);
                    
                    plugin.setInput(datePlot.getBarData());
                    plugin.clearOutput();
                    plugin.calculate();

                    plot.addIndicator(plugin.getOutput());
                }

                indicators.add(indicator);
            }
            if (o instanceof ChartObject)
            {
                ChartObject object = (ChartObject)o;
                object.clearChanged();
                
                ObjectPlugin plugin = ChartsPlugin.createObjectPlugin(object.getPluginId());
                object.setData(plugin);
                if (plugin != null)
                {
                    Settings settings = new Settings();
                    for (Iterator iter = object.getParameters().keySet().iterator(); iter.hasNext(); )
                    {
                        String key = (String)iter.next();
                        settings.set(key, (String)object.getParameters().get(key));
                    }
                    plugin.setSettings(settings);
                    
                    plot.addObject(plugin);
                }

                objects.add(object);
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
         */
        public void itemRemoved(Object o)
        {
            if (o instanceof ChartIndicator)
            {
                ChartIndicator indicator = (ChartIndicator)o;
                IndicatorPlugin plugin = (IndicatorPlugin)indicator.getData();
                if (plugin != null)
                    plot.removeIndicator(plugin.getOutput());
                indicators.remove(indicator);
                getSite().getSelectionProvider().setSelection(new NullSelection());
            }
            if (o instanceof ChartObject)
            {
                ChartObject object = (ChartObject)o;
                ObjectPlugin plugin = (ObjectPlugin)object.getData();
                if (plugin != null)
                {
                    plot.removeObject(plugin);
                    plugin.dispose();
                }
                objects.remove(object);
                getSite().getSelectionProvider().setSelection(new NullSelection());
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.charts.events.PlotSelectionListener#plotSelected(net.sourceforge.eclipsetrader.charts.events.PlotSelectionEvent)
         */
        public void plotSelected(PlotSelectionEvent e)
        {
            if (e.indicator != null || e.object != null)
            {
                for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
                {
                    CTabFolder folder = (CTabFolder)iter.next();
                    CTabItem[] items = folder.getItems();
                    for (int i = 0; i < items.length; i++)
                    {
                        if (e.plot == ((ChartTabItem)items[i]).getPlot())
                        {
                            if (!((ChartTabItem)items[i]).getPlot().getSelection())
                            {
                                ((ChartTabItem)items[i]).getPlot().setSelection(true);
                                ((ChartTabItem)items[i]).getPlot().getScalePlot().redraw();
                            }
                        }
                        else
                        {
                            ((ChartTabItem)items[i]).getPlot().getIndicatorPlot().deselectAll();
                            ((ChartTabItem)items[i]).getPlot().getIndicatorPlot().redrawAll();
                            if (((ChartTabItem)items[i]).getPlot().getSelection())
                            {
                                ((ChartTabItem)items[i]).getPlot().setSelection(false);
                                ((ChartTabItem)items[i]).getPlot().getScalePlot().redraw();
                            }
                        }
                    }
                }
            }

            if (e.indicator != null)
            {
                for (Iterator iter = indicators.iterator(); iter.hasNext(); )
                {
                    ChartIndicator indicator = (ChartIndicator)iter.next();
                    if (((IndicatorPlugin)indicator.getData()).getOutput() == e.indicator)
                    {
                        getSite().getSelectionProvider().setSelection(new IndicatorSelection(indicator));
                        return;
                    }
                }
                getSite().getSelectionProvider().setSelection(new NullSelection());
            }
            else if (e.object != null)
            {
                for (Iterator iter = objects.iterator(); iter.hasNext(); )
                {
                    ChartObject object = (ChartObject)iter.next();
                    if (object.getData() == e.object)
                    {
                        getSite().getSelectionProvider().setSelection(new ObjectSelection(object));
                        return;
                    }
                }
                getSite().getSelectionProvider().setSelection(new NullSelection());
            }
            else
                getSite().getSelectionProvider().setSelection(new NullSelection());
        }
        
        public Plot getPlot()
        {
            return plot;
        }
    }

    public List getTabGroups()
    {
        return tabGroups;
    }

    public DatePlot getDatePlot()
    {
        return datePlot;
    }
}
