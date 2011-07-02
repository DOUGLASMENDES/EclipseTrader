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

package org.eclipsetrader.news.internal.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.IHeadLine;

public class HeadLineLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {

    private DateFormat formatter = DateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
    private Color addedBackground;
    private Color addedForeground;
    private Font addedFont;
    private Color readedBackground;
    private Color readedForeground;
    private Font readedFont;

    public HeadLineLabelProvider() {
        addedForeground = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
        readedForeground = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        IHeadLine newsItem = (IHeadLine) element;

        switch (columnIndex) {
            case 0:
                return formatter.format(newsItem.getDate());

            case 1:
                return getSafeString(newsItem.getText());

            case 2: {
                String text = ""; //$NON-NLS-1$
                ISecurity[] o = newsItem.getMembers();
                for (int i = 0; i < o.length; i++) {
                    if (!text.equals("")) {
                        text += ", "; //$NON-NLS-1$
                    }
                    text += o[i].getName();
                }
                return text;
            }

            case 3:
                return newsItem.getSource();
        }

        return ""; //$NON-NLS-1$
    }

    protected String getSafeString(String title) {
        title = title.replaceAll("&agrave;", "à"); //$NON-NLS-1$ //$NON-NLS-2$
        title = title.replaceAll("&egrave;", "è"); //$NON-NLS-1$ //$NON-NLS-2$
        title = title.replaceAll("&eacute;", "é"); //$NON-NLS-1$ //$NON-NLS-2$
        title = title.replaceAll("&igrave;", "ì"); //$NON-NLS-1$ //$NON-NLS-2$
        title = title.replaceAll("&ograve;", "ò"); //$NON-NLS-1$ //$NON-NLS-2$
        title = title.replaceAll("&pgrave;", "ù"); //$NON-NLS-1$ //$NON-NLS-2$
        return title;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    @Override
    public Color getBackground(Object element) {
        IHeadLine newsItem = (IHeadLine) element;

        if (newsItem.isReaded()) {
            return readedBackground;
        }
        else if (newsItem.isRecent()) {
            return addedBackground;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object element) {
        IHeadLine newsItem = (IHeadLine) element;

        if (newsItem.isReaded()) {
            return readedForeground;
        }
        else if (newsItem.isRecent()) {
            return addedForeground;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    @Override
    public Font getFont(Object element) {
        IHeadLine newsItem = (IHeadLine) element;

        if (newsItem.isReaded()) {
            return readedFont;
        }
        else if (newsItem.isRecent()) {
            return addedFont;
        }

        return null;
    }

    public void setAddedBackground(Color addedBackground) {
        this.addedBackground = addedBackground;
    }

    public void setAddedFont(Font addedFont) {
        this.addedFont = addedFont;
    }

    public void setAddedForeground(Color addedForeground) {
        this.addedForeground = addedForeground;
    }

    public void setReadedBackground(Color readedBackground) {
        this.readedBackground = readedBackground;
    }

    public void setReadedFont(Font readedFont) {
        this.readedFont = readedFont;
    }

    public void setReadedForeground(Color readedForeground) {
        this.readedForeground = readedForeground;
    }
}
