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

package net.sourceforge.eclipsetrader.trading.actions;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.ICollectionObserver;
import net.sourceforge.eclipsetrader.core.db.Watchlist;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenWatchlistAction implements IWorkbenchWindowPulldownDelegate2, SelectionListener, ICollectionObserver, Observer
{
    private static OpenWatchlistAction instance;
    private Menu menu;
    
    public static OpenWatchlistAction getInstance()
    {
        return instance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window)
    {
        instance = this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose()
    {
        CorePlugin.getRepository().allWatchlists().removeCollectionObserver(this);

        if (menu != null && !menu.isDisposed())
        {
            MenuItem[] items = menu.getItems();
            for (int i = 0; i < items.length; i++)
                ((Watchlist)items[i].getData()).deleteObserver(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent)
    {
        if (menu != null)
        {
            menu.dispose();
            CorePlugin.getRepository().allWatchlists().removeCollectionObserver(this);
        }
        menu = new Menu(parent);
        
        List list = CorePlugin.getRepository().allWatchlists();
        if (list.size() == 0)
        {
            MenuItem item = new MenuItem(menu, SWT.NONE);
            item.setText("Empty");
            item.setEnabled(false);
        }
        else
        {
            Collections.sort(list, new Comparator() {
                public int compare(Object arg0, Object arg1)
                {
                    return ((Watchlist)arg0).getDescription().compareTo(((Watchlist)arg1).getDescription());
                }
            });

            for (Iterator iter = list.iterator(); iter.hasNext() == true; )
            {
                Watchlist watchlist = (Watchlist)iter.next();
                MenuItem item = new MenuItem(menu, SWT.NONE);
                item.setText(watchlist.getDescription());
                item.setData(watchlist);
                item.addSelectionListener(this);
                watchlist.addObserver(this);
            }
        }

        CorePlugin.getRepository().allWatchlists().addCollectionObserver(this);

        return menu;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        Watchlist watchlist = (Watchlist)o;
        
        MenuItem[] items = menu.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (watchlist.equals(items[i].getData()))
            {
                if (!items[i].getText().equals(watchlist.getDescription()))
                    items[i].setText(watchlist.getDescription());
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemAdded(java.lang.Object)
     */
    public void itemAdded(Object o)
    {
        Watchlist watchlist = (Watchlist)o;
        
        MenuItem[] items = menu.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (watchlist.getDescription().compareTo(items[i].getText()) < 0)
            {
                MenuItem item = new MenuItem(menu, SWT.NONE, i);
                item.setText(watchlist.getDescription());
                item.setData(watchlist);
                item.addSelectionListener(this);
                return;
            }
        }
        
        MenuItem item = new MenuItem(menu, SWT.NONE);
        item.setText(watchlist.getDescription());
        item.setData(watchlist);
        item.addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.core.ICollectionObserver#itemRemoved(java.lang.Object)
     */
    public void itemRemoved(Object o)
    {
        Watchlist watchlist = (Watchlist)o;
        
        MenuItem[] items = menu.getItems();
        for (int i = 0; i < items.length; i++)
        {
            if (watchlist.equals(items[i].getData()))
            {
                items[i].dispose();
                watchlist.deleteObserver(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
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
        Watchlist watchlist = (Watchlist) ((MenuItem) e.getSource()).getData();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(WatchlistView.VIEW_ID, String.valueOf(watchlist.getId()), IWorkbenchPage.VIEW_ACTIVATE);
        }
        catch (PartInitException e1) {
            CorePlugin.logException(e1);
        }
    }
}
