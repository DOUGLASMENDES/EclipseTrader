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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.internal.UIActivator;

public class CurrentPriceLineFactory implements IChartObjectFactory {

    private ISecurity security;
    private MarketPricingEnvironment pricingEnvironment;
    private CurrentPriceLine object = new CurrentPriceLine();

    private IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            if (!event.getSecurity().equals(security)) {
                return;
            }
            for (PricingDelta delta : event.getDelta()) {
                if (delta.getNewValue() instanceof ITrade) {
                    object.setTrade((ITrade) delta.getNewValue());
                }
            }
        }
    };

    public CurrentPriceLineFactory() {
        IMarketService marketService = UIActivator.getDefault().getMarketService();

        pricingEnvironment = new MarketPricingEnvironment(marketService);
        pricingEnvironment.addPricingListener(pricingListener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
     */
    @Override
    public String getId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
     */
    @Override
    public String getName() {
        return Messages.CurrentPriceLineFactory_Name;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
     */
    @Override
    public IChartObject createObject(IDataSeries source) {
        return object;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
     */
    @Override
    public IChartParameters getParameters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
     */
    @Override
    public void setParameters(IChartParameters parameters) {
    }

    public void setSecurity(ISecurity security) {
        this.security = security;
    }

    public void setEnable(boolean enable) {
        if (enable) {
            pricingEnvironment.addSecurity(security);

            ITrade trade = pricingEnvironment.getTrade(security);
            object.setTrade(trade);
        }
        else {
            pricingEnvironment.removeSecurity(security);
            object.setTrade(null);
        }
    }

    public void dispose() {
        pricingEnvironment.dispose();
    }
}
