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
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.views.IViewItem;

public class GainLabelProvider extends CellLabelProvider {

    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentageFormatter = NumberFormat.getInstance();
    private Color positiveColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private Color negativeColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    private Font font;

    public GainLabelProvider() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);

        percentageFormatter.setGroupingUsed(true);
        percentageFormatter.setMinimumIntegerDigits(1);
        percentageFormatter.setMinimumFractionDigits(2);
        percentageFormatter.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    protected void initialize(ColumnViewer viewer, ViewerColumn column) {
        if (font == null) {
            Display display = viewer.getControl().getDisplay();
            FontData[] fontData = display.getSystemFont().getFontData();
            font = new Font(display, fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD);
        }
        super.initialize(viewer, column);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        if (!(cell.getElement() instanceof IViewItem)) {
            return;
        }

        GainViewItemVisitor visitor = new GainViewItemVisitor();
        ((IViewItem) cell.getElement()).accept(visitor);

        double value = visitor.getValue();
        double percentage = visitor.getPercentage();

        cell.setText((value > 0 ? "+" : "") + formatter.format(value) + " (" + (value > 0 ? "+" : "") + percentageFormatter.format(percentage) + "%)");
        cell.setForeground(value != 0 ? value > 0 ? positiveColor : negativeColor : null);

        if (cell.getElement() instanceof BrokerElement) {
            cell.setFont(font);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        super.dispose();
    }
}
