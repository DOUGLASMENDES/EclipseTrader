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

package net.sourceforge.eclipsetrader.trading.views;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.FeedMonitor;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Alert;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.transfers.WatchlistItemTransfer;
import net.sourceforge.eclipsetrader.core.ui.SelectionProvider;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.actions.SetRibbonLayoutAction;
import net.sourceforge.eclipsetrader.trading.actions.SetTableLayoutAction;
import net.sourceforge.eclipsetrader.trading.internal.AbstractLayout;
import net.sourceforge.eclipsetrader.trading.internal.CopyAction;
import net.sourceforge.eclipsetrader.trading.internal.CutAction;
import net.sourceforge.eclipsetrader.trading.internal.DeleteWatchlistItemAction;
import net.sourceforge.eclipsetrader.trading.internal.PasteAction;
import net.sourceforge.eclipsetrader.trading.internal.WatchlistBoxViewer;
import net.sourceforge.eclipsetrader.trading.internal.WatchlistTableViewer;
import net.sourceforge.eclipsetrader.trading.wizards.WatchlistSettingsDialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class WatchlistView extends ViewPart implements ICollectionObserver, Observer
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.watchlist";
    public static final String PREFS_SHOW_TOTALS = "SHOW_TOTALS";
    public static final String PREFS_SORTING = "SORT";
    public static final int TABLE = 0;
    public static final int RIBBON = 1;
    Watchlist watchlist;
    PreferenceStore preferenceStore = new PreferenceStore();
    Composite parent;
    AbstractLayout layout;
    Action tableLayout = new SetTableLayoutAction(this);
    Action ribbonLayout = new SetRibbonLayoutAction(this);
    Action cutAction = new CutAction(this);
    Action copyAction = new CopyAction(this);
    Action pasteAction;
    Action deleteAction = new DeleteWatchlistItemAction(this);
    DropTargetListener dropTargetListener = new DropTargetListener() {
        public void dragEnter(DropTargetEvent event)
        {
            if (event.detail == DND.DROP_DEFAULT)
                event.detail = DND.DROP_COPY;
        
            event.currentDataType = null;
            
            TransferData[] data = event.dataTypes;
            for (int i = 0; i < data.length; i++)
            {
                if (WatchlistItemTransfer.getInstance().isSupportedType(data[i]))
                {
                    event.currentDataType = data[i];
                    break;
                }
            }
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
                itemsDropped(securities, new Point(event.x, event.y));
            }
            else if (WatchlistItemTransfer.getInstance().isSupportedType(event.currentDataType))
            {
                WatchlistItem[] items = (WatchlistItem[]) event.data;
                itemsDropped(items, new Point(event.x, event.y));
            }
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        preferenceStore = new PreferenceStore(TradingPlugin.getDefault().getStateLocation().append("watchlist." + site.getSecondaryId() + ".prefs").toOSString());
        preferenceStore.setDefault(WatchlistView.PREFS_SORTING, "watchlist.description;0");
        preferenceStore.setDefault(WatchlistView.PREFS_SHOW_TOTALS, false);
        try {
            preferenceStore.load();
        } catch(Exception e) {
        }
        
        IMenuManager menuManager = site.getActionBars().getMenuManager();
        menuManager.add(new Separator("top")); //$NON-NLS-1$
        menuManager.add(new Separator()); //$NON-NLS-1$
        IMenuManager layoutMenu = new MenuManager("Layout", "layout");
        layoutMenu.add(tableLayout);
        layoutMenu.add(ribbonLayout);
        menuManager.add(layoutMenu);
        menuManager.add(new Separator("toggles")); //$NON-NLS-1$
        menuManager.add(new Separator("search")); //$NON-NLS-1$
        menuManager.add(new Separator("additions")); //$NON-NLS-1$
        menuManager.add(new Separator("bottom")); //$NON-NLS-1$
        
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
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
        this.parent = parent;
        getSite().setSelectionProvider(new SelectionProvider());

        // Drag and drop support
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance(), WatchlistItemTransfer.getInstance() });
        target.addDropListener(dropTargetListener);
        
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler("cut", cutAction);
        actionBars.setGlobalActionHandler("copy", copyAction);
        actionBars.setGlobalActionHandler("paste", pasteAction = new PasteAction(this));
        actionBars.setGlobalActionHandler("delete", deleteAction);
        actionBars.setGlobalActionHandler("settings", new Action() {
            public void run()
            {
                WatchlistSettingsDialog dlg = new WatchlistSettingsDialog(getWatchlist(), getViewSite().getShell());
                dlg.open();
            }
        });
        
        watchlist = (Watchlist)CorePlugin.getRepository().load(Watchlist.class, new Integer(getViewSite().getSecondaryId()));
        if (watchlist.getDescription().length() != 0)
        {
            setTitleToolTip(getPartName());
            setPartName(watchlist.getDescription());
        }

        parent.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                if (watchlist != null)
                {
                    setLayout(watchlist.getStyle());
                    for (Iterator itemIter = watchlist.getItems().iterator(); itemIter.hasNext(); )
                    {
                        WatchlistItem watchlistItem = (WatchlistItem)itemIter.next();
                        for (Iterator iter = watchlistItem.getAlerts().iterator(); iter.hasNext(); )
                        {
                            Alert alert = (Alert) iter.next();
                            AlertPlugin plugin = (AlertPlugin) alert.getData();
                            if (plugin == null)
                            {
                                plugin = TradingPlugin.createAlertPlugin(alert.getPluginId());
                                plugin.init(watchlistItem.getSecurity(), alert);
                                plugin.setLastSeen(alert.getLastSeen());
                                alert.setData(plugin);
                            }
                        }
                        watchlistItem.addObserver(WatchlistView.this);
                        Security security = watchlistItem.getSecurity();
                        if (security != null && security.getQuoteFeed() != null)
                            FeedMonitor.monitor(security);
                    }
                    watchlist.getItems().addCollectionObserver(WatchlistView.this);
                    watchlist.addObserver(WatchlistView.this);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        parent.setFocus();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (watchlist != null)
        {
            watchlist.deleteObserver(this);
            watchlist.getItems().removeCollectionObserver(this);

            for (Iterator iter = watchlist.getItems().iterator(); iter.hasNext(); )
            {
                WatchlistItem watchlistItem = (WatchlistItem)iter.next();
                Security security = watchlistItem.getSecurity();
                if (security != null && security.getQuoteFeed() != null)
                    FeedMonitor.cancelMonitor(security);
            }
        }

        layout.dispose();

        try {
            preferenceStore.save();
        } catch(Exception e) {
            Logger.getLogger(getClass()).warn(e);
        }

        super.dispose();
    }
    
    public void fillMenuBars(IMenuManager mgr)
    {
        mgr.add(new Separator());
        mgr.add(cutAction);
        mgr.add(copyAction);
        mgr.add(pasteAction);
        mgr.add(new Separator());
        mgr.add(deleteAction);
    }
    
    public PreferenceStore getPreferenceStore()
    {
        return preferenceStore;
    }

    public void setLayout(int style)
    {
        if (layout != null)
            layout.dispose();

        switch(style)
        {
            case TABLE:
                layout = new WatchlistTableViewer(this);
                break;
            case RIBBON:
                layout = new WatchlistBoxViewer(this);
                break;
        }

        tableLayout.setChecked(style == TABLE);
        ribbonLayout.setChecked(style == RIBBON);
        watchlist.setStyle(style);
        CorePlugin.getRepository().save(watchlist);

        layout.createPartControl(parent);
        layout.updateView();
        parent.layout();
    }

    public AbstractLayout getLayout()
    {
        return layout;
    }

    public Watchlist getWatchlist()
    {
        return watchlist;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        if (o == watchlist)
        {
            parent.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (watchlist.getDescription().length() != 0 && !watchlist.getDescription().equals(getPartName()))
                        setPartName(watchlist.getDescription());
                    layout.updateView();
                }
            });
        }
        if (o instanceof WatchlistItem)
        {
            final WatchlistItem watchlistItem = (WatchlistItem)o;
            for (Iterator iter = watchlistItem.getAlerts().iterator(); iter.hasNext(); )
            {
                Alert alert = (Alert) iter.next();
                AlertPlugin plugin = (AlertPlugin) alert.getData();
                if (plugin == null)
                {
                    plugin = TradingPlugin.createAlertPlugin(alert.getPluginId());
                    plugin.init(watchlistItem.getSecurity(), alert);
                    plugin.setLastSeen(alert.getLastSeen());
                    alert.setData(plugin);
                }
                if (plugin != null && !plugin.isSeenToday())
                {
                    if (plugin.apply())
                    {
                        plugin.setLastSeen(Calendar.getInstance().getTime());
                        alert.setLastSeen(plugin.getLastSeen());
                        if (alert.isHilight())
                        {
                            final RGB foreground = plugin.getHilightForeground();
                            final RGB background = plugin.getHilightBackground();
                            parent.getDisplay().asyncExec(new Runnable() {
                                public void run()
                                {
                                    layout.tickAlert(watchlistItem, foreground, background);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        WatchlistItem watchlistItem = (WatchlistItem)o;

        layout.itemAdded(o);

        Security security = watchlistItem.getSecurity();
        if (security != null && security.getQuoteFeed() != null)
            FeedMonitor.monitor(security);
        
        for (Iterator iter = watchlistItem.getAlerts().iterator(); iter.hasNext(); )
        {
            Alert alert = (Alert) iter.next();
            AlertPlugin plugin = (AlertPlugin) alert.getData();
            if (plugin == null)
            {
                plugin = TradingPlugin.createAlertPlugin(alert.getPluginId());
                plugin.init(watchlistItem.getSecurity(), alert);
                plugin.setLastSeen(alert.getLastSeen());
                alert.setData(plugin);
            }
        }
        
        watchlistItem.addObserver(this);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        WatchlistItem watchlistItem = (WatchlistItem)o;

        watchlistItem.deleteObserver(this);
        layout.itemRemoved(o);

        Security security = watchlistItem.getSecurity();
        if (security != null && security.getQuoteFeed() != null)
            FeedMonitor.cancelMonitor(security);
    }

    void itemsDropped(Security[] securities, Point point)
    {
        int index = layout.getItemIndex(point);
        if (index == -1)
            index = watchlist.getItems().size();

        if (watchlist != null)
        {
            for (int i = 0; i < securities.length; i++)
            {
                WatchlistItem item = new WatchlistItem();
                item.setParent(watchlist);
                item.setSecurity(securities[i]);
                watchlist.getItems().add(index++, item);
            }
            CorePlugin.getRepository().save(watchlist);
        }
    }

    void itemsDropped(WatchlistItem[] items, Point point)
    {
        int index = layout.getItemIndex(point);
        if (index == -1)
            index = watchlist.getItems().size();

        if (watchlist != null)
        {
            for (int i = 0; i < items.length; i++)
            {
                items[i].setParent(watchlist);
                watchlist.getItems().add(index++, items[i]);
            }
            CorePlugin.getRepository().save(watchlist);
        }
    }
    
    public WatchlistItem[] getSelection()
    {
        return layout.getSelection();
    }
}
