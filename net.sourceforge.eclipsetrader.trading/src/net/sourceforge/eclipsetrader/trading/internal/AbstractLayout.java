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

package net.sourceforge.eclipsetrader.trading.internal;

import java.util.Observable;

import net.sourceforge.eclipsetrader.core.db.WatchlistItem;
import net.sourceforge.eclipsetrader.trading.views.WatchlistView;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;


public abstract class AbstractLayout
{
    private WatchlistView view;

    public AbstractLayout(WatchlistView view)
    {
        this.view = view;
    }
    
    public WatchlistView getView()
    {
        return view;
    }

    public abstract Composite createPartControl(Composite parent);
    
    public abstract void dispose();
    
    public abstract void updateView();

    public abstract void update(Observable o, Object arg);
    
    public abstract void itemAdded(Object o);
    
    public abstract void itemRemoved(Object o);

    public abstract WatchlistItem[] getSelection();
    
    public void tickAlert(WatchlistItem watchlistItem)
    {
    }
    
    public IViewSite getViewSite()
    {
        return view.getViewSite();
    }
}
