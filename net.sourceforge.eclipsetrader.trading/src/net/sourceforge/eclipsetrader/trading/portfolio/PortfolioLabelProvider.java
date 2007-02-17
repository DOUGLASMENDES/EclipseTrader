/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.portfolio;

import java.text.NumberFormat;

import net.sourceforge.eclipsetrader.core.db.PortfolioPosition;
import net.sourceforge.eclipsetrader.core.db.feed.Quote;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class PortfolioLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, IFontProvider
{
    NumberFormat numberFormat = NumberFormat.getInstance();
    NumberFormat priceFormat = NumberFormat.getInstance();
    NumberFormat valueFormat = NumberFormat.getInstance();
    NumberFormat percentageFormat = NumberFormat.getInstance();
    Color negativeForeground;
    Color positiveForeground;
    Font boldFont;

    public PortfolioLabelProvider()
    {
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);

        priceFormat.setGroupingUsed(true);
        priceFormat.setMinimumIntegerDigits(1);
        priceFormat.setMinimumFractionDigits(4);
        priceFormat.setMaximumFractionDigits(4);

        percentageFormat.setGroupingUsed(true);
        percentageFormat.setMinimumIntegerDigits(1);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setMaximumFractionDigits(2);

        valueFormat.setGroupingUsed(true);
        valueFormat.setMinimumIntegerDigits(1);
        valueFormat.setMinimumFractionDigits(2);
        valueFormat.setMaximumFractionDigits(2);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element)
    {
        if (element instanceof AccountGroupTreeNode)
            return ((AccountGroupTreeNode)element).value.getDescription();
        if (element instanceof AccountTreeNode)
            return ((AccountTreeNode)element).value.getDescription();
        if (element instanceof PositionTreeNode)
            return ((PositionTreeNode)element).value.getSecurity().getDescription();
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        if (element instanceof AccountGroupTreeNode)
        {
            if (columnIndex == 0)
                return ((AccountGroupTreeNode)element).value.getDescription();
        }
        if (element instanceof AccountTreeNode)
        {
            if (columnIndex == 0)
                return ((AccountTreeNode)element).value.getDescription();
        }
        if (element instanceof PositionTreeNode)
        {
            PortfolioPosition position = ((PositionTreeNode)element).value; 
            Quote quote = ((PositionTreeNode)element).quote;
            switch(columnIndex)
            {
                case 0:
                    return position.getSecurity().toString();
                case 1:
                    return numberFormat.format(position.getQuantity());
                case 2:
                    return priceFormat.format(position.getPrice());
                case 3:
                    return quote == null ? "" : priceFormat.format(quote.getLast());
                case 4:
                    return quote == null ? "" : valueFormat.format(position.getMarketValue());
                case 5:
                {
                    double gain = (position.getQuantity() > 0) ? position.getMarketValue() - position.getValue() : position.getValue() - position.getMarketValue();
                    String s1 = valueFormat.format(gain);
                    String s2 = percentageFormat.format(gain / position.getValue() * 100.0);
                    if (gain > 0)
                        return "+" + s1 + " (+" + s2 + "%)";
                    else
                        return s1 + " (" + s2 + "%)";
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    public Color getBackground(Object element, int columnIndex)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    public Color getForeground(Object element, int columnIndex)
    {
        if (element instanceof PositionTreeNode)
        {
            PortfolioPosition position = ((PositionTreeNode)element).value; 
            if (columnIndex == 5)
            {
                double gain = (position.getQuantity() > 0) ? position.getMarketValue() - position.getValue() : position.getValue() - position.getMarketValue();
                if (gain > 0)
                    return positiveForeground;
                else if (gain < 0)
                    return negativeForeground;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element)
    {
        if (element instanceof AccountGroupTreeNode || element instanceof AccountTreeNode)
            return boldFont;
        return null;
    }
}
