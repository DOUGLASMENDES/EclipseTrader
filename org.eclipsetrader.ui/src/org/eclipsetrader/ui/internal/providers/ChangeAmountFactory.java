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

package org.eclipsetrader.ui.internal.providers;

import java.text.NumberFormat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.ILastClose;
import org.eclipsetrader.core.feed.IPrice;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class ChangeAmountFactory extends AbstractProviderFactory {

    private NumberFormat formatter = NumberFormat.getInstance();
    private Color positiveColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private Color negativeColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    public class DataProvider implements IDataProvider {

        public DataProvider() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public void init(IAdaptable adaptable) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        @Override
        public IDataProviderFactory getFactory() {
            return ChangeAmountFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            ILastClose close = (ILastClose) adaptable.getAdapter(ILastClose.class);
            IPrice price = (IPrice) adaptable.getAdapter(IPrice.class);
            if (close != null && close.getPrice() != null && price != null && price.getPrice() != null) {
                Double value = price.getPrice() - close.getPrice();
                Color color = value != 0 ? value > 0 ? positiveColor : negativeColor : null;
                return new NumberValue(value, (value > 0 ? "+" : "") + formatter.format(value), color);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
        }
    }

    public ChangeAmountFactory() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
     */
    @Override
    public IDataProvider createProvider() {
        return new DataProvider();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getType() {
        return new Class[] {
            Double.class, String.class,
        };
    }
}
