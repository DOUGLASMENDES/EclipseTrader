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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.Level2FeedMonitor;
import net.sourceforge.eclipsetrader.core.db.Level2;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.transfers.SecurityTransfer;
import net.sourceforge.eclipsetrader.core.ui.SecuritySelection;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.actions.ToggleFollowSelectionAction;
import net.sourceforge.eclipsetrader.trading.actions.ToggleLevelColorsAction;
import net.sourceforge.eclipsetrader.trading.actions.TogglePriceGroupingAction;
import net.sourceforge.eclipsetrader.trading.internal.CellTicker;
import net.sourceforge.eclipsetrader.trading.internal.Trendbar;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class Level2View extends ViewPart implements Observer, ISelectionListener
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.trading.level2";
    /**
     * @deprecated use the instance area preference store instead
     */
    public static final String PREFERENCES_ID = "LEVEL2_PREFS_";
    public static final String PREFS_SECURITY = "security";
    public static final String PREFS_SECONDARY_ID = "secondaryId";
    public static final String PREFS_GROUP_PRICES = "groupPrices";
    public static final String PREFS_COLOR_LEVELS = "colorLevels";
    public static final String PREFS_FOLLOW_SELECTION = "followSelection";
    public static final String TRENDBAR_INDICATOR = "TRENDBAR_INDICATOR";
    public static final String LEVEL_1_BACKGROUND = "LEVEL_1_BACKGROUND";
    public static final String LEVEL_2_BACKGROUND = "LEVEL_2_BACKGROUND";
    public static final String LEVEL_3_BACKGROUND = "LEVEL_3_BACKGROUND";
    public static final String LEVEL_4_BACKGROUND = "LEVEL_4_BACKGROUND";
    public static final String LEVEL_5_BACKGROUND = "LEVEL_5_BACKGROUND";
    public static final String LEVEL_1_FOREGROUND = "LEVEL_1_FOREGROUND";
    public static final String LEVEL_2_FOREGROUND = "LEVEL_2_FOREGROUND";
    public static final String LEVEL_3_FOREGROUND = "LEVEL_3_FOREGROUND";
    public static final String LEVEL_4_FOREGROUND = "LEVEL_4_FOREGROUND";
    public static final String LEVEL_5_FOREGROUND = "LEVEL_5_FOREGROUND";
    static boolean DEFAULT_GROUP_PRICES = false;
    static boolean DEFAULT_COLOR_LEVELS = true;
    static boolean DEFAULT_FOLLOW_SELECTION = false;
    Security security;
    Composite info;
    Label time;
    Label volume;
    Label last;
    Label high;
    Label change;
    Label low;
    Trendbar trendbar;
    Table table;
    boolean groupPrices = DEFAULT_GROUP_PRICES;
    boolean colorLevels = DEFAULT_COLOR_LEVELS;
    boolean followSelection = DEFAULT_FOLLOW_SELECTION;
    private NumberFormat numberFormatter = NumberFormat.getInstance();
    private NumberFormat priceFormatter = NumberFormat.getInstance();
    private NumberFormat priceFormatter2 = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
    private Color background;
    private Color emptyBackground;
    private Color negativeForeground = new Color(null, 224, 0, 0);
    private Color positiveForeground = new Color(null, 0, 224, 0);
    private Color[] bandBackground = new Color[5];
    private Color[] bandForeground = new Color[5];
    private ITheme theme;
    PreferenceStore preferences;
    Action toggleLevelColorsAction;
    Action togglePriceGroupingAction;
    Action toggleFollowSelectionAction;
    private IPropertyChangeListener themeChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            if (event.getProperty().equals(LEVEL_1_BACKGROUND))
            {
                bandBackground[0] = theme.getColorRegistry().get(LEVEL_1_BACKGROUND);
                trendbar.setBandColor(0, bandBackground[0]);
            }
            else if (event.getProperty().equals(LEVEL_1_FOREGROUND))
                bandForeground[0] = theme.getColorRegistry().get(LEVEL_1_FOREGROUND);
            else if (event.getProperty().equals(LEVEL_2_BACKGROUND))
            {
                bandBackground[1] = theme.getColorRegistry().get(LEVEL_2_BACKGROUND);
                trendbar.setBandColor(1, bandBackground[1]);
            }
            else if (event.getProperty().equals(LEVEL_2_FOREGROUND))
                bandForeground[1] = theme.getColorRegistry().get(LEVEL_2_FOREGROUND);
            else if (event.getProperty().equals(LEVEL_3_BACKGROUND))
            {
                bandBackground[2] = theme.getColorRegistry().get(LEVEL_3_BACKGROUND);
                trendbar.setBandColor(2, bandBackground[2]);
            }
            else if (event.getProperty().equals(LEVEL_3_FOREGROUND))
                bandForeground[2] = theme.getColorRegistry().get(LEVEL_3_FOREGROUND);
            else if (event.getProperty().equals(LEVEL_4_BACKGROUND))
            {
                bandBackground[3] = theme.getColorRegistry().get(LEVEL_4_BACKGROUND);
                trendbar.setBandColor(3, bandBackground[3]);
            }
            else if (event.getProperty().equals(LEVEL_4_FOREGROUND))
                bandForeground[3] = theme.getColorRegistry().get(LEVEL_4_FOREGROUND);
            else if (event.getProperty().equals(LEVEL_5_BACKGROUND))
            {
                bandBackground[4] = theme.getColorRegistry().get(LEVEL_5_BACKGROUND);
                trendbar.setBandColor(4, bandBackground[4]);
            }
            else if (event.getProperty().equals(LEVEL_5_FOREGROUND))
                bandForeground[4] = theme.getColorRegistry().get(LEVEL_5_FOREGROUND);
            else if (event.getProperty().equals(TRENDBAR_INDICATOR))
                trendbar.setIndicatorColor(theme.getColorRegistry().get(TRENDBAR_INDICATOR));
            updateTable();
        }
    };
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
    boolean tableUpdaterScheduled = false;
    Runnable tableUpdater = new Runnable() {
        public void run()
        {
            tableUpdaterScheduled = false;
            if (!table.isDisposed())
                updateTable();
        }
    };
    boolean infoUpdaterScheduled = false;
    Runnable infoUpdater = new Runnable() {
        public void run()
        {
            infoUpdaterScheduled = false;
            if (!info.isDisposed())
                updateInfo();
        }
    };

    public Level2View()
    {
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumIntegerDigits(1);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(0);

        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setMaximumFractionDigits(4);

        priceFormatter2.setGroupingUsed(true);
        priceFormatter2.setMinimumIntegerDigits(1);
        priceFormatter2.setMinimumFractionDigits(4);
        priceFormatter2.setMaximumFractionDigits(4);

        percentFormatter.setGroupingUsed(false);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
    }
    
    public static IPath getPreferenceStoreLocation(Security security)
    {
        return TradingPlugin.getDefault().getStateLocation().append("level2." + String.valueOf(security.getId()) + ".prefs");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);

        toggleLevelColorsAction = new ToggleLevelColorsAction(this);
        togglePriceGroupingAction = new TogglePriceGroupingAction(this);
        toggleFollowSelectionAction = new ToggleFollowSelectionAction(this);

        Integer id = null;
        if (memento != null)
            id = memento.getInteger(PREFS_SECURITY);

        if (id == null)
            try {
                id = new Integer(getViewSite().getSecondaryId());
            } catch(Exception e) {
            }

        if (id != null)
            security = (Security)CorePlugin.getRepository().load(Security.class, id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento)
    {
        if (security != null)
            memento.putInteger(PREFS_SECURITY, security.getId().intValue());
        super.saveState(memento);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        DropTarget target = new DropTarget(parent, DND.DROP_COPY|DND.DROP_MOVE);
        target.setTransfer(new Transfer[] { SecurityTransfer.getInstance() });
        target.addDropListener(dropTargetListener);
        
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);

        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        theme = themeManager.getCurrentTheme();
        bandBackground[0] = theme.getColorRegistry().get(LEVEL_1_BACKGROUND);
        bandForeground[0] = theme.getColorRegistry().get(LEVEL_1_FOREGROUND);
        bandBackground[1] = theme.getColorRegistry().get(LEVEL_2_BACKGROUND);
        bandForeground[1] = theme.getColorRegistry().get(LEVEL_2_FOREGROUND);
        bandBackground[2] = theme.getColorRegistry().get(LEVEL_3_BACKGROUND);
        bandForeground[2] = theme.getColorRegistry().get(LEVEL_3_FOREGROUND);
        bandBackground[3] = theme.getColorRegistry().get(LEVEL_4_BACKGROUND);
        bandForeground[3] = theme.getColorRegistry().get(LEVEL_4_FOREGROUND);
        bandBackground[4] = theme.getColorRegistry().get(LEVEL_5_BACKGROUND);
        bandForeground[4] = theme.getColorRegistry().get(LEVEL_5_FOREGROUND);
        theme.addPropertyChangeListener(themeChangeListener);

        info = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(4, false);
        gridLayout.marginWidth = 3;
        gridLayout.marginHeight = 3;
        gridLayout.verticalSpacing = 2;
        gridLayout.horizontalSpacing = 10;
        info.setLayout(gridLayout);
        info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
        label.setText("Time"); //$NON-NLS-1$
        time = new Label(info, SWT.RIGHT);
        time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        label = new Label(info, SWT.NONE);
        label.setText("Volume"); //$NON-NLS-1$
        volume = new Label(info, SWT.RIGHT);
        volume.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        label = new Label(info, SWT.NONE);
        label.setText("Last Price"); //$NON-NLS-1$
        last = new Label(info, SWT.NONE);
        last.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        label = new Label(info, SWT.NONE);
        label.setText("High"); //$NON-NLS-1$
        high = new Label(info, SWT.NONE);
        high.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        label = new Label(info, SWT.NONE);
        label.setText("Change"); //$NON-NLS-1$
        change = new Label(info, SWT.NONE);
        change.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        label = new Label(info, SWT.NONE);
        label.setText("Low"); //$NON-NLS-1$
        low = new Label(info, SWT.NONE);
        low.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        trendbar = new Trendbar(content, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.heightHint = 16;
        trendbar.setLayoutData(gridData);
        trendbar.setBandColors(bandBackground);
        trendbar.setIndicatorColor(theme.getColorRegistry().get(TRENDBAR_INDICATOR));

        table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION|SWT.HIDE_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        background = table.getBackground();
        emptyBackground = parent.getBackground();
        table.setBackground(emptyBackground);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);
        column = new TableColumn(table, SWT.CENTER);
        column.setText("#");
        column = new TableColumn(table, SWT.CENTER);
        column.setText("Q.ty");
        column = new TableColumn(table, SWT.CENTER);
        column.setText("Bid");
        column = new TableColumn(table, SWT.CENTER);
        column.setText("Ask");
        column = new TableColumn(table, SWT.CENTER);
        column.setText("Q.ty");
        column = new TableColumn(table, SWT.CENTER);
        column.setText("#");
        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                table.setRedraw(false);
                updateColumnWidth();
                table.setRedraw(true);
            }
        });
        table.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e)
            {
                ((Table)e.widget).deselectAll();
                info.getParent().setFocus();
            }
        });
        
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(toggleLevelColorsAction);
        menuManager.add(togglePriceGroupingAction);
        menuManager.add(toggleFollowSelectionAction);
        getViewSite().getActionBars().updateActionBars();

        if (security != null)
        {
            setPartName(security.getDescription());
            loadPreferences();
            content.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    updateInfo();
                    updateTable();

                    security.deleteObserver(Level2View.this);
                    security.getQuoteMonitor().addObserver(Level2View.this);
                    security.getLevel2Monitor().addObserver(Level2View.this);
                    Level2FeedMonitor.monitor(security);
                }
            });
        }
        getSite().getPage().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        info.getParent().setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose()
    {
        if (security != null)
        {
            security.deleteObserver(this);
            security.getQuoteMonitor().deleteObserver(this);
            security.getLevel2Monitor().deleteObserver(this);
            Level2FeedMonitor.cancelMonitor(security);

            savePreferences();
        }
        
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
        
        background.dispose();
        negativeForeground.dispose();
        positiveForeground.dispose();
        emptyBackground.dispose();
        getSite().getPage().removeSelectionListener(this);
        
        super.dispose();
    }

    protected void loadPreferences()
    {
        preferences = new PreferenceStore(getPreferenceStoreLocation(security).toOSString());
        preferences.setDefault(PREFS_SECONDARY_ID, "");
        preferences.setDefault(PREFS_GROUP_PRICES, DEFAULT_GROUP_PRICES);
        preferences.setDefault(PREFS_COLOR_LEVELS, DEFAULT_COLOR_LEVELS);
        preferences.setDefault(PREFS_FOLLOW_SELECTION, DEFAULT_FOLLOW_SELECTION);

        try {
            preferences.load();
        } catch(Exception e) {
            // Reads the preferences from the old-style preference store
            String[] prefs = TradingPlugin.getDefault().getPluginPreferences().getString(PREFERENCES_ID + String.valueOf(security.getId())).split(",");
            if (prefs.length > 0)
                preferences.setValue(PREFS_GROUP_PRICES, prefs[0].equals("true"));
            if (prefs.length > 1)
                preferences.setValue(PREFS_COLOR_LEVELS, prefs[1].equals("true"));
            if (prefs.length > 2)
            	preferences.setValue(PREFS_FOLLOW_SELECTION, prefs[2].equals("true"));
            TradingPlugin.getDefault().getPluginPreferences().setValue(PREFERENCES_ID + String.valueOf(security.getId()), "");
        }                

        groupPrices = preferences.getBoolean(PREFS_GROUP_PRICES);
        colorLevels = preferences.getBoolean(PREFS_COLOR_LEVELS);
        followSelection = preferences.getBoolean(PREFS_FOLLOW_SELECTION);

        // Save the secondary id value immediately, so subsequent level2 requests for the same
        // security will open this view
        preferences.setValue(PREFS_SECONDARY_ID, getViewSite().getSecondaryId());
        try {
            preferences.save();
        } catch(Exception e) {
            Logger.getLogger(getClass()).warn(e);
        }

        toggleLevelColorsAction.setChecked(colorLevels);
        togglePriceGroupingAction.setChecked(groupPrices);
        toggleFollowSelectionAction.setChecked(followSelection);
    }

    protected void savePreferences()
    {
        preferences.setFilename(getPreferenceStoreLocation(security).toOSString());
        preferences.setValue(PREFS_GROUP_PRICES, groupPrices);
        preferences.setValue(PREFS_COLOR_LEVELS, colorLevels);
        preferences.setValue(PREFS_FOLLOW_SELECTION, followSelection);

        try {
            preferences.save();
        } catch(Exception e) {
            Logger.getLogger(getClass()).warn(e);
        }
    }
    
    public void setSecurity(Security newSecurity)
    {
        if (security == null)
        {
            security = newSecurity;
            loadPreferences();
        }
        else
        {
            security.deleteObserver(this);
            security.getQuoteMonitor().deleteObserver(this);
            security.getLevel2Monitor().deleteObserver(this);
            Level2FeedMonitor.cancelMonitor(security);

            preferences.setValue(PREFS_SECONDARY_ID, "");
            savePreferences();
            
            security = newSecurity;
            
            preferences.setValue(PREFS_SECONDARY_ID, getViewSite().getSecondaryId());
            savePreferences();
        }
        
        updateInfo();
        updateTable();

        security.addObserver(this);
        security.getQuoteMonitor().addObserver(this);
        security.getLevel2Monitor().addObserver(this);
        Level2FeedMonitor.monitor(security);
    }
    
    void updateInfo()
    {
        if (!getPartName().equals(security.getDescription()))
            setPartName(security.getDescription());

        if (security.getQuote() != null)
        {
            if (security.getQuote().getDate() != null)
                time.setText(dateFormatter.format(security.getQuote().getDate()));
            volume.setText(numberFormatter.format(security.getQuote().getVolume()));
            last.setText(priceFormatter2.format(security.getQuote().getLast()));
            if (security.getClose() != null)
            {
                double chg = (security.getQuote().getLast() - security.getClose().doubleValue()) / security.getClose().doubleValue() * 100;
                if (chg > 0)
                {
                    change.setText("+" + percentFormatter.format(chg) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
                    change.setForeground(positiveForeground);
                }
                else
                {
                    change.setText(percentFormatter.format(chg) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (chg < 0)
                        change.setForeground(negativeForeground);
                    else
                        change.setForeground(null);
                }
            }
        }
        if (security.getHigh() != null)
            high.setText(priceFormatter2.format(security.getHigh()));
        if (security.getLow() != null)
            low.setText(priceFormatter2.format(security.getLow()));
        info.layout(true, true);
    }
    
    void updateTable()
    {
        int bidLevel = -1, askLevel = -1;
        String bidValue = "", askValue = "";
        Level2TableItem tableItem = null;
        
        List bid = new ArrayList();
        if (security.getLevel2Bid() != null)
        {
            if (groupPrices)
                bid = security.getLevel2Bid().getGrouped();
            else
                bid = security.getLevel2Bid().getList();
        }
        List ask = new ArrayList();
        if (security.getLevel2Ask() != null)
        {
            if (groupPrices)
                ask = security.getLevel2Ask().getGrouped();
            else
                ask = security.getLevel2Ask().getList();
        }

        int total = Math.max(bid.size(), ask.size());
        for (int index = 0; index < total; index++)
        {
            if (index < table.getItemCount())
                tableItem = (Level2TableItem)table.getItem(index);
            else
                tableItem = new Level2TableItem(table, SWT.NONE);
            
            if (index < bid.size())
            {
                Level2.Item item = (Level2.Item)bid.get(index);
                String price = priceFormatter.format(item.price);
                String number = numberFormatter.format(item.number);
                String quantity = numberFormatter.format(item.quantity);
                tableItem.setText(1, number);
                tableItem.setText(2, quantity);
                tableItem.setText(3, price);
                
                if (!tableItem.getText(3).equals(bidValue))
                {
                    bidLevel++;
                    bidValue = tableItem.getText(3);
                }
                if (colorLevels && bidLevel < bandBackground.length)
                {
                    tableItem.setForeground(1, bandForeground[bidLevel]);
                    tableItem.setBackground(1, bandBackground[bidLevel]);
                    tableItem.setForeground(2, bandForeground[bidLevel]);
                    tableItem.setBackground(2, bandBackground[bidLevel]);
                    tableItem.setForeground(3, bandForeground[bidLevel]);
                    tableItem.setBackground(3, bandBackground[bidLevel]);
                }
                else
                {
                    tableItem.setForeground(1, null);
                    tableItem.setBackground(1, background);
                    tableItem.setForeground(2, null);
                    tableItem.setBackground(2, background);
                    tableItem.setForeground(3, null);
                    tableItem.setBackground(3, background);
                }
            }
            else
            {
                tableItem.setText(1, "");
                tableItem.setText(2, "");
                tableItem.setText(3, "");
                tableItem.setBackground(1, emptyBackground);
                tableItem.setBackground(2, emptyBackground);
                tableItem.setBackground(3, emptyBackground);
            }
            
            if (index < ask.size())
            {
                Level2.Item item = (Level2.Item)ask.get(index);
                tableItem.setText(4, priceFormatter.format(item.price));
                tableItem.setText(5, numberFormatter.format(item.quantity));
                tableItem.setText(6, numberFormatter.format(item.number));
                if (!tableItem.getText(4).equals(askValue))
                {
                    askLevel++;
                    askValue = tableItem.getText(4);
                }
                if (colorLevels && askLevel < bandBackground.length)
                {
                    tableItem.setForeground(4, bandForeground[askLevel]);
                    tableItem.setBackground(4, bandBackground[askLevel]);
                    tableItem.setForeground(5, bandForeground[askLevel]);
                    tableItem.setBackground(5, bandBackground[askLevel]);
                    tableItem.setForeground(6, bandForeground[askLevel]);
                    tableItem.setBackground(6, bandBackground[askLevel]);
                }
                else
                {
                    tableItem.setForeground(4, null);
                    tableItem.setBackground(4, background);
                    tableItem.setForeground(5, null);
                    tableItem.setBackground(5, background);
                    tableItem.setForeground(6, null);
                    tableItem.setBackground(6, background);
                }
            }
            else
            {
                tableItem.setText(4, "");
                tableItem.setText(5, "");
                tableItem.setText(6, "");
                tableItem.setBackground(4, emptyBackground);
                tableItem.setBackground(5, emptyBackground);
                tableItem.setBackground(6, emptyBackground);
            }
        }
        
        while(table.getItemCount() > total)
            table.getItem(total).dispose();
        
        if (security.getLevel2Bid() != null && security.getLevel2Ask() != null)
            trendbar.setData(security.getLevel2Bid(), security.getLevel2Ask());

        updateColumnWidth();
    }
    
    /**
     * Adapt the columns width to the extents of the contained text.
     */
    void updateColumnWidth()
    {
        GC gc = new GC(table);
        
        // Get the maximum extent of the column headers
        int c1 = Math.max(gc.textExtent(table.getColumn(1).getText()).x, gc.textExtent(table.getColumn(6).getText()).x);
        int c2 = Math.max(gc.textExtent(table.getColumn(2).getText()).x, gc.textExtent(table.getColumn(5).getText()).x);
        int c3 = Math.max(gc.textExtent(table.getColumn(3).getText()).x, gc.textExtent(table.getColumn(4).getText()).x);

        // Get the maximum extent of all items
        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++)
        {
            c1 = Math.max(c1, Math.max(gc.textExtent(items[i].getText(1)).x, gc.textExtent(items[i].getText(6)).x));
            c2 = Math.max(c2, Math.max(gc.textExtent(items[i].getText(2)).x, gc.textExtent(items[i].getText(5)).x));
            c3 = Math.max(c3, Math.max(gc.textExtent(items[i].getText(3)).x, gc.textExtent(items[i].getText(4)).x));
        }
        
        // Adds a border
        c1 += 12;
        c2 += 12;
        c3 += 12;

        gc.dispose();

        // Calculates the ratio of columns 1 and 2 based on the total width
        double columnsWidth = c1 + c2 + c3;
        double r1 = c1 / columnsWidth;
        double r2 = c2 / columnsWidth;

        // Get the pixel width based on the table size and the columns ratio
        int width = table.getClientArea().width;
        c1 = (int) ((width / 2) * r1);
        c2 = (int) ((width / 2) * r2);
        int c3a = (width / 2) - c1 - c2;
        int c3b = width - (c1 + c2 + c3a + c2 + c1);

        // Set the columns size only if necessary
        if (table.getColumn(1).getWidth() != c1)
            table.getColumn(1).setWidth(c1);
        if (table.getColumn(2).getWidth() != c2)
            table.getColumn(2).setWidth(c2);
        if (table.getColumn(3).getWidth() != c3a)
            table.getColumn(3).setWidth(c3a);
        if (table.getColumn(4).getWidth() != c3b)
            table.getColumn(4).setWidth(c3b);
        if (table.getColumn(5).getWidth() != c2)
            table.getColumn(5).setWidth(c2);
        if (table.getColumn(6).getWidth() != c1)
            table.getColumn(6).setWidth(c1);
    }
    
    public boolean getGroupPrices()
    {
        return groupPrices;
    }
    
    public void setGroupPrices(boolean groupPrices)
    {
        this.groupPrices = groupPrices;
        updateTable();
    }

    public boolean getColorLevels()
    {
        return colorLevels;
    }

    public void setColorLevels(boolean colorLevels)
    {
        this.colorLevels = colorLevels;
        updateTable();
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        if (o == security.getLevel2Monitor())
        {
            if (!tableUpdaterScheduled)
            {
                table.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!table.isDisposed())
                            table.getDisplay().timerExec(200, tableUpdater);
                    }
                });
                tableUpdaterScheduled = true;
            }
        }
        else
        {
            if (!infoUpdaterScheduled)
            {
                info.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        if (!info.isDisposed())
                            info.getDisplay().timerExec(200, infoUpdater);
                    }
                });
                infoUpdaterScheduled = true;
            }
        }
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

    public boolean isFollowSelection()
    {
        return followSelection;
    }

    public void setFollowSelection(boolean followSelection)
    {
        this.followSelection = followSelection;
    }
    
    private class Level2TableItem extends TableItem implements DisposeListener
    {
        private CellTicker ticker;

        public Level2TableItem(Table parent, int style, int index)
        {
            super(parent, style, index);
            ticker = new CellTicker(this, CellTicker.BACKGROUND|CellTicker.FOREGROUND);
            addDisposeListener(this);
        }

        public Level2TableItem(Table parent, int style)
        {
            super(parent, style);
            ticker = new CellTicker(this, CellTicker.BACKGROUND|CellTicker.FOREGROUND);
            addDisposeListener(this);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.TableItem#checkSubclass()
         */
        protected void checkSubclass()
        {
        }
        
        public CellTicker getCellTicker()
        {
            return ticker;
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        public void widgetDisposed(DisposeEvent e)
        {
            ticker.dispose();
        }
    }
}
