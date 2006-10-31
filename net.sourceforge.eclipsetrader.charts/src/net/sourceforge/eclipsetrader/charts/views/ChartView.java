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

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.DatePlot;
import net.sourceforge.eclipsetrader.charts.IIndicatorPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPlugin;
import net.sourceforge.eclipsetrader.charts.Plot;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.charts.actions.AutoScaleAction;
import net.sourceforge.eclipsetrader.charts.actions.Set10MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set15MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set1DayIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set1MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set2MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set30MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set5MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.Set60MinuteIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.SetCustomPeriodAction;
import net.sourceforge.eclipsetrader.charts.actions.SetLast2YearsPeriodAction;
import net.sourceforge.eclipsetrader.charts.actions.SetLast6MonthsPeriodAction;
import net.sourceforge.eclipsetrader.charts.actions.SetLastYearPeriodAction;
import net.sourceforge.eclipsetrader.charts.actions.SetMonthlyIntervalAction;
import net.sourceforge.eclipsetrader.charts.actions.SetViewAllAction;
import net.sourceforge.eclipsetrader.charts.actions.SetWeeklyIntervalAction;
import net.sourceforge.eclipsetrader.charts.dialogs.ChartSettingsDialog;
import net.sourceforge.eclipsetrader.charts.events.ChartSelectionProvider;
import net.sourceforge.eclipsetrader.charts.events.IndicatorSelection;
import net.sourceforge.eclipsetrader.charts.events.ObjectSelection;
import net.sourceforge.eclipsetrader.charts.events.PlotEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotListener;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseListener;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionListener;
import net.sourceforge.eclipsetrader.charts.events.TabSelection;
import net.sourceforge.eclipsetrader.charts.internal.CopyAction;
import net.sourceforge.eclipsetrader.charts.internal.CutAction;
import net.sourceforge.eclipsetrader.charts.internal.DeleteAction;
import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.charts.internal.PasteAction;
import net.sourceforge.eclipsetrader.charts.internal.PasteSpecialAction;
import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.FeedMonitor;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.IHistoryFeed;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Chart;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;
import net.sourceforge.eclipsetrader.core.db.ChartObject;
import net.sourceforge.eclipsetrader.core.db.ChartRow;
import net.sourceforge.eclipsetrader.core.db.ChartTab;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.NullSelection;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class ChartView extends ViewPart implements PlotMouseListener, CTabFolder2Listener, ICollectionObserver, Observer, ISelectionListener
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.views.chart"; //$NON-NLS-1$
    public static final int PERIOD_ALL = 0;
    public static final int PERIOD_LAST6MONTHS = 1;
    public static final int PERIOD_LASTYEAR = 2;
    public static final int PERIOD_LAST2YEARS = 3;
    public static final int PERIOD_CUSTOM = -1;
    public static final int HIDE_TABS_NEVER = 0;
    public static final int HIDE_TABS_ONLYONE = 1;
    public static final String PREFS_FOLLOW_SELECTION = "followSelection";
    public static final String PREFS_SHOW_ADJUSTED_VALUES = "showAdjustedValues";
    public static final String PREFS_SHOW_MARKETVALUE = "showMarketValue";
    static boolean DEFAULT_FOLLOW_SELECTION = false;
    static boolean DEFAULT_SHOW_ADJUSTED_VALUES = false;
    static boolean DEFAULT_SHOW_MARKET_VALUE = false;
    Chart chart;
    SashForm sashForm;
    DatePlot datePlot;
    ScrollBar hBar;
    List tabGroups = new ArrayList();
    Security security;
    int oldMouseX = -1, oldMouseY = -1;
    boolean autoScale = false;
    boolean followSelection = false;
    boolean showAdjustedValues = false;
    boolean showMarketValue = false;
    ChartObject newChartObject;
    Action viewAll = new SetViewAllAction(this);
    Action viewLast2Years = new SetLast2YearsPeriodAction(this);
    Action viewLastYear = new SetLastYearPeriodAction(this);
    Action viewLast6Months = new SetLast6MonthsPeriodAction(this);
    Action viewCustom = new SetCustomPeriodAction(this);
    Action monthlyAction = new SetMonthlyIntervalAction(this);
    Action weeklyAction = new SetWeeklyIntervalAction(this);
    Action dailyAction = new Set1DayIntervalAction(this);
    Action minute1Action = new Set1MinuteIntervalAction(this);
    Action minute2Action = new Set2MinuteIntervalAction(this);
    Action minute5Action = new Set5MinuteIntervalAction(this);
    Action minute10Action = new Set10MinuteIntervalAction(this);
    Action minute15Action = new Set15MinuteIntervalAction(this);
    Action minute30Action = new Set30MinuteIntervalAction(this);
    Action minute60Action = new Set60MinuteIntervalAction(this);
    Action autoScaleAction = new AutoScaleAction(this);
    Action toggleFollowSelectionAction;
    Action toggleAdjustedValuesAction;
    Action toggleMarketValueAction;
    Action cutAction;
    Action copyAction;
    Action pasteAction;
    Action pasteSpecialAction;
    Action deleteAction;
    PreferenceStore preferences;
    Logger logger = Logger.getLogger(getClass());
    DropTargetListener dropTargetListener = new DropTargetListener() {
        public void dragEnter(DropTargetEvent event)
        {
            event.detail = DND.DROP_COPY;
            event.currentDataType = null;
            
            TransferData[] data = event.dataTypes;
            if (event.currentDataType == null)
            {
                for (int i = 0; i < data.length; i++)
                {
                    if (SecurityTransfer.getInstance().isSupportedType(data[i]))
                    {
                        event.currentDataType = data[i];
                        break;
                    }
                }
            }
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
            if (SecurityTransfer.getInstance().isSupportedType(event.currentDataType))
            {
                Security[] securities = (Security[]) event.data;
                setSecurity(securities[0]);
            }
        }
    };
    Runnable updateMarketValue = new Runnable() {
        public void run()
        {
            if (!sashForm.isDisposed())
            {
                Control[] rows = sashForm.getChildren();
                for (int r = 0; r < rows.length; r++)
                {
                    if (rows[r] instanceof ChartTabFolder)
                    {
                        CTabItem[] items = ((ChartTabFolder)rows[r]).getItems();
                        for (int i = 0; i < items.length; i++)
                        {
                            if (items[i] instanceof ChartTabItem)
                            {
                                ChartTab tab = ((ChartTabItem)items[i]).getChartTab();
                                ChartIndicator[] obj = (ChartIndicator[])tab.getIndicators().toArray(new ChartIndicator[tab.getIndicators().size()]);
                                for (int x = 0; x < obj.length; x++)
                                {
                                    if (obj[x].getPluginId().equals("net.sourceforge.eclipsetrader.indicators.bars") || obj[x].getPluginId().equals("net.sourceforge.eclipsetrader.indicators.line"))
                                    {
                                        Plot plot = ((ChartTabItem)items[i]).getPlot();
                                        if (showMarketValue && security.getQuote() != null)
                                        {
                                            plot.getScalePlot().setMarketValue(security.getQuote().getLast());
                                            plot.getIndicatorPlot().setMarketValue(security.getQuote().getLast());
                                        }
                                        else
                                        {
                                            plot.getIndicatorPlot().hideMarketValue();
                                            plot.getScalePlot().hideMarketValue();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    Observer quoteObserver = new Observer() {
        public void update(Observable o, Object arg)
        {
            sashForm.getDisplay().timerExec(200, updateMarketValue);
        }
    };
    IPropertyChangeListener pluginPropertiesChangeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(ChartsPlugin.PREFS_EXTEND_SCALE))
            {
                int range = ((Integer)event.getNewValue()).intValue();
                for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
                {
                    CTabFolder folder = (CTabFolder)iter.next();
                    CTabItem[] items = folder.getItems();
                    for (int i = 0; i < items.length; i++)
                        ((Plot)items[i].getControl()).getScaler().setExtendRange(range);
                }
                redrawView();
            }
            else if (event.getProperty().equals(ChartsPlugin.PREFS_EXTEND_PERIOD))
            {
                datePlot.setExtendPeriod(((Integer)event.getNewValue()).intValue());
                updateView();
            }
        }
    };
    
    public static IPath getPreferenceStoreLocation(Chart chart)
    {
        return ChartsPlugin.getDefault().getStateLocation().append("chart." + String.valueOf(chart.getId()) + ".prefs");
    }

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
        
        IMenuManager newObjectMenu = new MenuManager(Messages.ChartView_NewObject, "newObject"); //$NON-NLS-2$
        menuManager.appendToGroup("group2", newObjectMenu); //$NON-NLS-1$

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
                        object.setPluginId(getId());
                        setNewChartObject(object);
                    }
                };
                action.setId(element.getAttribute("id")); //$NON-NLS-1$
                newObjectMenu.add(action);
            }
        }

        IMenuManager periodMenu = new MenuManager(Messages.ChartView_Period, "period"); //$NON-NLS-2$
        menuManager.appendToGroup("group3", periodMenu); //$NON-NLS-1$
        periodMenu.add(viewAll);
        periodMenu.add(viewLast2Years);
        periodMenu.add(viewLastYear);
        periodMenu.add(viewLast6Months);
        periodMenu.add(viewCustom);
        
        IMenuManager intervalMenu = new MenuManager(Messages.ChartView_SetInterval, "interval"); //$NON-NLS-2$
        menuManager.appendToGroup("group3", intervalMenu); //$NON-NLS-1$
        intervalMenu.add(monthlyAction);
        intervalMenu.add(weeklyAction);
        intervalMenu.add(dailyAction);
        intervalMenu.add(minute60Action);
        intervalMenu.add(minute30Action);
        intervalMenu.add(minute15Action);
        intervalMenu.add(minute10Action);
        intervalMenu.add(minute5Action);
        intervalMenu.add(minute2Action);
        intervalMenu.add(minute1Action);

        menuManager.appendToGroup("group3", new Separator()); //$NON-NLS-1$
        menuManager.appendToGroup("group3", autoScaleAction); //$NON-NLS-1$
        
        toggleAdjustedValuesAction = new Action("Show adjusted values", Action.AS_CHECK_BOX) {
            public void run()
            {
                showAdjustedValues = toggleAdjustedValuesAction.isChecked();
                preferences.setValue(PREFS_SHOW_ADJUSTED_VALUES, showAdjustedValues);
                updateView();
            }
        };
        menuManager.appendToGroup("group3", toggleAdjustedValuesAction); //$NON-NLS-1$
        
        toggleMarketValueAction = new Action("Show market value", Action.AS_CHECK_BOX) {
            public void run()
            {
                showMarketValue = toggleMarketValueAction.isChecked();
                preferences.setValue(PREFS_SHOW_MARKETVALUE, showMarketValue);
                if (showMarketValue)
                {
                    security.getQuoteMonitor().addObserver(quoteObserver);
                    quoteObserver.update(security, security.getQuote());
                    FeedMonitor.monitor(security);
                }
                else
                {
                    security.getQuoteMonitor().deleteObserver(quoteObserver);
                    FeedMonitor.cancelMonitor(security);
                }
                quoteObserver.update(security, security.getQuote());
            }
        };
        menuManager.appendToGroup("group3", toggleMarketValueAction); //$NON-NLS-1$
        
        toggleFollowSelectionAction = new Action("Follow Security Selection", Action.AS_CHECK_BOX) {
            public void run()
            {
                followSelection = toggleFollowSelectionAction.isChecked();
                preferences.setValue(PREFS_FOLLOW_SELECTION, followSelection);
                setContentDescription(followSelection ? security.getDescription() : "");
            }
        };
        menuManager.appendToGroup("group3", toggleFollowSelectionAction); //$NON-NLS-1$
        
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
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        target.addDropListener(dropTargetListener);
        
        Composite content = new Composite(parent, SWT.H_SCROLL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 2;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        content.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_ESCAPE)
                {
                    newChartObject = null;
                    sashForm.setCursor(null);
                }
            }
        });
        
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
        getSite().getSelectionProvider().setSelection(new NullSelection());

        getSite().getPage().addSelectionListener(this);

        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("settings", new Action() { //$NON-NLS-1$
            public void run()
            {
                ChartSettingsDialog dlg = new ChartSettingsDialog(getChart(), getViewSite().getShell());
                dlg.open();
            }
        });
        actionBars.setGlobalActionHandler("cut", cutAction = new CutAction(this)); //$NON-NLS-1$
        actionBars.setGlobalActionHandler("copy", copyAction = new CopyAction(this)); //$NON-NLS-1$
        actionBars.setGlobalActionHandler("paste", pasteAction = new PasteAction(this)); //$NON-NLS-1$
        actionBars.setGlobalActionHandler("pasteSpecial", pasteSpecialAction = new PasteSpecialAction(this)); //$NON-NLS-1$
        actionBars.setGlobalActionHandler("delete", deleteAction = new DeleteAction(this)); //$NON-NLS-1$

        Integer id = new Integer(Integer.parseInt(getViewSite().getSecondaryId()));
        chart = (Chart)CorePlugin.getRepository().load(Chart.class, id);
        
        preferences = new PreferenceStore(getPreferenceStoreLocation(chart).toOSString());
        preferences.setDefault(PREFS_FOLLOW_SELECTION, DEFAULT_FOLLOW_SELECTION);
        preferences.setDefault(PREFS_SHOW_ADJUSTED_VALUES, DEFAULT_SHOW_ADJUSTED_VALUES);
        preferences.setDefault(PREFS_SHOW_MARKETVALUE, DEFAULT_SHOW_MARKET_VALUE);
        try {
            preferences.load();
        } catch(Exception e) {
        }
        
        security = chart.getSecurity();
        setPartName(chart.getTitle());
        setTitleToolTip(security.getDescription());

        autoScale = chart.isAutoScale();
        followSelection = preferences.getBoolean(PREFS_FOLLOW_SELECTION);
        showAdjustedValues = preferences.getBoolean(PREFS_SHOW_ADJUSTED_VALUES);
        showMarketValue = preferences.getBoolean(PREFS_SHOW_MARKETVALUE);
        updateActionBars();
        setContentDescription(followSelection ? security.getDescription() : "");

        IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();
        datePlot.setExtendPeriod(pluginPreferences.getInt(ChartsPlugin.PREFS_EXTEND_PERIOD));
        pluginPreferences.addPropertyChangeListener(pluginPropertiesChangeListener);
        
        try {
            sashForm.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    datePlot.setInterval(chart.getCompression());
                    if (datePlot.getInterval() < BarData.INTERVAL_DAILY)
                        datePlot.setBarData(new BarData(security.getIntradayHistory()).getCompressed(datePlot.getInterval()));
                    else
                    {
                        int period = chart.getPeriod();
                        int size = security.getHistory().size();
                        BarData barData = new BarData(showAdjustedValues ? security.getAdjustedHistory() : security.getHistory());
                        
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
                                    calendar.setTime(chart.getBeginDate());
                                    end = chart.getEndDate(); 
                                    break;
                            }
                            barData = new BarData(security.getHistory()).getPeriod(calendar.getTime(), end);
                        }

                        if (datePlot.getInterval() != BarData.INTERVAL_DAILY)
                            barData = barData.getCompressed(datePlot.getInterval());

                        datePlot.setBarData(barData);
                    }

                    try {
                        for (int r = 0; r < chart.getRows().size(); r++)
                            itemAdded(chart.getRows().get(r));
                        chart.getRows().addCollectionObserver(ChartView.this);
                        chart.addObserver(ChartView.this);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    if (showMarketValue)
                    {
                        security.getQuoteMonitor().addObserver(quoteObserver);
                        quoteObserver.update(security, security.getQuote());
                        FeedMonitor.monitor(security);
                    }

                    boolean askInitialUpdate = false;
                    if (datePlot.getInterval() < BarData.INTERVAL_DAILY)
                        askInitialUpdate = (security.getIntradayHistory().size() == 0);
                    else
                        askInitialUpdate = (security.getHistory().size() == 0);
                    if (askInitialUpdate)
                    {
                        if (MessageDialog.openQuestion(getViewSite().getShell(), chart.getTitle(), Messages.ChartView_NoDataMessage))
                        {
                            String id = ""; //$NON-NLS-1$
                            if (chart.getSecurity().getHistoryFeed() != null)
                                id = chart.getSecurity().getHistoryFeed().getId();
                            final IHistoryFeed feed = CorePlugin.createHistoryFeedPlugin(id);
                            if (feed != null)
                            {
                                Job job = new Job(Messages.ChartView_UpdateChartMessage) {
                                    protected IStatus run(IProgressMonitor monitor)
                                    {
                                        monitor.beginTask(Messages.ChartView_UpdatingMessage + chart.getSecurity().getDescription(), 1);
                                        int interval = IHistoryFeed.INTERVAL_DAILY;
                                        if (getInterval() < BarData.INTERVAL_DAILY)
                                            interval = IHistoryFeed.INTERVAL_MINUTE;
                                        feed.updateHistory(chart.getSecurity(), interval);
                                        monitor.worked(1);
                                        monitor.done();
                                        return Status.OK_STATUS;
                                    }
                                };
                                job.setUser(true);
                                job.schedule();
                            }
                        }
                    }
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        ChartsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(pluginPropertiesChangeListener);

        if (showMarketValue)
        {
            security.getQuoteMonitor().deleteObserver(quoteObserver);
            FeedMonitor.cancelMonitor(security);
        }
        getSite().getPage().removeSelectionListener(this);
        if (chart != null)
        {
            chart.deleteObserver(ChartView.this);
            chart.getRows().removeCollectionObserver(ChartView.this);
        }

        try {
            if (preferences != null)
                preferences.save();
        } catch(Exception e) {
            Logger.getLogger(getClass()).warn(e);
        }
        
        super.dispose();
    }
    
    public void updateActionBars()
    {
        viewAll.setChecked(chart.getPeriod() == PERIOD_ALL);
        viewLast2Years.setChecked(chart.getPeriod() == PERIOD_LAST2YEARS);
        viewLastYear.setChecked(chart.getPeriod() == PERIOD_LASTYEAR);
        viewLast6Months.setChecked(chart.getPeriod() == PERIOD_LAST6MONTHS);
        viewCustom.setChecked(chart.getPeriod() == PERIOD_CUSTOM);
        
        minute1Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE1);
        minute2Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE2);
        minute5Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE5);
        minute10Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE10);
        minute15Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE15);
        minute30Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE30);
        minute60Action.setChecked(chart.getCompression() == BarData.INTERVAL_MINUTE60);
        dailyAction.setChecked(chart.getCompression() == BarData.INTERVAL_DAILY);
        weeklyAction.setChecked(chart.getCompression() == BarData.INTERVAL_WEEKLY);
        monthlyAction.setChecked(chart.getCompression() == BarData.INTERVAL_MONTHLY);
        
        autoScaleAction.setChecked(autoScale);
        toggleFollowSelectionAction.setChecked(followSelection);
        toggleAdjustedValuesAction.setChecked(showAdjustedValues);
        toggleMarketValueAction.setChecked(showMarketValue);
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
        chart.setCompression(interval);
        CorePlugin.getRepository().save(chart);
    }
    
    public void setAutoScale(boolean autoScale)
    {
        this.autoScale = autoScale;
        
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            CTabItem[] items = folder.getItems();
            for (int i = 0; i < items.length; i++)
                ((Plot)items[i].getControl()).getIndicatorPlot().setAutoScale(autoScale);
        }
        
        chart.setAutoScale(autoScale);
        CorePlugin.getRepository().save(chart);
    }

    public int getPeriod()
    {
        return chart.getPeriod();
    }

    public void setPeriod(int period)
    {
        chart.setPeriod(period);
        CorePlugin.getRepository().save(chart);
    }

    public void setPeriod(Date beginDate, Date endDate)
    {
        chart.setPeriod(PERIOD_CUSTOM);
        chart.setBeginDate(beginDate);
        chart.setEndDate(endDate);
        CorePlugin.getRepository().save(chart);
    }

    public void setNewChartObject(ChartObject object)
    {
        this.newChartObject = object;
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            Plot plot = (Plot)folder.getSelection().getControl();
            if (plot.getIndicatorPlot().getSelection() != null || plot.getIndicatorPlot().getObjectSelection() != null)
            {
                plot.getIndicatorPlot().deselectAll();
                plot.getIndicatorPlot().redrawAll();
                plot.getIndicatorPlot().update();
            }
        }
        sashForm.setCursor(new Cursor(null, SWT.CURSOR_CROSS));
    }
    
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
            {
                ((Plot)items[i].getControl()).getIndicatorPlot().setPlotBounds(bounds);
                if (autoScale)
                    ((Plot)items[i].getControl()).updateScale();
            }
        }
    }
    
    public void setSecurity(Security newSecurity)
    {
        if (!newSecurity.equals(security))
        {
            if (showMarketValue)
            {
                security.getQuoteMonitor().deleteObserver(quoteObserver);
                FeedMonitor.cancelMonitor(security);
            }
            
            security = newSecurity;
            setTitleToolTip(newSecurity.getDescription());
            setContentDescription(followSelection ? security.getDescription() : "");

            chart.setSecurity(newSecurity);
            for(Iterator iter1 = chart.getRows().iterator(); iter1.hasNext(); )
            {
                ChartRow row = (ChartRow)iter1.next();
                for (Iterator iter2 = row.getTabs().iterator(); iter2.hasNext(); )
                {
                    ChartTab tab = (ChartTab)iter2.next();
                    ChartObject[] obj = (ChartObject[])tab.getObjects().toArray(new ChartObject[tab.getObjects().size()]);
                    for (int i = 0; i < obj.length; i++)
                        tab.getObjects().remove(obj[i]);
                }
            }

            if (showMarketValue)
            {
                security.getQuoteMonitor().addObserver(quoteObserver);
                quoteObserver.update(security, security.getQuote());
                FeedMonitor.monitor(security);
            }
            
            CorePlugin.getRepository().save(chart);
        }
    }
    
    public void redrawView()
    {
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            CTabItem[] items = folder.getItems();
            for (int i = 0; i < items.length; i++)
            {
                if (autoScale)
                    ((Plot)items[i].getControl()).updateScale();
                else
                    ((Plot)items[i].getControl()).resetScale();
            }
        }
    }
    
    public void updateView()
    {
        datePlot.setInterval(chart.getCompression());
        if (datePlot.getInterval() < BarData.INTERVAL_DAILY)
            datePlot.setBarData(new BarData(security.getIntradayHistory()).getCompressed(datePlot.getInterval()));
        else
        {
            int period = chart.getPeriod();
            int size = security.getHistory().size();
            BarData barData = new BarData(showAdjustedValues ? security.getAdjustedHistory() : security.getHistory());
            
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
                        calendar.setTime(chart.getBeginDate());
                        end = chart.getEndDate(); 
                        break;
                }
                barData = new BarData(security.getHistory()).getPeriod(calendar.getTime(), end);
            }

            if (datePlot.getInterval() != BarData.INTERVAL_DAILY)
                barData = barData.getCompressed(datePlot.getInterval());

            datePlot.setBarData(barData);
        }
        
        for(Iterator iter = tabGroups.iterator(); iter.hasNext(); )
        {
            CTabFolder folder = (CTabFolder)iter.next();
            CTabItem[] items = folder.getItems();
            for (int i = 0; i < items.length; i++)
                ((ChartTabItem)items[i]).update();
        }

        quoteObserver.update(security, security.getQuote());
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
                {
                    setPartName(chart.getTitle());
                    updateActionBars();
                    updateView();
                }
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
        
        if (MessageDialog.openQuestion(folder.getShell(), getPartName(), Messages.ChartView_DeleteMessagePrefix + item.getText() + Messages.ChartView_DeleteMessageSuffix))
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
                   getSite().getSelectionProvider().setSelection(new NullSelection());
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
                        getSite().getSelectionProvider().setSelection(new TabSelection(((ChartTabItem) folder.getSelection()).getChartTab()));
                    }
                }
                else
                {
                    if (plot.getIndicatorPlot().getSelection() != null || plot.getIndicatorPlot().getObjectSelection() != null)
                    {
                        plot.getIndicatorPlot().deselectAll();
                        plot.getIndicatorPlot().redrawAll();
                        plot.getIndicatorPlot().update();
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

        if (e.button == 1)
        {
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        if (followSelection)
        {
            if (selection instanceof SecuritySelection)
                setSecurity(((SecuritySelection) selection).getSecurity());
        }
    }

    public class ChartTabFolder extends CTabFolder implements ICollectionObserver, IPropertyChangeListener
    {
        private ChartRow chartRow;
        private int autoHideTabs = HIDE_TABS_NEVER;

        public ChartTabFolder(Composite parent, int style, ChartRow row)
        {
            super(parent, style|SWT.CLOSE);
            this.chartRow = row;
            this.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    ChartsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(ChartTabFolder.this);
                    PlatformUI.getPreferenceStore().removePropertyChangeListener(ChartTabFolder.this);
                    ChartTabFolder.this.chartRow.getTabs().removeCollectionObserver(ChartTabFolder.this);
                }
            });

            this.chartRow.getTabs().addCollectionObserver(this);
            addCTabFolder2Listener(ChartView.this);
            setMaximizeVisible(true);
            setMinimizeVisible(false);
            setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS)); //$NON-NLS-1$
            addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if (e.item instanceof ChartTabItem)
                    {
                        for (Iterator iter = tabGroups.iterator(); iter.hasNext(); )
                        {
                            CTabItem[] items = ((CTabFolder) iter.next()).getItems();
                            for(int i = 0; i < items.length; i++)
                            {
                                Plot plot = ((ChartTabItem) items[i]).getPlot();
                                if (items[i] == e.item)
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
                                    if (plot.getSelection())
                                    {
                                        plot.setSelection(false);
                                        plot.getScalePlot().redraw();
                                        plot.getScalePlot().update();
                                    }
                                    if (plot.getIndicatorPlot().getSelection() != null || plot.getIndicatorPlot().getObjectSelection() != null)
                                    {
                                        plot.getIndicatorPlot().deselectAll();
                                        plot.getIndicatorPlot().redrawAll();
                                        plot.getIndicatorPlot().update();
                                    }
                                }
                            }
                        }
                        getSite().getSelectionProvider().setSelection(new TabSelection(((ChartTabItem) e.item).getChartTab()));
                    }
                }
            });
            
            PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
            IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();
            pluginPreferences.addPropertyChangeListener(this);
            autoHideTabs = pluginPreferences.getInt(ChartsPlugin.PREFS_HIDE_TABS);

            for (int t = 0; t < this.chartRow.getTabs().size(); t++)
            {
                ChartTab chartTab = (ChartTab)row.getTabs().get(t);
                new ChartTabItem(this, SWT.NONE, chartTab);
                chartTab.clearChanged();
            }

            updateTabsVisibility();
            if (getItemCount() != 0)
                setSelection(getItem(0));
        }
        
        protected void updateTabsVisibility()
        {
            if (autoHideTabs == HIDE_TABS_NEVER)
                setTabHeight(-1);
            else if (autoHideTabs == HIDE_TABS_ONLYONE)
                setTabHeight(getItemCount() == 1 ? 0 : -1);
            layout();
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
            
            setSelection(getItem(getItemCount() - 1));

            updateTabsVisibility();
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

            updateTabsVisibility();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event)
        {
            String property = event.getProperty(); 
            if (property.equals(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS))
                setSimple(((Boolean)event.getNewValue()).booleanValue());
            else if (property.equals(ChartsPlugin.PREFS_HIDE_TABS))
            {
                autoHideTabs = ((Integer)event.getNewValue()).intValue();
                updateTabsVisibility();
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
            plot.getScaler().setExtendRange(ChartsPlugin.getDefault().getPreferenceStore().getInt(ChartsPlugin.PREFS_EXTEND_SCALE));
            plot.getIndicatorPlot().setPlotBounds(datePlot.getIndicatorPlot().getPlotBounds());
            plot.addPlotMouseListener(ChartView.this);
            plot.addPlotSelectionListener(ChartTabItem.this);
            plot.getIndicatorPlot().setAutoScale(autoScale);
            setControl(plot);
            
            plot.getIndicatorPlot().addControlListener(new ControlAdapter() {
                public void controlResized(ControlEvent e)
                {
                    if (autoScale)
                        plot.updateScale();
                }
            });

            // Create popup menu
            MenuManager menuMgr = new MenuManager("#popupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
            menuMgr.setRemoveAllWhenShown(true);
            menuMgr.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager mgr)
                {
                    mgr.add(new GroupMarker("top")); //$NON-NLS-1$
                    mgr.add(new Separator());
                    mgr.add(cutAction);
                    mgr.add(copyAction);
                    mgr.add(pasteAction);
                    mgr.add(pasteSpecialAction);
                    mgr.add(new Separator());
                    mgr.add(deleteAction);
                    mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
                    mgr.add(new GroupMarker("bottom")); //$NON-NLS-1$
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
            {
                plot.createNewObject(plugin, e);
                getSite().getSelectionProvider().setSelection(new ObjectSelection(object));
                logger.debug("object " + object.getPluginId() + " selected");
            }
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
            if (autoScale)
                plot.updateScale();
            
            plot.setRedraw(true);
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
                
                IIndicatorPlugin plugin = ChartsPlugin.createIndicatorPlugin(indicator.getPluginId());
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
                if (autoScale)
                    plot.updateScale();
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
                IIndicatorPlugin plugin = (IIndicatorPlugin)indicator.getData();
                if (plugin != null)
                    plot.removeIndicator(plugin.getOutput());
                indicators.remove(indicator);
                getSite().getSelectionProvider().setSelection(new TabSelection(this.chartTab));
                if (autoScale)
                    plot.updateScale();
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
                getSite().getSelectionProvider().setSelection(new TabSelection(this.chartTab));
            }
        }

        /* (non-Javadoc)
         * @see net.sourceforge.eclipsetrader.charts.events.PlotSelectionListener#plotSelected(net.sourceforge.eclipsetrader.charts.events.PlotSelectionEvent)
         */
        public void plotSelected(PlotSelectionEvent e)
        {
            logger.debug("enter plotSelected");
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
                    if (((IIndicatorPlugin)indicator.getData()).getOutput() == e.indicator)
                    {
                        getSite().getSelectionProvider().setSelection(new IndicatorSelection(indicator));
                        logger.debug("indicator " + indicator.getPluginId() + " selected");
                        return;
                    }
                }
                getSite().getSelectionProvider().setSelection(new TabSelection(this.chartTab));
                logger.debug("indicator " + e.indicator.getClass().getName() + " not found");
            }
            else if (e.object != null)
            {
                for (Iterator iter = objects.iterator(); iter.hasNext(); )
                {
                    ChartObject object = (ChartObject)iter.next();
                    if (object.getData() == e.object)
                    {
                        getSite().getSelectionProvider().setSelection(new ObjectSelection(object));
                        logger.debug("object " + object.getPluginId() + " selected");
                        return;
                    }
                }
                getSite().getSelectionProvider().setSelection(new TabSelection(this.chartTab));
                logger.debug("object " + e.object.getClass().getName() + " selected");
            }
            else
            {
                getSite().getSelectionProvider().setSelection(new TabSelection(this.chartTab));
                logger.debug("nothing selected");
            }
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
