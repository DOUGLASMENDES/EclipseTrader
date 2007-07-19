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

package net.sourceforge.eclipsetrader.charts.actions;

import net.sourceforge.eclipsetrader.charts.views.ChartView;

import org.eclipse.jface.action.Action;

/**
 */
public class AutoScaleAction extends Action
{
    private ChartView view;

    public AutoScaleAction(ChartView view)
    {
        super(Messages.AutoScaleAction_Text, AS_CHECK_BOX);
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        ((ChartView)view).setAutoScale(isChecked());
        ((ChartView)view).redrawView();
    }
}
