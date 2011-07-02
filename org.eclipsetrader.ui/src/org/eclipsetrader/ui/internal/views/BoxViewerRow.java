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

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class BoxViewerRow extends ViewerRow {

    private BoxItem item;
    private int columnCount;

    public BoxViewerRow(BoxItem item, int columnCount) {
        this.item = item;
        this.columnCount = columnCount;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#clone()
     */
    @Override
    public Object clone() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getBackground(int)
     */
    @Override
    public Color getBackground(int columnIndex) {
        return item.getBackground(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getForeground(int)
     */
    @Override
    public Color getForeground(int columnIndex) {
        return item.getForeground(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getBounds()
     */
    @Override
    public Rectangle getBounds() {
        return item.getBounds();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getBounds(int)
     */
    @Override
    public Rectangle getBounds(int columnIndex) {
        return item.getBounds(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return columnCount;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getControl()
     */
    @Override
    public Control getControl() {
        return item.getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getElement()
     */
    @Override
    public Object getElement() {
        return item.getData();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getFont(int)
     */
    @Override
    public Font getFont(int columnIndex) {
        return item.getFont(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getImage(int)
     */
    @Override
    public Image getImage(int columnIndex) {
        return item.getImage(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getItem()
     */
    @Override
    public Widget getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getNeighbor(int, boolean)
     */
    @Override
    public ViewerRow getNeighbor(int direction, boolean sameLevel) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getText(int)
     */
    @Override
    public String getText(int columnIndex) {
        return item.getText(columnIndex);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#getTreePath()
     */
    @Override
    public TreePath getTreePath() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#setBackground(int, org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setBackground(int columnIndex, Color color) {
        item.setBackground(columnIndex, color);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#setFont(int, org.eclipse.swt.graphics.Font)
     */
    @Override
    public void setFont(int columnIndex, Font font) {
        item.setFont(columnIndex, font);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#setForeground(int, org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setForeground(int columnIndex, Color color) {
        item.setForeground(columnIndex, color);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#setImage(int, org.eclipse.swt.graphics.Image)
     */
    @Override
    public void setImage(int columnIndex, Image image) {
        item.setImage(columnIndex, image);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerRow#setText(int, java.lang.String)
     */
    @Override
    public void setText(int columnIndex, String text) {
        item.setText(columnIndex, text);
    }
}
