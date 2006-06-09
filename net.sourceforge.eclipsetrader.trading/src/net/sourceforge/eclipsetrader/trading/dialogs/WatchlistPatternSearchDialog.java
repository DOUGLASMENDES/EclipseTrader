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

package net.sourceforge.eclipsetrader.trading.dialogs;

import java.util.Date;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.trading.internal.PatternsSearchPage;
import net.sourceforge.eclipsetrader.trading.internal.WatchlistPatternsSearchPage;
import net.sourceforge.eclipsetrader.trading.views.PatternSearchView;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WatchlistPatternSearchDialog extends PatternSearchDialog
{
    protected Combo watchlists;

    public WatchlistPatternSearchDialog(Shell parentShell)
    {
        super(parentShell);
    }

    public WatchlistPatternSearchDialog(IViewPart view)
    {
        super(view);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Watchlist");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        watchlists = new Combo(content, SWT.READ_ONLY);
        watchlists.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        for (Iterator iter = CorePlugin.getRepository().allWatchlists().iterator(); iter.hasNext(); )
        {
            Watchlist list = (Watchlist)iter.next();
            watchlists.add(list.getDescription());
            watchlists.setData(list.getDescription(), list);
        }
        if (view instanceof WatchlistView)
            watchlists.setText(((WatchlistView)view).getWatchlist().getDescription());
        else
            watchlists.select(0);

        return super.createDialogArea(content);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    public void okPressed()
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        try {
            PatternSearchView view = (PatternSearchView)page.showView(PatternSearchView.VIEW_ID);
            
            Watchlist list = (Watchlist)watchlists.getData(watchlists.getText());
            int period = BarData.INTERVAL_DAILY;
            switch (this.period.getSelectionIndex())
            {
                case 0:
                    period = BarData.INTERVAL_DAILY;
                    break;
                case 1:
                    period = BarData.INTERVAL_WEEKLY;
                    break;
                case 2:
                    period = BarData.INTERVAL_MONTHLY;
                    break;
            }

            Date beginDate = null;
            Date endDate = null;
            try {
                if (begin.getText().length() != 0)
                    beginDate = dateParse.parse(begin.getText());
                if (end.getText().length() != 0)
                    endDate = dateParse.parse(end.getText());
            } catch(Exception e) {
                CorePlugin.logException(e);
            }

            PatternsSearchPage searchPage = null; 
            if (pattern.getData(pattern.getText()) != null)
            {
                IPattern plugin = CorePlugin.createPatternPlugin((String)pattern.getData(pattern.getText()));
                searchPage = new WatchlistPatternsSearchPage(list, plugin, pattern.getText(), period, beginDate, endDate, bullishOnly.getSelection());
            }
            else
                searchPage = new WatchlistPatternsSearchPage(list, period, beginDate, endDate, bullishOnly.getSelection());
            searchPage.setAllOccurrences(allOccurrences.getSelection());
            view.addPage(searchPage);

        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        super.okPressed();
    }
}
