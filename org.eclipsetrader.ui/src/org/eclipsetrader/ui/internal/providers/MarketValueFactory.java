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
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IHolding;

public class MarketValueFactory extends AbstractProviderFactory {

    private NumberFormat formatter = NumberFormat.getInstance();

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
            return MarketValueFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            final ITrade trade = (ITrade) adaptable.getAdapter(ITrade.class);
            if (trade == null || trade.getPrice() == null) {
                return null;
            }
            final IHolding holding = (IHolding) adaptable.getAdapter(IHolding.class);
            if (holding != null && holding.getPosition() != null) {
                Double value = trade.getPrice() * holding.getPosition();
                return new NumberValue(value, formatter.format(value));
            }
            final IPosition position = (IPosition) adaptable.getAdapter(IPosition.class);
            if (position != null && position.getQuantity() != null) {
                Double value = trade.getPrice() * position.getPrice();
                return new NumberValue(value, formatter.format(value));
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

    public MarketValueFactory() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
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
