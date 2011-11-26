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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBarOpen;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.CoreActivator;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.IPosition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class JavaScriptEngineInstrument {

    public static final String FUNCTION_ON_STRATEGY_START = "onStrategyStart"; //$NON-NLS-1$
    public static final String FUNCTION_ON_QUOTE = "onQuote"; //$NON-NLS-1$
    public static final String FUNCTION_ON_TRADE = "onTrade"; //$NON-NLS-1$
    public static final String FUNCTION_ON_BAR_OPEN = "onBarOpen"; //$NON-NLS-1$
    public static final String FUNCTION_ON_BAR = "onBar"; //$NON-NLS-1$
    public static final String FUNCTION_ON_POSITION_OPENED = "onPositionOpened"; //$NON-NLS-1$
    public static final String FUNCTION_ON_POSITION_CHANGED = "onPositionChanged"; //$NON-NLS-1$
    public static final String FUNCTION_ON_POSITION_CLOSED = "onPositionClosed"; //$NON-NLS-1$

    public static final String PROPERTY_QUOTE = "quote"; //$NON-NLS-1$
    public static final String PROPERTY_TRADE = "trade"; //$NON-NLS-1$
    public static final String PROPERTY_BAR = "bar"; //$NON-NLS-1$
    public static final String PROPERTY_BARS = "bars"; //$NON-NLS-1$
    public static final String PROPERTY_POSITION = "position"; //$NON-NLS-1$

    private final Scriptable scope;
    private final ISecurity instrument;
    private final IScriptStrategy strategy;
    private Function onQuote;
    private Function onTrade;
    private Function onBarOpen;
    private Function onBar;
    private Function onPositionOpened;
    private Function onPositionChanged;
    private Function onPositionClosed;

    private BarsDataSeriesFunction bars;

    private final Log log = LogFactory.getLog(getClass());

    public JavaScriptEngineInstrument(Scriptable sharedScope, ISecurity instrument, IScriptStrategy strategy) throws Exception {
        this.instrument = instrument;
        this.strategy = strategy;

        Context cx = Context.enter();
        try {
            scope = cx.newObject(sharedScope);
            scope.setPrototype(sharedScope);
            for (Object id : ScriptableObject.getPropertyIds(sharedScope)) {
                Object obj = ScriptableObject.getProperty(sharedScope, (String) id);
                ScriptableObject.putProperty(scope, (String) id, obj);
            }
            scope.setParentScope(null);

            ScriptableObject.defineClass(scope, BarsDataSeriesFunction.class);
            ScriptableObject.defineClass(scope, LimitOrderFunction.class);
            ScriptableObject.defineClass(scope, MarketOrderFunction.class);
            ScriptableObject.defineClass(scope, HasPositionFunction.class);
            defineClasses();

            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, Context.javaToJS(instrument, scope));

            bars = (BarsDataSeriesFunction) cx.newObject(scope, BarsDataSeriesFunction.FUNCTION_NAME);
            ScriptableObject.putProperty(scope, PROPERTY_BARS, bars);

            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.feed);", strategy.getName(), 0, null); //$NON-NLS-1$
            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.instruments);", strategy.getName(), 0, null); //$NON-NLS-1$
            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.trading);", strategy.getName(), 0, null); //$NON-NLS-1$

            for (IScript script : strategy.getIncludes()) {
                cx.evaluateString(scope, script.getText(), script.getName(), 1, null);
            }
            cx.evaluateString(scope, strategy.getText(), strategy.getName(), 1, null);

            Object obj = scope.get(FUNCTION_ON_QUOTE, scope);
            if (obj instanceof Function) {
                onQuote = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_TRADE, scope);
            if (obj instanceof Function) {
                onTrade = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_BAR_OPEN, scope);
            if (obj instanceof Function) {
                onBarOpen = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_BAR, scope);
            if (obj instanceof Function) {
                onBar = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_POSITION_OPENED, scope);
            if (obj instanceof Function) {
                onPositionOpened = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_POSITION_CHANGED, scope);
            if (obj instanceof Function) {
                onPositionChanged = (Function) obj;
            }

            obj = scope.get(FUNCTION_ON_POSITION_CLOSED, scope);
            if (obj instanceof Function) {
                onPositionClosed = (Function) obj;
            }
        } finally {
            Context.exit();
        }
    }

    protected void defineClasses() {
        if (!Platform.isRunning() || Platform.getExtensionRegistry() == null) {
            return;
        }
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CoreActivator.SCRIPTS_EXTENSION_ID);
        if (extensionPoint != null) {
            IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                String strID = configElements[j].getAttribute("class"); //$NON-NLS-1$
                try {
                    ScriptableObject object = (ScriptableObject) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
                    ScriptableObject.defineClass(scope, object.getClass());
                } catch (Exception e) {
                    Status status = new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, 0, "Unable to define function " + strID, e); //$NON-NLS-1$
                    CoreActivator.log(status);
                }
            }
        }
    }

    public ISecurity getInstrument() {
        return instrument;
    }

    public void onStrategyStart() {
        Context cx = Context.enter();
        try {
            Object obj = scope.get(FUNCTION_ON_STRATEGY_START, scope);
            if (obj instanceof Function) {
                ((Function) obj).call(cx, scope, scope, new Object[0]);
            }
        } finally {
            Context.exit();
        }
    }

    public void onQuote(IQuote quote) {
        Context cx = Context.enter();
        try {
            if (onQuote != null) {
                onQuote.call(cx, scope, scope, new Object[] {
                    quote
                });
            }
            ScriptableObject.putProperty(scope, PROPERTY_QUOTE, quote);
        } finally {
            Context.exit();
        }
    }

    public void onTrade(ITrade trade) {
        Context cx = Context.enter();
        try {
            if (onTrade != null) {
                onTrade.call(cx, scope, scope, new Object[] {
                    trade
                });
            }
            ScriptableObject.putProperty(scope, PROPERTY_TRADE, trade);
        } finally {
            Context.exit();
        }
    }

    public void onBarOpen(IBarOpen bar) {
        Context cx = Context.enter();
        try {
            if (onBarOpen != null) {
                onBarOpen.call(cx, scope, scope, new Object[] {
                    bar
                });
            }
        } finally {
            Context.exit();
        }
    }

    public void onBar(IBar bar) {
        Context cx = Context.enter();
        try {
            bars.append(bar);
            if (onBar != null) {
                onBar.call(cx, scope, scope, new Object[] {
                    bar
                });
            }
            ScriptableObject.putProperty(scope, PROPERTY_BAR, bar);
        } finally {
            Context.exit();
        }
    }

    public void onPositionOpen(IPosition position) {
        log.info("onPositionOpen: " + position);
        Context cx = Context.enter();
        try {
            ScriptableObject.putProperty(scope, PROPERTY_POSITION, position);
            if (onPositionOpened != null) {
                onPositionOpened.call(cx, scope, scope, new Object[] {
                    position
                });
            }
        } finally {
            Context.exit();
        }
    }

    public void onPositionChange(IPosition position) {
        log.info("onPositionChange: " + position);
        Context cx = Context.enter();
        try {
            ScriptableObject.putProperty(scope, PROPERTY_POSITION, position);
            if (onPositionChanged != null) {
                onPositionChanged.call(cx, scope, scope, new Object[] {
                    position
                });
            }
        } finally {
            Context.exit();
        }
    }

    public void onPositionClosed(IPosition position) {
        log.info("onPositionClosed: " + position);
        Context cx = Context.enter();
        try {
            ScriptableObject.deleteProperty(scope, PROPERTY_POSITION);
            if (onPositionClosed != null) {
                onPositionClosed.call(cx, scope, scope, new Object[] {
                    position
                });
            }
        } finally {
            Context.exit();
        }
    }

    public void setPosition(IPosition position) {
        Context.enter();
        try {
            ScriptableObject.putProperty(scope, PROPERTY_POSITION, position);
        } finally {
            Context.exit();
        }
    }

    public Object get(String name) {
        return scope.get(name, scope);
    }

    public Scriptable getScope() {
        return scope;
    }

    public IBar[] getBars() {
        return bars.toArray();
    }

    public void backfill(int backfillBars) {
        BundleContext context = CoreActivator.getDefault().getBundle().getBundleContext();
        ServiceReference<IRepositoryService> serviceReference = context.getServiceReference(IRepositoryService.class);
        if (serviceReference != null) {
            IRepositoryService repositoryService = context.getService(serviceReference);

            IHistory history = repositoryService.getHistoryFor(instrument);
            IOHLC[] ohlc = history.getOHLC();

            TimeSpan[] timeSpan = strategy.getBarsTimeSpan();
            for (int i = 0; i < timeSpan.length; i++) {
                if (timeSpan[i].equals(TimeSpan.days(1))) {
                    for (int index = ohlc.length - backfillBars; index < ohlc.length; index++) {
                        bars.append(new Bar(ohlc[index].getDate(), timeSpan[i], ohlc[index].getOpen(), ohlc[index].getHigh(), ohlc[index].getLow(), ohlc[index].getClose(), ohlc[index].getVolume()));
                    }
                }
                else {
                    int filled = 0;
                    for (int index = ohlc.length - 1; index >= 0 && filled < backfillBars; index--) {
                        IHistory subHistory = history.getSubset(ohlc[index].getDate(), ohlc[index].getDate(), timeSpan[i]);
                        IOHLC[] subOhlc = subHistory.getOHLC();
                        for (int ii = subOhlc.length - 1; ii >= 0 && filled < backfillBars; ii--) {
                            bars.prepend(new Bar(subOhlc[ii].getDate(), timeSpan[i], subOhlc[ii].getOpen(), subOhlc[ii].getHigh(), subOhlc[ii].getLow(), subOhlc[ii].getClose(), subOhlc[ii].getVolume()));
                            filled++;
                        }
                    }
                }
            }

            context.ungetService(serviceReference);
        }
    }
}
