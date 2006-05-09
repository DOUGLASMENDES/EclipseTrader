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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.dialogs.SearchPageSelectionDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class PatternSearchView extends ViewPart implements SelectionListener
{
    public static final String VIEW_ID = "net.sourceforge.eclipsetrader.patternSearch";
    private Table table;
    private Color evenForeground = new Color(null, 0, 0, 0);
    private Color evenBackground = new Color(null, 255, 255, 255);
    private Color oddForeground = new Color(null, 0, 0, 0);
    private Color oddBackground = new Color(null, 240, 240, 240);
    private Color negativeForeground = new Color(null, 240, 0, 0);
    private Color positiveForeground = new Color(null, 0, 192, 0);
    private List pages = new ArrayList();
    private IPatternSearchPage currentPage;
    private Action historyMenu;
    private Action refreshAction;
    private Action removeCurrentAction;
    private Action removeAllAction;
    private IMenuCreator menuCreator = new IMenuCreator() {
        private Menu menu;

        public void dispose()
        {
        }

        public Menu getMenu(Control parent)
        {
            if (menu != null)
                menu.dispose();
            menu = new Menu(parent);

            for (Iterator iter = pages.iterator(); iter.hasNext(); )
            {
                IPatternSearchPage page = (IPatternSearchPage) iter.next();
                MenuItem item = new MenuItem(menu, SWT.CHECK);
                item.setText(page.getShortDescription());
                item.setData(page);
                item.addSelectionListener(PatternSearchView.this);
                if (page == currentPage)
                    item.setSelection(true);
            }

            return menu;
        }

        public Menu getMenu(Menu parent)
        {
            return menu;
        }
    };
    
    public PatternSearchView()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException
    {
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator("begin")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group1")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group2")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group3")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group4")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group5")); //$NON-NLS-1$
        toolBarManager.add(new Separator("group6")); //$NON-NLS-1$
        toolBarManager.add(new Separator("additions")); //$NON-NLS-1$
        toolBarManager.add(new Separator("end")); //$NON-NLS-1$

        removeCurrentAction = new Action() {
            public void run()
            {
                if (currentPage != null)
                {
                    int index = pages.indexOf(currentPage);
                    pages.remove(currentPage);
                    if (index > 0)
                        index--;
                    else
                        index = 0;
                    currentPage = pages.size() > 0 ? (IPatternSearchPage) pages.get(index) : null;
                    updateView();
                }
            }
        };
        removeCurrentAction.setToolTipText("Remove Current Search");
        removeCurrentAction.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/elcl16/search_rem.gif"));
        removeCurrentAction.setDisabledImageDescriptor(TradingPlugin.getImageDescriptor("icons/dlcl16/search_rem.gif"));
        removeCurrentAction.setEnabled(false);
        toolBarManager.appendToGroup("group5", removeCurrentAction);
        removeAllAction = new Action() {
            public void run()
            {
                pages.clear();
                currentPage = null;
                updateView();
            }
        };
        removeAllAction.setToolTipText("Remove All Searches");
        removeAllAction.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/elcl16/search_remall.gif"));
        removeAllAction.setDisabledImageDescriptor(TradingPlugin.getImageDescriptor("icons/dlcl16/search_remall.gif"));
        removeAllAction.setEnabled(false);
        toolBarManager.appendToGroup("group5", removeAllAction);
        refreshAction = new Action() {
            public void run()
            {
                if (currentPage != null)
                {
                    Job job = new Job("Search pattern") {
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            currentPage.run(monitor);
                            table.getDisplay().asyncExec(new Runnable() {
                                public void run()
                                {
                                    updateView();
                                }
                            });
                            return Status.OK_STATUS;
                        }
                    };
                    job.setUser(true);
                    job.schedule();
                }
            }
        };
        refreshAction.setToolTipText("Search Again");
        refreshAction.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/etool16/refresh.gif"));
        refreshAction.setDisabledImageDescriptor(TradingPlugin.getImageDescriptor("icons/dtool16/refresh.gif"));
        refreshAction.setEnabled(false);
        toolBarManager.appendToGroup("end", refreshAction);
        historyMenu = new Action("History", Action.AS_DROP_DOWN_MENU) {
            public void run()
            {
                SearchPageSelectionDialog dlg = new SearchPageSelectionDialog(getViewSite().getShell(), pages, currentPage);
                if (dlg.open() == SearchPageSelectionDialog.OK)
                {
                    currentPage = dlg.getSelectedPage();
                    updateView();
                }
            }
        };
        historyMenu.setImageDescriptor(TradingPlugin.getImageDescriptor("icons/elcl16/search_history.gif"));
        historyMenu.setDisabledImageDescriptor(TradingPlugin.getImageDescriptor("icons/dlcl16/search_history.gif"));
        historyMenu.setToolTipText("Search History");
        historyMenu.setMenuCreator(menuCreator);
        toolBarManager.appendToGroup("end", historyMenu);
        
        site.getActionBars().updateActionBars();
        
        super.init(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        
        table = new Table(content, SWT.SINGLE|SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(0);
        column.setResizable(false);

        column = new TableColumn(table, SWT.LEFT);
        column.setText("Code");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Name");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Date");
        column = new TableColumn(table, SWT.RIGHT);
        column.setText("Price");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Pattern");
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Bullish / Bearish");
        
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();

        setContentDescription(" ");
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
    }
    
    public void addPage(final IPatternSearchPage page)
    {
        Job job = new Job("Search pattern") {
            protected IStatus run(IProgressMonitor monitor)
            {
                page.run(monitor);
                pages.add(page);
                currentPage = page;
                table.getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        updateView();
                    }
                });
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }
    
    private void updateView()
    {
        table.setRedraw(false);
        table.removeAll();

        if (currentPage != null)
        {
            setContentDescription(currentPage.getDescription());

            int index = 0;
            for (Iterator iter = currentPage.getResults().iterator(); iter.hasNext(); index++)
            {
                PatternSearchItem item = (PatternSearchItem) iter.next();
                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setBackground((index & 1) == 0 ? evenBackground : oddBackground);
                tableItem.setForeground((index & 1) == 0 ? evenForeground : oddForeground);
                tableItem.setText(1, item.getCode());
                tableItem.setText(2, item.getDescription());
                tableItem.setText(3, item.getDate() != null ? item.getDate() : "");
                tableItem.setText(4, item.getPrice() != null ? item.getPrice() : "");
                tableItem.setText(5, item.getPattern());
                tableItem.setText(6, item.getOpportunity());
                tableItem.setForeground(6, item.getOpportunity().equals("Bullish") ? positiveForeground : negativeForeground);
            }
        }
        else
            setContentDescription(" ");

        table.setRedraw(true);
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumn(i).pack();

        removeCurrentAction.setEnabled(currentPage != null);
        removeAllAction.setEnabled(currentPage != null);
        refreshAction.setEnabled(currentPage != null);
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
        currentPage = (IPatternSearchPage) e.widget.getData();;
        updateView();
    }
}
