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

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.IPattern;
import net.sourceforge.eclipsetrader.core.db.BarData;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.trading.internal.AllPatternsSearchPage;
import net.sourceforge.eclipsetrader.trading.internal.SinglePatternSearchPage;
import net.sourceforge.eclipsetrader.trading.views.PatternSearchView;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class PatternSearchDialog extends Dialog
{
    private IViewPart view;
    private Combo watchlists;
    private Combo period;
    private Combo pattern;
    private Spinner bars;
    private Button bullishOnly;

    public PatternSearchDialog(Shell parentShell)
    {
        super(parentShell);
    }

    public PatternSearchDialog(IViewPart view)
    {
        super(view.getViewSite().getShell());
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Pattern Search");
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

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add("Daily");
        period.add("Weekly");
        period.add("Monthly");
        period.select(1);

        label = new Label(content, SWT.NONE);
        label.setText("Pattern");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        pattern = new Combo(content, SWT.READ_ONLY);
        pattern.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        pattern.add("All");
        for (Iterator iter = CorePlugin.getAllPatternPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            pattern.add(element.getAttribute("name"));
            pattern.setData(element.getAttribute("name"), element.getAttribute("id"));
        }
        pattern.select(0);

        label = new Label(content, SWT.NONE);
        label.setText("Bars");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        bars = new Spinner(content, SWT.BORDER);
        bars.setMinimum(1);
        bars.setMaximum(9999);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        bullishOnly = new Button(content, SWT.CHECK);
        bullishOnly.setText("Bullish Only");
        
        return super.createDialogArea(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
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

            if (pattern.getData(pattern.getText()) != null)
            {
                IPattern plugin = CorePlugin.createPatternPlugin((String)pattern.getData(pattern.getText()));
                view.addPage(new SinglePatternSearchPage(list, period, plugin, pattern.getText(), bars.getSelection(), bullishOnly.getSelection()));
            }
            else
                view.addPage(new AllPatternsSearchPage(list, period, bars.getSelection(), bullishOnly.getSelection()));

        } catch(Exception e) {
            CorePlugin.logException(e);
        }
        super.okPressed();
    }
}
