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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IDecoratorManager;

public class WatchListColumnLabelProvider extends ColumnLabelProvider {

    private WatchListViewColumn column;

    private ILabelDecorator labelDecorator;
    private IColorDecorator colorDecorator;

    private Color evenRowsColor;
    private Color oddRowsColor;
    private Color tickBackgroundColor;
    private Color[] tickOddRowsFade;
    private Color[] tickEvenRowsFade;

    private Map<WatchListViewItem, Color> colorMap = new HashMap<WatchListViewItem, Color>();

    public WatchListColumnLabelProvider(WatchListViewColumn column, IDecoratorManager decoratorManager) {
        this.column = column;
        if (decoratorManager != null) {
            this.labelDecorator = decoratorManager.getLabelDecorator();
            if (this.labelDecorator instanceof IColorDecorator) {
                this.colorDecorator = (IColorDecorator) this.labelDecorator;
            }
        }
    }

    public void setColors(Color evenRowsColor, Color oddRowsColor, Color tickBackgroundColor, Color[] tickEvenRowsFade, Color[] tickOddRowsFade) {
        this.evenRowsColor = evenRowsColor;
        this.oddRowsColor = oddRowsColor;
        this.tickBackgroundColor = tickBackgroundColor;
        this.tickEvenRowsFade = tickEvenRowsFade;
        this.tickOddRowsFade = tickOddRowsFade;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return column.getDataProviderFactory().getId().equals(property);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        WatchListViewItem viewItem = (WatchListViewItem) cell.getElement();

        IAdaptable value = viewItem.getValue(column.getDataProviderFactory().getId());
        if (value != null) {
            String s = (String) value.getAdapter(String.class);
            if (s != null && !cell.getText().equals(s)) {
                cell.setText(s);
            }

            Image image = (Image) value.getAdapter(Image.class);
            cell.setImage(image);

            Color color = (Color) value.getAdapter(Color.class);
            cell.setForeground(color);

            Font font = (Font) value.getAdapter(Font.class);
            cell.setFont(font);
        }
        else {
            cell.setText("");
            cell.setImage(null);
            cell.setForeground(null);
            cell.setFont(null);
        }

        Color decoratedBackgroundColor = colorMap.get(viewItem);
        if (labelDecorator != null) {
            boolean ready = "*".equals(labelDecorator.decorateText("", viewItem));
            if (ready) {
                if (colorDecorator != null) {
                    decoratedBackgroundColor = colorDecorator.decorateBackground(viewItem);
                    colorMap.put(viewItem, decoratedBackgroundColor);
                }
            }
        }
        if (decoratedBackgroundColor != null) {
            cell.setBackground(decoratedBackgroundColor);
            return;
        }

        if (cell.getImage() == null) {
            updateBackground(cell);
        }
    }

    protected void updateBackground(ViewerCell cell) {
        WatchListViewItem viewItem = (WatchListViewItem) cell.getElement();
        Integer timer = viewItem.getUpdateTime(column.getDataProviderFactory().getId());

        if (cell.getControl() instanceof Table && cell.getItem() instanceof TableItem) {
            int rowIndex = ((Table) cell.getControl()).indexOf((TableItem) cell.getItem());
            cell.setBackground((rowIndex & 1) != 0 ? oddRowsColor : evenRowsColor);
            if (timer != null) {
                if (timer > 0) {
                    switch (timer) {
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
        else {
            cell.setBackground(evenRowsColor);
            if (timer > 0) {
                switch (timer) {
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
        if (labelDecorator != null) {
            labelDecorator.addListener(listener);
        }
        super.addListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
        if (labelDecorator != null) {
            labelDecorator.removeListener(listener);
        }
        super.removeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        if (labelDecorator != null) {
            labelDecorator.dispose();
        }
        super.dispose();
    }
}
