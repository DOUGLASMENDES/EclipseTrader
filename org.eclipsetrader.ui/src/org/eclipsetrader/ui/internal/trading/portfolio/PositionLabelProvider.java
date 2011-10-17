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

package org.eclipsetrader.ui.internal.trading.portfolio;

import java.text.NumberFormat;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipsetrader.core.trading.IPosition;

public class PositionLabelProvider extends CellLabelProvider {

    private NumberFormat formatter = NumberFormat.getInstance();

    public PositionLabelProvider() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        if (!(cell.getElement() instanceof PositionElement)) {
            return;
        }

        PositionElement element = (PositionElement) cell.getElement();

        IPosition position = (IPosition) element.getAdapter(IPosition.class);
        if (position == null) {
            return;
        }

        cell.setText(formatter.format(position.getQuantity()));
    }
}
