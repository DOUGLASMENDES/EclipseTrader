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

package org.eclipsetrader.core.ats.engines;

import java.util.List;

import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BackfillFunction extends ScriptableObject {

    private static final long serialVersionUID = 7916839001932000909L;

    public BackfillFunction() {
    }

    @SuppressWarnings("unchecked")
    public static Object jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) throws Exception {
        List<IBar> bars = (List<IBar>) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngineInstrument.PROPERTY_BARS);
        TimeSpan[] timeSpan = (TimeSpan[]) ScriptableObject.getProperty(getTopLevelScope(ctorObj), JavaScriptEngine.PROPERTY_BARSIZE);
        ISecurity instrument = (ISecurity) ScriptableObject.getProperty(getTopLevelScope(ctorObj), BaseOrderFunction.PROPERTY_INSTRUMENT);

        int backfillBars = (int) Context.toNumber(args[0]);

        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
        ServiceReference<IRepositoryService> serviceReference = context.getServiceReference(IRepositoryService.class);
        if (serviceReference != null) {
            IRepositoryService repositoryService = context.getService(serviceReference);

            IHistory history = repositoryService.getHistoryFor(instrument);
            IOHLC[] ohlc = history.getOHLC();

            for (int i = 0; i < timeSpan.length; i++) {
                if (timeSpan[i].equals(TimeSpan.days(1))) {
                    for (int index = ohlc.length - backfillBars; index < ohlc.length; index++) {
                        bars.add(new Bar(ohlc[index].getDate(), timeSpan[i], ohlc[index].getOpen(), ohlc[index].getHigh(), ohlc[index].getLow(), ohlc[index].getClose(), ohlc[index].getVolume()));
                    }
                }
                else {
                    int filled = 0;
                    for (int index = ohlc.length - 1; index >= 0 && filled < backfillBars; index--) {
                        IHistory subHistory = history.getSubset(ohlc[index].getDate(), ohlc[index].getDate(), timeSpan[i]);
                        IOHLC[] subOhlc = subHistory.getOHLC();
                        for (int ii = subOhlc.length - 1; ii >= 0 && filled < backfillBars; ii--) {
                            bars.add(0, new Bar(subOhlc[ii].getDate(), timeSpan[i], subOhlc[ii].getOpen(), subOhlc[ii].getHigh(), subOhlc[ii].getLow(), subOhlc[ii].getClose(), subOhlc[ii].getVolume()));
                            filled++;
                        }
                    }
                }
            }

            context.ungetService(serviceReference);
        }

        return new Integer(bars.size());
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    @Override
    public String getClassName() {
        return "backfill"; //$NON-NLS-1$
    }
}
