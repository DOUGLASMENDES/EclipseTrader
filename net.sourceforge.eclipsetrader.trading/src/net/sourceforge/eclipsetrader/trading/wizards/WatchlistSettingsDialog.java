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

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.core.db.WatchlistItem;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class WatchlistSettingsDialog extends PreferenceDialog
{
    private Watchlist watchlist;
    private GeneralPage generalPage;
    private ColumnsPage columnsPage;
    private ItemsPage itemsPage;
//    private PreferenceNode alertsNode = new PreferenceNode("alerts", new AlertsNodePage());
    private PreferenceNode alertsNode;

    public WatchlistSettingsDialog(Watchlist watchlist, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.watchlist = watchlist;
        
        generalPage = new GeneralPage(watchlist) {
            public void performFinish()
            {
                WatchlistSettingsDialog.this.watchlist.setDescription(getText());
                WatchlistSettingsDialog.this.watchlist.setCurrency(getCurrency());
                super.performFinish();
            }
        };
        columnsPage = new ColumnsPage() {
            public void performFinish()
            {
                WatchlistSettingsDialog.this.watchlist.setColumns(getColumns());
                super.performFinish();
            }
        };
        itemsPage = new ItemsPage() {
            public void performFinish()
            {
                WatchlistSettingsDialog.this.watchlist.setItems(getItems());
                super.performFinish();
            }
        };

        getPreferenceManager().addToRoot(new PreferenceNode("general", new CommonDialogPage(generalPage) {
            protected Control createContents(Composite parent)
            {
                Control control = super.createContents(parent);
                generalPage.setText(WatchlistSettingsDialog.this.watchlist.getDescription());
                return control;
            }
        }));
        getPreferenceManager().addToRoot(new PreferenceNode("columns", new CommonDialogPage(columnsPage) {
            protected Control createContents(Composite parent)
            {
                Control control = super.createContents(parent);
                columnsPage.setColumns(WatchlistSettingsDialog.this.watchlist.getColumns());
                return control;
            }
        }));
        alertsNode = new PreferenceNode("items", new CommonDialogPage(itemsPage) {
            protected Control createContents(Composite parent)
            {
                Control control = super.createContents(parent);
                itemsPage.setItems(WatchlistSettingsDialog.this.watchlist.getItems());
                return control;
            }
        });
        getPreferenceManager().addToRoot(alertsNode);

        for (Iterator iter = watchlist.getItems().iterator(); iter.hasNext(); )
        {
            WatchlistItem watchlistItem = (WatchlistItem)iter.next();
            alertsNode.add(new PreferenceNode("alerts" + String.valueOf(watchlistItem.getId()), new ItemPreferencePage(watchlistItem)));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Watchlist Settings");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#okPressed()
     */
    protected void okPressed()
    {
        super.okPressed();
        CorePlugin.getRepository().save(watchlist);
    }
}
