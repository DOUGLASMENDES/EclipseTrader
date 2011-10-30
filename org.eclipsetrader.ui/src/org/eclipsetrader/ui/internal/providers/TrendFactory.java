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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;

public class TrendFactory extends AbstractProviderFactory {

    private Image stable;
    private Image up;
    private Image down;

    public class DataProvider implements IDataProvider {

        private Map<IAdaptable, List<Double>> values = new HashMap<IAdaptable, List<Double>>();

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
            return TrendFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            ITrade trade = (ITrade) adaptable.getAdapter(ITrade.class);
            if (trade != null && trade.getPrice() != null && trade.getTime() != null) {
                List<Double> oldTrades = values.get(adaptable);
                if (oldTrades == null) {
                    oldTrades = new ArrayList<Double>();
                    values.put(adaptable, oldTrades);
                }

                Double oldTrade = oldTrades.size() != 0 ? oldTrades.get(oldTrades.size() - 1) : null;
                if (oldTrade == null || !oldTrade.equals(trade.getPrice())) {
                    oldTrades.add(trade.getPrice());
                    if (oldTrades.size() > 5) {
                        oldTrades.remove(0);
                    }
                }

                double direction = oldTrades.size() > 1 ? getSlope(oldTrades.toArray(new Double[oldTrades.size()])) : 0.0;
                Image value = direction == 0 ? stable : direction < 0 ? down : up;
                return new ImageValue(value);
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

    public static class ImageValue implements IAdaptable {

        private final Image value;

        public ImageValue(Image value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IAdaptable)) {
                return false;
            }
            Image s = (Image) ((IAdaptable) obj).getAdapter(Image.class);
            return s == value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(Image.class)) {
                return value;
            }
            return null;
        }
    }

    public TrendFactory() {
        ImageRegistry registry = UIActivator.getDefault().getImageRegistry();
        stable = registry.get(UIConstants.TREND_STABLE_ICON);
        up = registry.get(UIConstants.TREND_UP_ICON);
        down = registry.get(UIConstants.TREND_DOWN_ICON);
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
            Image.class,
        };
    }

    protected int compareValues(Number o1, Number o2) {
        if (o1 != null && o2 != null) {
            if (o1.doubleValue() < o2.doubleValue()) {
                return -1;
            }
            if (o1.doubleValue() > o2.doubleValue()) {
                return 1;
            }
            return 0;
        }

        return 0;
    }

    protected double getSlope(Double[] values) {
        int numberPlotPoints = 0;
        double sumxx = 0;
        double sumxy = 0;
        double sumx = 0;
        double sumy = 0;
        for (int i = 0; i < values.length; i++, numberPlotPoints++) {
            double x = numberPlotPoints;
            double y = values[i];
            sumx += x;
            sumy += y;
            sumxx += x * x;
            sumxy += x * y;
        }
        double n = numberPlotPoints;
        double Sxx = sumxx - sumx * sumx / n;
        double Sxy = sumxy - sumx * sumy / n;
        double b = Sxy / Sxx;
        return b;
    }
}
