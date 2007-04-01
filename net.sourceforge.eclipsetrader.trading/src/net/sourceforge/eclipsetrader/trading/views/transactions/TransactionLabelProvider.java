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

package net.sourceforge.eclipsetrader.trading.views.transactions;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import net.sourceforge.eclipsetrader.core.db.Transaction;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class TransactionLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider
{
    Color negativeForeground = new Color(null, 240, 0, 0);
    Color positiveForeground = new Color(null, 0, 192, 0);
    NumberFormat nf = NumberFormat.getInstance();
    NumberFormat pf = NumberFormat.getInstance();
    SimpleDateFormat df = new SimpleDateFormat(Messages.TransactionsView_DateTimeFormat);

    public TransactionLabelProvider()
    {
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        pf.setGroupingUsed(true);
        pf.setMinimumIntegerDigits(1);
        pf.setMinimumFractionDigits(4);
        pf.setMaximumFractionDigits(4);
    }
    
    public void dispose()
    {
        negativeForeground.dispose();
        positiveForeground.dispose();
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
        Transaction transaction = (Transaction)element;

        switch(columnIndex)
        {
            case 1:
                return df.format(transaction.getDate());
            case 2:
                return transaction.getSecurity().getCode();
            case 3:
                return transaction.getSecurity().getDescription();
            case 4:
                return transaction.getQuantity() >= 0 ? Messages.TransactionsView_Buy : Messages.TransactionsView_Sell;
            case 5:
                return String.valueOf(Math.abs(transaction.getQuantity()));
            case 6:
                return pf.format(transaction.getPrice());
            case 7:
                return nf.format(transaction.getExpenses());
            case 8:
                return nf.format(transaction.getAmount());
        }

        return ""; //$NON-NLS-1$
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
        Transaction transaction = (Transaction)element;
        if (columnIndex == 8)
            return transaction.getAmount() >= 0 ? positiveForeground : negativeForeground;
        return null;
    }
}
