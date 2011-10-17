/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ToolsContributionItem extends CompoundContributionItem {

    private Action lineAction;

    public ToolsContributionItem() {
        lineAction = new Action(Messages.ToolsContributionItem_Action, IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
            }
        };
        lineAction.setImageDescriptor(ChartsUIActivator.imageDescriptorFromPlugin("icons/etool16/line.gif")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
     */
    @Override
    protected IContributionItem[] getContributionItems() {
        IContributionItem[] items = new IContributionItem[] {
            new ActionContributionItem(lineAction),
        };
        return items;
    }
}
