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
import java.text.ParseException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.Holding;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IEditableDataProvider;
import org.eclipsetrader.core.views.IHolding;
import org.eclipsetrader.ui.internal.UIActivator;

public class PositionFactory extends AbstractProviderFactory {

    private NumberFormat formatter = NumberFormat.getInstance();

    public class DataProvider implements IDataProvider, IEditableDataProvider {

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
            return PositionFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            IHolding holding = (IHolding) adaptable.getAdapter(IHolding.class);
            if (holding != null && holding.getPosition() != null) {
                Long value = holding.getPosition();
                return new NumberValue(value, formatter.format(value));
            }
            IPosition position = (IPosition) adaptable.getAdapter(IPosition.class);
            if (position != null && position.getQuantity() != null) {
                Long value = position.getQuantity();
                return new NumberValue(value, formatter.format(value));
            }
            return new NumberValue(null, "");
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IEditableDataProvider#setValue(org.eclipse.core.runtime.IAdaptable, java.lang.Object)
         */
        @Override
        public void setValue(IAdaptable adaptable, Object value) {
            Holding element = (Holding) adaptable.getAdapter(Holding.class);
            if (element != null) {
                Long l = null;
                if (value instanceof Number) {
                    l = ((Number) value).longValue();
                }
                else if (value != null) {
                    try {
                        if (!"".equals(value.toString())) {
                            l = formatter.parse(value.toString()).longValue();
                        }
                    } catch (ParseException e) {
                        UIActivator.log("Error parsing edited position value", e);
                    }
                }
                element.setPosition(l);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
        }
    }

    public PositionFactory() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
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
            Long.class, String.class,
        };
    }
}
