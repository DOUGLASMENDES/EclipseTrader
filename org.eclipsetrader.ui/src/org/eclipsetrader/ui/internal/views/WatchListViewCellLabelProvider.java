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

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class WatchListViewCellLabelProvider extends ObservableMapOwnerDrawCellLabelProvider {

    static final int LINE_WIDTH = 2;

    private ColumnViewer viewer;
    private ViewerColumn column;

    private Map<Object, Object> valueMap = new HashMap<Object, Object>();
    private Map<Object, Object> decoratorMap = new HashMap<Object, Object>();

    private boolean ownerDrawEnabled;
    private boolean win32 = Platform.isRunning() && Platform.WS_WIN32.equals(Platform.getWS());

    public WatchListViewCellLabelProvider(IObservableMap attributeMap) {
        super(attributeMap);
    }

    public WatchListViewCellLabelProvider(IObservableMap[] attributeMaps) {
        super(attributeMaps);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    protected void initialize(ColumnViewer viewer, ViewerColumn column) {
        Assert.isTrue(this.viewer == null && this.column == null, "Label provider instance already in use"); //$NON-NLS-1$
        this.viewer = viewer;
        this.column = column;
        initialize(viewer, column, ownerDrawEnabled);
    }

    public void setOwnerDrawEnabled(boolean enabled) {
        this.ownerDrawEnabled = enabled;
        if (viewer != null && column != null) {
            setOwnerDrawEnabled(viewer, column, enabled);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void measure(Event event, Object element) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#erase(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void erase(Event event, Object element) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void paint(Event event, Object element) {
        if (attributeMaps.length == 1 || !ownerDrawEnabled) {
            return;
        }

        WatchListViewCellAttribute attribute = (WatchListViewCellAttribute) attributeMaps[1].get(element);
        if (attribute == null) {
            return;
        }

        Table table = (Table) event.widget;
        int width = table.getColumn(event.index).getWidth();

        int rowIndex = table.indexOf((TableItem) event.item);
        Color color = (rowIndex & 1) != 0 ? attribute.oddBackground : attribute.evenBackground;

        event.gc.setLineWidth(LINE_WIDTH);
        if (color != null && !color.isDisposed()) {
            event.gc.setForeground(color);
        }
        if (win32) {
            event.gc.drawRectangle(event.x + 1, event.y + 1, width - LINE_WIDTH, event.height - LINE_WIDTH);
        }
        else {
            event.gc.drawRectangle(event.x, event.y + 1, width - LINE_WIDTH - 1, event.height - LINE_WIDTH - 1);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell) {
        WatchListViewItem element = (WatchListViewItem) cell.getElement();

        IAdaptable adaptableValue = (IAdaptable) attributeMaps[0].get(element);
        WatchListViewCellAttribute attribute = (WatchListViewCellAttribute) attributeMaps[1].get(element);
        if (adaptableValue == null) {
            return;
        }

        if (!objectEquals(adaptableValue, valueMap.get(element))) {
            String text = (String) adaptableValue.getAdapter(String.class);
            if (text == null) {
                text = ""; //$NON-NLS-1$
            }
            if (!text.equals(cell.getText())) {
                cell.setText(text);
            }

            cell.setForeground((Color) adaptableValue.getAdapter(Color.class));
            cell.setFont((Font) adaptableValue.getAdapter(Font.class));

            ImageData imageData = (ImageData) adaptableValue.getAdapter(ImageData.class);
            if (imageData != null) {
                imageData.transparentPixel = imageData.palette.getPixel(new RGB(255, 255, 255));
                Image newImage = new Image(Display.getDefault(), imageData);
                Image oldImage = cell.getImage();
                cell.setImage(newImage);
                if (oldImage != null) {
                    oldImage.dispose();
                }
            }
            else {
                Image image = (Image) adaptableValue.getAdapter(Image.class);
                cell.setImage(image != null && image.isDisposed() ? null : image);
            }
            valueMap.put(element, adaptableValue);
        }

        if (!objectEquals(attribute, decoratorMap.get(element))) {
            if (ownerDrawEnabled) {
                cell.setBackground(null);
                Rectangle rect = cell.getBounds();
                cell.getControl().redraw(rect.x, rect.y, rect.width, rect.height, false);
            }
            else {
                if (attribute == null) {
                    cell.setBackground(null);
                }
                else {
                    TableItem tableItem = (TableItem) cell.getViewerRow().getItem();
                    int rowIndex = tableItem.getParent().indexOf(tableItem);
                    if ((rowIndex & 1) != 0) {
                        if (attribute.oddBackground == null || !attribute.oddBackground.isDisposed()) {
                            cell.setBackground(attribute.oddBackground);
                        }
                    }
                    else {
                        if (attribute.evenBackground == null || !attribute.evenBackground.isDisposed()) {
                            cell.setBackground(attribute.evenBackground);
                        }
                    }
                }
            }
            decoratorMap.put(element, attribute);
        }
    }

    boolean objectEquals(Object oldValue, Object newValue) {
        if (oldValue == newValue) {
            return true;
        }
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return true;
        }
        return false;
    }
}
