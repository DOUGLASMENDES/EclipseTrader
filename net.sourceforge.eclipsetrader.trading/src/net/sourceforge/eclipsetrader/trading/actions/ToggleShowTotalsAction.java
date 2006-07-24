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

import net.sourceforge.eclipsetrader.trading.internal.WatchlistTableViewer;

import org.eclipse.jface.action.Action;

public class ToggleShowTotalsAction extends Action
{
    private WatchlistTableViewer view;
    
    public ToggleShowTotalsAction(WatchlistTableViewer view)
    {
        super("Show Totals", AS_CHECK_BOX);
        this.view = view;
        setId("toggleShowTotalsAction");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        boolean value = view.isShowTotals();
        view.setShowTotals(!value);
        view.updateView();
    }
}
