/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.trading.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * A layout for a tree.
 * Call <code>addColumnData</code> to add columns.
 */
public class TreeLayout extends Layout
{

    /**
     * The number of extra pixels taken as horizontal trim by the table column. 
     * To ensure there are N pixels available for the content of the column,
     * assign N+COLUMN_TRIM for the column width.
     * 
     * @since 3.1
     */
    private static int COLUMN_TRIM = "carbon".equals(SWT.getPlatform()) ? 24 : 3; //$NON-NLS-1$
        
    
    /**
     * The list of column layout data (element type:
     *  <code>ColumnLayoutData</code>).
     */
    private List columns = new ArrayList();

    /**
     * Indicates whether <code>layout</code> has yet to be called.
     */
    private boolean firstTime = true;

    public TreeLayout()
    {
    }

    /**
     * Adds a new column of data to this table layout.
     *
     * @param data the column layout data
     */
    public void addColumnData(ColumnLayoutData data) {
        columns.add(data);
    }

    /* (non-Javadoc)
     * Method declared on Layout.
     */
    public Point computeSize(Composite c, int wHint, int hHint, boolean flush) {
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
            return new Point(wHint, hHint);

        Tree table = (Tree) c;
        // To avoid recursions.
        table.setLayout(null);
        // Use native layout algorithm
        Point result = table.computeSize(wHint, hHint, flush);
        table.setLayout(this);

        int width = 0;
        int size = columns.size();
        for (int i = 0; i < size; ++i) {
            ColumnLayoutData layoutData = (ColumnLayoutData) columns.get(i);
            if (layoutData instanceof ColumnPixelData) {
                ColumnPixelData col = (ColumnPixelData) layoutData;
                width += col.width;
                if (col.addTrim) {
                    width += COLUMN_TRIM;
                }
            } else if (layoutData instanceof ColumnWeightData) {
                ColumnWeightData col = (ColumnWeightData) layoutData;
                width += col.minimumWidth;
            } else {
                Assert.isTrue(false, "Unknown column layout data");//$NON-NLS-1$
            }
        }
        if (width > result.x)
            result.x = width;
        return result;
    }

    /* (non-Javadoc)
     * Method declared on Layout.
     */
    public void layout(Composite c, boolean flush) {
        // Only do initial layout.  Trying to maintain proportions when resizing is too hard,
        // causes lots of widget flicker, causes scroll bars to appear and occasionally stick around (on Windows),
        // requires hooking column resizing as well, and may not be what the user wants anyway.
        if (!firstTime)
            return;

        Tree table = (Tree) c;
        int width = table.getClientArea().width;

        // XXX: Layout is being called with an invalid value the first time
        // it is being called on Linux. This method resets the
        // Layout to null so we make sure we run it only when
        // the value is OK.
        if (width <= 1)
            return;

        TreeColumn[] tableColumns = table.getColumns();
        int size = Math.min(columns.size(), tableColumns.length);
        int[] widths = new int[size];
        int fixedWidth = 0;
        int numberOfWeightColumns = 0;
        int totalWeight = 0;

        // First calc space occupied by fixed columns
        for (int i = 0; i < size; i++) {
            ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
            if (col instanceof ColumnPixelData) {
                ColumnPixelData cpd = (ColumnPixelData) col;
                int pixels = cpd.width;
                if (cpd.addTrim) {
                    pixels += COLUMN_TRIM;
                }
                widths[i] = pixels;
                fixedWidth += pixels;
            } else if (col instanceof ColumnWeightData) {
                ColumnWeightData cw = (ColumnWeightData) col;
                numberOfWeightColumns++;
                // first time, use the weight specified by the column data, otherwise use the actual width as the weight
                // int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
                int weight = cw.weight;
                totalWeight += weight;
            } else {
                Assert.isTrue(false, "Unknown column layout data");//$NON-NLS-1$
            }
        }

        // Do we have columns that have a weight
        if (numberOfWeightColumns > 0) {
            // Now distribute the rest to the columns with weight.
            int rest = width - fixedWidth;
            int totalDistributed = 0;
            for (int i = 0; i < size; ++i) {
                ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
                if (col instanceof ColumnWeightData) {
                    ColumnWeightData cw = (ColumnWeightData) col;
                    // calculate weight as above
                    // int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
                    int weight = cw.weight;
                    int pixels = totalWeight == 0 ? 0 : weight * rest
                            / totalWeight;
                    if (pixels < cw.minimumWidth)
                        pixels = cw.minimumWidth;
                    totalDistributed += pixels;
                    widths[i] = pixels;
                }
            }

            // Distribute any remaining pixels to columns with weight.
            int diff = rest - totalDistributed;
            for (int i = 0; diff > 0; ++i) {
                if (i == size)
                    i = 0;
                ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
                if (col instanceof ColumnWeightData) {
                    ++widths[i];
                    --diff;
                }
            }
        }

        //  System.out.print("Width: " + width);
        //  int total = 0;
        //  for (int i = 0; i < widths.length; i++) {
        //      System.out.print(" " + widths[i]);
        //      total += widths[i];
        //  }
        //  System.out.println(" Total: " + total);

        firstTime = false;

        for (int i = 0; i < size; i++) {
            tableColumns[i].setWidth(widths[i]);
        }
    }
}
