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

package org.eclipsetrader.ui.internal.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipsetrader.core.views.ISessionData;
import org.eclipsetrader.core.views.IViewItem;

public class WatchListViewItemLabelProvider extends CellLabelProvider {

    public static final String K_FADE_LEVELS = "fade_levels";
    private Color evenRowsColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    private Color oddRowsColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    private Color tickBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    private Color[] tickOddRowsFade = new Color[3];
    private Color[] tickEvenRowsFade = new Color[3];

    public WatchListViewItemLabelProvider() {
        setTickBackground(Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        for (int i = 0; i < tickEvenRowsFade.length; i++) {
            tickEvenRowsFade[i].dispose();
        }
        for (int i = 0; i < tickOddRowsFade.length; i++) {
            tickOddRowsFade[i].dispose();
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return property != null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        IViewItem element = (IViewItem) cell.getElement();
        IAdaptable[] values = element.getValues();

        if (values != null && cell.getColumnIndex() >= 0 && cell.getColumnIndex() < values.length) {
            if (values[cell.getColumnIndex()] != null) {
                String s = (String) values[cell.getColumnIndex()].getAdapter(String.class);
                if (s != null && !cell.getText().equals(s)) {
                    cell.setText(s);
                }

                Image i = (Image) values[cell.getColumnIndex()].getAdapter(Image.class);
                if (i != cell.getImage()) {
                    cell.setImage(i);
                }

                Color color = (Color) values[cell.getColumnIndex()].getAdapter(Color.class);
                if (color != null) {
                    cell.setForeground(color);
                }
            }
            else {
                if (!cell.getText().equals("")) {
                    cell.setText("");
                }
                if (null != cell.getImage()) {
                    cell.setImage(null);
                }
                cell.setForeground(null);
            }
        }
        else {
            if (!cell.getText().equals("")) {
                cell.setText("");
            }
            if (null != cell.getImage()) {
                cell.setImage(null);
            }
            cell.setForeground(null);
        }

        if (cell.getImage() == null) {
            updateBackground(cell);
        }
    }

    protected void updateBackground(ViewerCell cell) {
        if (cell.getControl() instanceof Table && cell.getItem() instanceof TableItem) {
            int rowIndex = ((Table) cell.getControl()).indexOf((TableItem) cell.getItem());
            cell.setBackground((rowIndex & 1) != 0 ? oddRowsColor : evenRowsColor);

            IViewItem element = (IViewItem) cell.getElement();
            ISessionData data = (ISessionData) element.getAdapter(ISessionData.class);
            if (data != null) {
                int[] timers = (int[]) data.getData(K_FADE_LEVELS);
                int columnIndex = cell.getColumnIndex();
                if (timers != null && columnIndex < timers.length) {
                    if (timers[columnIndex] > 0) {
                        switch (timers[columnIndex]) {
                            case 4:
                                cell.setBackground((rowIndex & 1) != 0 ? tickOddRowsFade[0] : tickEvenRowsFade[0]);
                                break;
                            case 3:
                                cell.setBackground((rowIndex & 1) != 0 ? tickOddRowsFade[1] : tickEvenRowsFade[1]);
                                break;
                            case 2:
                                cell.setBackground((rowIndex & 1) != 0 ? tickOddRowsFade[2] : tickEvenRowsFade[2]);
                                break;
                            case 1:
                                break;
                            default:
                                cell.setBackground(tickBackgroundColor);
                                break;
                        }
                    }
                }
            }
        }
        else {
            cell.setBackground(evenRowsColor);

            IViewItem element = (IViewItem) cell.getElement();
            ISessionData data = (ISessionData) element.getAdapter(ISessionData.class);
            if (data != null) {
                int[] timers = (int[]) data.getData(K_FADE_LEVELS);
                int columnIndex = cell.getColumnIndex();
                if (timers != null && columnIndex < timers.length) {
                    if (timers[columnIndex] > 0) {
                        switch (timers[columnIndex]) {
                            case 4:
                                cell.setBackground(tickEvenRowsFade[0]);
                                break;
                            case 3:
                                cell.setBackground(tickEvenRowsFade[1]);
                                break;
                            case 2:
                                cell.setBackground(tickEvenRowsFade[2]);
                                break;
                            case 1:
                                break;
                            default:
                                cell.setBackground(tickBackgroundColor);
                                break;
                        }
                    }
                }
            }
        }
    }

    public void setTickBackground(RGB color) {
        int steps = 100 / (tickEvenRowsFade.length + 1);
        for (int i = 0, ratio = 100 - steps; i < tickEvenRowsFade.length; i++, ratio -= steps) {
            RGB rgb = blend(tickBackgroundColor.getRGB(), evenRowsColor.getRGB(), ratio);
            tickEvenRowsFade[i] = new Color(Display.getDefault(), rgb);
        }

        steps = 100 / (tickOddRowsFade.length + 1);
        for (int i = 0, ratio = 100 - steps; i < tickOddRowsFade.length; i++, ratio -= steps) {
            RGB rgb = blend(tickBackgroundColor.getRGB(), oddRowsColor.getRGB(), ratio);
            tickOddRowsFade[i] = new Color(Display.getDefault(), rgb);
        }
    }

    private RGB blend(RGB c1, RGB c2, int ratio) {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    private int blend(int v1, int v2, int ratio) {
        return (ratio * v1 + (100 - ratio) * v2) / 100;
    }
}
