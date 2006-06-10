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

package net.sourceforge.eclipsetrader.trading.wizards;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Alert;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.AlertPlugin;
import net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class WatchlistItemPropertiesDialog extends PreferenceDialog
{
    WatchlistItem watchlistItem;
    private NumberFormat priceFormatter = NumberFormat.getInstance();

    public WatchlistItemPropertiesDialog(WatchlistItem watchlistItem, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.watchlistItem = watchlistItem;

        priceFormatter.setGroupingUsed(true);
        priceFormatter.setMinimumIntegerDigits(1);
        priceFormatter.setMinimumFractionDigits(4);
        priceFormatter.setMaximumFractionDigits(4);

        getPreferenceManager().addToRoot(new PreferenceNode("general", new GeneralPage()));
        getPreferenceManager().addToRoot(new PreferenceNode("alerts", new AlertsPage()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(watchlistItem.getSecurity().getDescription() + " Properties");
    }
    
    private class GeneralPage extends PreferencePage
    {
        private Text position;
        private Text price;

        public GeneralPage()
        {
            super("");
            setTitle("General");
            setValid(false);
            noDefaultAndApplyButton();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
            
            Label label = new Label(content, SWT.NONE);
            label.setText("Position");
            label.setLayoutData(new GridData(125, SWT.DEFAULT));
            position = new Text(content, SWT.BORDER);
            position.setLayoutData(new GridData(80, SWT.DEFAULT));
            if (watchlistItem.getPosition() != null)
                position.setText(String.valueOf(watchlistItem.getPosition()));
            
            label = new Label(content, SWT.NONE);
            label.setText("Price");
            label.setLayoutData(new GridData(125, SWT.DEFAULT));
            price = new Text(content, SWT.BORDER);
            price.setLayoutData(new GridData(80, SWT.DEFAULT));
            price.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e)
                {
                    if (price.getText().length() != 0)
                        try {
                            double value = priceFormatter.parse(price.getText()).doubleValue();
                            price.setText(priceFormatter.format(value));
                        } catch(Exception e1) {
                            CorePlugin.logException(e1);
                        }
                }
            });
            if (watchlistItem.getPaidPrice() != null)
                position.setText(priceFormatter.format(watchlistItem.getPaidPrice()));
            
            setValid(true);
            
            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
            {
                if (position.getText().length() != 0)
                    watchlistItem.setPosition(Integer.parseInt(position.getText()));
                else
                    watchlistItem.setPaidPrice(null);
    
                if (price.getText().length() != 0)
                {
                    try {
                        watchlistItem.setPaidPrice(priceFormatter.parse(price.getText()).doubleValue());
                    } catch(Exception e1) {
                        CorePlugin.logException(e1);
                    }
                }
                else
                    watchlistItem.setPaidPrice(null);
            }
            
            return super.performOk();
        }
    }
    
    private class AlertsPage extends PreferencePage
    {
        private Table table;
        private Button add;
        private Button delete;
        private Button reset;
        private Composite group;
        private Button popup;
        private Button hilight;

        public AlertsPage()
        {
            super("");
            setTitle("Alerts");
            setValid(false);
            noDefaultAndApplyButton();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent)
        {
            Composite content = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            content.setLayout(gridLayout);
            content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
            
            table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
            gridData.widthHint = 250;
            gridData.heightHint = table.getItemHeight() * 5;
            table.setLayoutData(gridData);
            table.setHeaderVisible(true);
            table.setLinesVisible(false);
            table.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if (table.getSelectionIndex() != -1)
                    {
                        CTabFolder folder = (CTabFolder) group.getChildren()[table.getSelectionIndex()];
                        ((StackLayout)group.getLayout()).topControl = folder;
                        reset.setEnabled(((CTabFolder)table.getItem(table.getSelectionIndex()).getData("folder")).getData("last") != null);
                        popup.setEnabled(true);
                        popup.setSelection(((Boolean) folder.getData("popup")).booleanValue());
                        hilight.setEnabled(true);
                        hilight.setSelection(((Boolean) folder.getData("hilight")).booleanValue());
                    }
                    else
                    {
                        reset.setEnabled(false);
                        popup.setEnabled(false);
                        popup.setSelection(false);
                        hilight.setEnabled(false);
                        hilight.setSelection(false);
                    }
                    delete.setEnabled(table.getSelectionIndex() != -1);
                    group.layout();
                }
            });
            table.addMouseListener(new MouseAdapter() {
                public void mouseDown(MouseEvent e)
                {
                    if (table.getItem(new Point(e.x, e.y)) == null)
                    {
                        table.deselectAll();
                        ((StackLayout)group.getLayout()).topControl = null; 
                        group.layout();
                        delete.setEnabled(false);
                        reset.setEnabled(false);
                        popup.setEnabled(false);
                        popup.setSelection(false);
                        hilight.setEnabled(false);
                        hilight.setSelection(false);
                    }
                }
            });
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText("Description");
            column = new TableColumn(table, SWT.NONE);
            column.setText("Last");

            Composite buttons = new Composite(content, SWT.NONE);
            gridLayout = new GridLayout();
            gridLayout.marginWidth = gridLayout.marginHeight = 0;
            buttons.setLayout(gridLayout);
            buttons.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false));
            
            ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
            
            add = new Button(buttons, SWT.PUSH);
            add.setImage(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD).createImage());
            add.setToolTipText("Add new alert");
            add.setEnabled(true);
            add.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    NewAlertWizard wizard = new NewAlertWizard();
                    Alert alert = wizard.open(watchlistItem);
                    if (alert != null)
                    {
                        itemAdded(alert);
                        for (int i = 0; i < table.getColumnCount(); i++)
                            table.getColumn(i).pack();
                    }
                }
            });
            
            delete = new Button(buttons, SWT.PUSH);
            delete.setImage(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage());
            delete.setToolTipText("Delete selected alert");
            delete.setEnabled(false);
            delete.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if (table.getSelectionIndex() != -1)
                    {
                        int index = table.getSelectionIndex();
                        ((CTabFolder)table.getItem(index).getData("folder")).dispose();
                        table.getItem(index).dispose();
                    }
                    ((StackLayout)group.getLayout()).topControl = null; 
                    group.layout();
                    delete.setEnabled(false);
                }
            });

            reset = new Button(buttons, SWT.PUSH);
            reset.setImage(images.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO).createImage());
            reset.setToolTipText("Reset last seen");
            reset.setEnabled(false);
            reset.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if (table.getSelectionIndex() != -1)
                    {
                        int index = table.getSelectionIndex();
                        ((CTabFolder)table.getItem(index).getData("folder")).setData("last", null);
                        table.getItem(index).setText(1, "");
                    }
                    reset.setEnabled(false);
                }
            });
            
            group = new Composite(content, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
            group.setLayout(new StackLayout());
            
            for (Iterator iter = watchlistItem.getAlerts().iterator(); iter.hasNext(); )
            {
                Alert alert = (Alert) iter.next();
                itemAdded(alert);
            }

            popup = new Button(content, SWT.CHECK);
            popup.setText("Pop-up a message");
            popup.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
            popup.setEnabled(false);
            popup.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    CTabFolder folder = (CTabFolder) ((StackLayout)group.getLayout()).topControl;
                    folder.setData("popup", new Boolean(popup.getSelection()));
                }
            });

            hilight = new Button(content, SWT.CHECK);
            hilight.setText("Hilight watchlist row");
            hilight.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
            hilight.setEnabled(false);
            hilight.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    CTabFolder folder = (CTabFolder) ((StackLayout)group.getLayout()).topControl;
                    folder.setData("hilight", new Boolean(hilight.getSelection()));
                }
            });

            for (int i = 0; i < table.getColumnCount(); i++)
                table.getColumn(i).pack();
            
            setValid(true);

            return content;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.PreferencePage#performOk()
         */
        public boolean performOk()
        {
            if (isValid())
            {
                watchlistItem.getAlerts().clear();
                
                Control[] childs = group.getChildren();
                for (int i = 0; i < childs.length; i++)
                {
                    Map parameters = new HashMap();
                    CTabItem[] items = ((CTabFolder)childs[i]).getItems();
                    for (int p = 0; p < items.length; p++)
                    {
                        AlertPluginPreferencePage page = (AlertPluginPreferencePage)items[p].getData();
                        page.performOk();
                        parameters.putAll(page.getParameters());
                    }
                    Alert alert = (Alert)childs[i].getData();
                    alert.setParameters(parameters);
                    alert.setLastSeen((Date)((CTabFolder)table.getItem(i).getData("folder")).getData("last"));
                    alert.setPopup(((Boolean)childs[i].getData("popup")).booleanValue());
                    alert.setHilight(((Boolean)childs[i].getData("hilight")).booleanValue());
                    ((AlertPlugin)alert.getData()).init(parameters);
                    ((AlertPlugin)alert.getData()).setLastSeen(alert.getLastSeen());
                    watchlistItem.getAlerts().add(alert);
                }
            }
            
            return super.performOk();
        }
        
        private void itemAdded(Alert alert)
        {
            AlertPlugin plugin = (AlertPlugin)alert.getData();
            if (plugin == null)
            {
                plugin = TradingPlugin.createAlertPlugin(alert.getPluginId());
                plugin.init(watchlistItem.getSecurity(), alert.getParameters());
                alert.setData(plugin);
            }
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, plugin.getDescription() + " (" + TradingPlugin.getAlertPluginName(alert.getPluginId()) + ")");
            if (alert.getLastSeen() != null)
                tableItem.setText(1, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(alert.getLastSeen()));
            tableItem.setData(alert);
            
            CTabFolder folder = new CTabFolder(group, SWT.TOP|SWT.FLAT|SWT.BORDER);
            folder.setMaximizeVisible(false);
            folder.setMinimizeVisible(false);
            folder.setSimple(PlatformUI.getPreferenceStore().getBoolean("SHOW_TRADITIONAL_STYLE_TABS"));
            folder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
            folder.setData(alert);
            folder.setData("last", plugin.getLastSeen());
            folder.setData("popup", new Boolean(alert.isPopup()));
            folder.setData("hilight", new Boolean(alert.isHilight()));
            tableItem.setData("folder", folder);

            IConfigurationElement[] pages = TradingPlugin.getAlertPluginPreferencePages(alert.getPluginId());
            for (int i = 0; i < pages.length; i++)
            {
                CTabItem tabItem = new CTabItem(folder, SWT.NONE);
                Composite itemContent = new Composite(folder, SWT.NONE);
                itemContent.setLayout(new GridLayout());
                tabItem.setControl(itemContent);

                tabItem.setText(pages[i].getAttribute("name"));
                if (pages[i].getAttribute("description") != null)
                    tabItem.setToolTipText(pages[i].getAttribute("description"));
                
                try {
                    AlertPluginPreferencePage page = (AlertPluginPreferencePage)pages[i].createExecutableExtension("class");
                    page.init(watchlistItem.getSecurity(), alert.getParameters());
                    Control control = page.createContents(itemContent);
                    if (control instanceof Composite)
                        ((Composite)control).setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
                    tabItem.setData(page);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                
                folder.setSelection(0);
            }
        }
    }
}
