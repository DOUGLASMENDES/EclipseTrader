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

package org.eclipsetrader.ui.internal.ats.monitor;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class TableDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider {

    ITableLabelProvider provider;
    ILabelDecorator decorator;

    public TableDecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
        super(provider, decorator);
        this.provider = (ITableLabelProvider) provider;
        this.decorator = decorator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Image image = provider.getColumnImage(element, columnIndex);
        if (columnIndex == 0 && decorator != null) {
            Image decorated = decorator.decorateImage(image, element);
            if (decorated != null) {
                return decorated;
            }
        }
        return image;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        String text = provider.getColumnText(element, columnIndex);
        if (columnIndex == 0 && decorator != null) {
            String decorated = decorator.decorateText(text, element);
            if (decorated != null) {
                return decorated;
            }
        }
        return text;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DecoratingLabelProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object element) {
        if (element instanceof TradingSystemInstrumentItem) {
            TradingSystemInstrumentItem item = (TradingSystemInstrumentItem) element;
            if (item.getStatus() != TradingSystemInstrumentItem.STATUS_NORMAL) {
                return Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
            }
        }
        return super.getForeground(element);
    }
}
