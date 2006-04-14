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
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.actions.ToggleLevelColorsAction;
import net.sourceforge.eclipsetrader.trading.actions.TogglePriceGroupingAction;
import net.sourceforge.eclipsetrader.trading.internal.CellTicker;
import net.sourceforge.eclipsetrader.trading.internal.Trendbar;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class Level2View extends ViewPart implements Observer
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.trading.level2";
    public static final String PREFERENCES_ID = "LEVEL2_PREFS_";
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
    private Security security;
    private Composite info;
    private Label time;
    private Label volume;
    private Label last;
    private Label high;
    private Label change;
    private Label low;
    private Trendbar trendbar;
    private Table table;
    private boolean groupPrices = false;
    private boolean colorLevels = true;
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
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
                int width = ((Table)e.widget).getClientArea().width;
                int c1 = (int) ((width / 2) * .18);
                int c2 = (int) ((width / 2) * .45);
                int c3 = (width / 2) - c1 - c2;
                ((Table)e.widget).getColumn(1).setWidth(c1);
                ((Table)e.widget).getColumn(2).setWidth(c2);
                ((Table)e.widget).getColumn(3).setWidth(c3);
                ((Table)e.widget).getColumn(4).setWidth(width - (c1 + c2) * 2 - c3);
                ((Table)e.widget).getColumn(5).setWidth(c2);
                ((Table)e.widget).getColumn(6).setWidth(c1);
            }
        });
        table.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e)
            {
                ((Table)e.widget).deselectAll();
                info.getParent().setFocus();
            }
        });
        
        security = (Security)CorePlugin.getRepository().load(Security.class, new Integer(getViewSite().getSecondaryId()));

        String defaultPrefs = String.valueOf(groupPrices) + "," + String.valueOf(colorLevels); 
        TradingPlugin.getDefault().getPluginPreferences().setDefault(PREFERENCES_ID + String.valueOf(security.getId()), defaultPrefs);
        
        String[] prefs = TradingPlugin.getDefault().getPluginPreferences().getString(PREFERENCES_ID + String.valueOf(security.getId())).split(",");
        if (prefs.length > 0)
            groupPrices = prefs[0].equals("true");
        if (prefs.length > 1)
            colorLevels = prefs[1].equals("true");
        
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(new ToggleLevelColorsAction(this));
        menuManager.add(new TogglePriceGroupingAction(this));
        getViewSite().getActionBars().updateActionBars();
        
        content.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                updateInfo();
                updateTable();
                security.getQuoteMonitor().addObserver(Level2View.this);
                security.getLevel2Monitor().addObserver(Level2View.this);
                Level2FeedMonitor.monitor(security);
            }
        });
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
            Level2FeedMonitor.cancelMonitor(security);
            security.deleteObserver(this);
            security.getQuoteMonitor().deleteObserver(this);
            security.getLevel2Monitor().deleteObserver(this);
        }
        if (theme != null)
            theme.removePropertyChangeListener(themeChangeListener);
        background.dispose();
        negativeForeground.dispose();
        positiveForeground.dispose();
        emptyBackground.dispose();
        super.dispose();
    }
    
    private void updateInfo()
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
    
    private void updateTable()
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
        
        table.setItemCount(total);
        
        if (security.getLevel2Bid() != null && security.getLevel2Ask() != null)
            trendbar.setData(security.getLevel2Bid(), security.getLevel2Ask());

        int width = table.getClientArea().width;
        int c1 = (int) ((width / 2) * .18);
        int c2 = (int) ((width / 2) * .45);
        int c3 = (width / 2) - c1 - c2;
        if (table.getColumn(1).getWidth() != c1)
            table.getColumn(1).setWidth(c1);
        if (table.getColumn(1).getWidth() != c2)
            table.getColumn(2).setWidth(c2);
        if (table.getColumn(3).getWidth() != c3)
            table.getColumn(3).setWidth(c3);
        if (table.getColumn(4).getWidth() != ((c1 + c2) * 2 - c3))
            table.getColumn(4).setWidth(width - (c1 + c2) * 2 - c3);
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
        
        String prefs = String.valueOf(groupPrices) + "," + String.valueOf(colorLevels); 
        TradingPlugin.getDefault().getPluginPreferences().setValue(PREFERENCES_ID + String.valueOf(security.getId()), prefs);

        updateTable();
    }

    public boolean getColorLevels()
    {
        return colorLevels;
    }

    public void setColorLevels(boolean colorLevels)
    {
        this.colorLevels = colorLevels;
        
        String prefs = String.valueOf(groupPrices) + "," + String.valueOf(colorLevels); 
        TradingPlugin.getDefault().getPluginPreferences().setValue(PREFERENCES_ID + String.valueOf(security.getId()), prefs);

        updateTable();
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        if (o == security.getLevel2Monitor())
        {
            info.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!info.isDisposed())
                        updateTable();
                }
            });
        }
        else
        {
            info.getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if (!info.isDisposed())
                        updateInfo();
                }
            });
        }
    }
    
    public Table getTable()
    {
        return table;
    }

    public Security getSecurity()
    {
        return security;
    }

    public Label getChange()
    {
        return change;
    }

    public Label getHigh()
    {
        return high;
    }

    public Label getLast()
    {
        return last;
    }

    public Label getLow()
    {
        return low;
    }

    public Label getTime()
    {
        return time;
    }

    public Label getVolume()
    {
        return volume;
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
