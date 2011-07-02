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

package org.eclipsetrader.internal.ui.trading.portfolio;

import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.core.views.IViewItemVisitor;

public class GainViewItemVisitor implements IViewItemVisitor {

    double purchaseValue = 0.0;
    double marketValue = 0.0;
    double value = 0.0;
    double percentage = 0.0;

    public GainViewItemVisitor() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IViewItemVisitor#visit(org.eclipsetrader.core.views.IViewItem)
     */
    @Override
    public boolean visit(IViewItem viewItem) {
        IPosition position = (IPosition) viewItem.getAdapter(IPosition.class);
        ITrade trade = (ITrade) viewItem.getAdapter(ITrade.class);

        if (position != null && trade != null && trade.getPrice() != null) {
            purchaseValue += position.getQuantity() * position.getPrice();
            marketValue += position.getQuantity() * trade.getPrice();
            value = marketValue - purchaseValue;
            percentage = value / purchaseValue * 100.0;
        }

        return true;
    }

    public double getValue() {
        return value;
    }

    public double getPercentage() {
        return percentage;
    }
}
