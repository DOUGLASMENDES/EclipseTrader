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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipsetrader.core.IScript;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBarOpen;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IPosition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptEngineInstrument {

    public static final String FUNCTION_ON_STRATEGY_START = "onStrategyStart";
    public static final String FUNCTION_ON_QUOTE = "onQuote";
    public static final String FUNCTION_ON_TRADE = "onTrade";
    public static final String FUNCTION_ON_BAR_OPEN = "onBarOpen";
    public static final String FUNCTION_ON_BAR = "onBar";
    public static final String FUNCTION_ON_POSITION_OPENED = "onPositionOpened";
    public static final String FUNCTION_ON_POSITION_CHANGED = "onPositionChanged";
    public static final String FUNCTION_ON_POSITION_CLOSED = "onPositionClosed";

    public static final String PROPERTY_QUOTE = "quote";
    public static final String PROPERTY_TRADE = "trade";
    public static final String PROPERTY_BAR = "bar";
    public static final String PROPERTY_BARS = "bars";
    public static final String PROPERTY_POSITION = "position";

    private final Scriptable scope;
    private final ISecurity instrument;
    private Function onQuote;
    private Function onTrade;
    private Function onBarOpen;
    private Function onBar;
    private Function onPositionOpened;
    private Function onPositionChanged;
    private Function onPositionClosed;

    private final List<IBar> bars = new ArrayList<IBar>();

    private final Log log = LogFactory.getLog(getClass());

    public JavaScriptEngineInstrument(Scriptable sharedScope, ISecurity instrument, IScriptStrategy strategy) throws Exception {
        this.instrument = instrument;

        Context cx = Context.enter();
        try {
            scope = cx.newObject(sharedScope);
            scope.setPrototype(sharedScope);
            for (Object id : ScriptableObject.getPropertyIds(sharedScope)) {
                Object obj = ScriptableObject.getProperty(sharedScope, (String) id);
                ScriptableObject.putProperty(scope, (String) id, obj);
            }
            scope.setParentScope(null);

            //ScriptableObject.defineClass(scope, BuyFunction.class);
            //ScriptableObject.defineClass(scope, SellFunction.class);
            ScriptableObject.defineClass(scope, LimitOrderFunction.class);
            ScriptableObject.defineClass(scope, MarketOrderFunction.class);
            ScriptableObject.defineClass(scope, HasPositionFunction.class);
            ScriptableObject.defineClass(scope, SMAFunction.class);
            ScriptableObject.defineClass(scope, EMAFunction.class);
            ScriptableObject.defineClass(scope, BBUFunction.class);
            ScriptableObject.defineClass(scope, BBLFunction.class);

            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_INSTRUMENT, instrument);
            ScriptableObject.putProperty(scope, PROPERTY_BARS, bars);

            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.feed);", strategy.getName(), 0, null);
            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.instruments);", strategy.getName(), 0, null);
            cx.evaluateString(scope, "importPackage(org.eclipsetrader.core.trading);", strategy.getName(), 0, null);

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
            if (onBar != null) {
                onBar.call(cx, scope, scope, new Object[] {
                    bar
                });
            }
            bars.add(bar);
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
        return bars.toArray(new IBar[bars.size()]);
    }
}
