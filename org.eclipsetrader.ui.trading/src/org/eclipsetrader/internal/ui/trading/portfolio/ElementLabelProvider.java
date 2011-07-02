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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class ElementLabelProvider extends CellLabelProvider {

    Font font;

    public ElementLabelProvider() {
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
        cell.setText(cell.getElement().toString());

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
