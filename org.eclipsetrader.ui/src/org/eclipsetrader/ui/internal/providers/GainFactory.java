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
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IHolding;

public class GainFactory extends AbstractProviderFactory {

    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentageFormatter = NumberFormat.getInstance();
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
            return GainFactory.this;
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
            IHolding holding = (IHolding) adaptable.getAdapter(IHolding.class);
            if (holding != null && holding.getPosition() != null && holding.getPurchasePrice() != null) {
                Double purchaseValue = holding.getPosition() * holding.getPurchasePrice();
                Double marketValue = holding.getPosition() * trade.getPrice();
                final Double value = marketValue - purchaseValue;
                final Double percentage = value / purchaseValue * 100.0;
                final Color color = value != 0 ? value > 0 ? positiveColor : negativeColor : null;
                return new IAdaptable() {

                    @Override
                    @SuppressWarnings({
                        "unchecked", "rawtypes"
                    })
                    public Object getAdapter(Class adapter) {
                        if (adapter.isAssignableFrom(String.class)) {
                            return (value > 0 ? "+" : "") + formatter.format(value) + " (" + (value > 0 ? "+" : "") + percentageFormatter.format(percentage) + "%)";
                        }
                        if (adapter.isAssignableFrom(Double.class)) {
                            return value;
                        }
                        if (adapter.isAssignableFrom(Color.class)) {
                            return color;
                        }
                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        if (!(obj instanceof IAdaptable)) {
                            return false;
                        }
                        Double s = (Double) ((IAdaptable) obj).getAdapter(Double.class);
                        return s == value || value != null && value.equals(s);
                    }
                };
            }
            final IPosition position = (IPosition) adaptable.getAdapter(IPosition.class);
            if (position != null && position.getQuantity() != null && position.getPrice() != null) {
                Double purchaseValue = position.getQuantity() * position.getPrice();
                Double marketValue = position.getQuantity() * trade.getPrice();
                final Double value = marketValue - purchaseValue;
                final Double percentage = value / purchaseValue * 100.0;
                final Color color = value != 0 ? value > 0 ? positiveColor : negativeColor : null;
                return new IAdaptable() {

                    @Override
                    @SuppressWarnings({
                        "unchecked", "rawtypes"
                    })
                    public Object getAdapter(Class adapter) {
                        if (adapter.isAssignableFrom(String.class)) {
                            return (value > 0 ? "+" : "") + formatter.format(value) + " (" + (value > 0 ? "+" : "") + percentageFormatter.format(percentage) + "%)";
                        }
                        if (adapter.isAssignableFrom(Double.class)) {
                            return value;
                        }
                        if (adapter.isAssignableFrom(Color.class)) {
                            return color;
                        }
                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        if (!(obj instanceof IAdaptable)) {
                            return false;
                        }
                        Double s = (Double) ((IAdaptable) obj).getAdapter(Double.class);
                        return s == value || value != null && value.equals(s);
                    }
                };
            }
            return new IAdaptable() {

                @Override
                @SuppressWarnings({
                    "unchecked", "rawtypes"
                })
                public Object getAdapter(Class adapter) {
                    if (adapter.isAssignableFrom(String.class)) {
                        return "";
                    }
                    if (adapter.isAssignableFrom(Double.class)) {
                        return null;
                    }
                    return null;
                }

                @Override
                public boolean equals(Object obj) {
                    if (!(obj instanceof IAdaptable)) {
                        return false;
                    }
                    Double s = (Double) ((IAdaptable) obj).getAdapter(Double.class);
                    return s == null;
                }
            };
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
        }
    }

    public GainFactory() {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);

        percentageFormatter.setGroupingUsed(true);
        percentageFormatter.setMinimumIntegerDigits(1);
        percentageFormatter.setMinimumFractionDigits(2);
        percentageFormatter.setMaximumFractionDigits(2);
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
    @SuppressWarnings("unchecked")
    public Class[] getType() {
        return new Class[] {
            Long.class, String.class,
        };
    }
}
