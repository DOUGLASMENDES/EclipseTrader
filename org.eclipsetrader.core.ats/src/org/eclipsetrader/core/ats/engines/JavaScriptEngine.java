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

import java.util.HashMap;
import java.util.Map;

import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.ats.ITradingInstrument;
import org.eclipsetrader.core.ats.ITradingSystem;
import org.eclipsetrader.core.feed.IBar;
import org.eclipsetrader.core.feed.IBarOpen;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.PositionEvent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptEngine {

    public static final String PROPERTY_INSTRUMENTS = "instruments";
    public static final String PROPERTY_POSITIONS = "positions";

    private final ITradingSystem tradingSystem;
    private final IScriptStrategy strategy;
    private final IPricingEnvironment pricingEnvironment;
    private final IAccount account;
    private final IBroker broker;

    private ScriptableObject scope;
    private Map<String, ISecurity> instrumentsMap = new HashMap<String, ISecurity>();
    private Map<String, IPosition> positionsMap = new HashMap<String, IPosition>();
    private Map<ISecurity, JavaScriptEngineInstrument> contextsMap = new HashMap<ISecurity, JavaScriptEngineInstrument>();

    public IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            doPricingUpdate(event);
        }
    };

    private IPositionListener positionListener = new IPositionListener() {

        @Override
        public void positionOpened(PositionEvent e) {
            JavaScriptEngineInstrument context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionOpen(e.position);
            updatePositionsMap();
        }

        @Override
        public void positionClosed(PositionEvent e) {
            JavaScriptEngineInstrument context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionClosed(e.position);
            updatePositionsMap();
        }

        @Override
        public void positionChanged(PositionEvent e) {
            JavaScriptEngineInstrument context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionChange(e.position);
            updatePositionsMap();
        }
    };

    public JavaScriptEngine(ITradingSystem tradingSystem, IPricingEnvironment pricingEnvironment, IAccount account, IBroker broker) {
        this.tradingSystem = tradingSystem;
        this.strategy = (IScriptStrategy) tradingSystem.getStrategy();
        this.pricingEnvironment = pricingEnvironment;
        this.account = account;
        this.broker = broker;
    }

    public void start() throws Exception {
        Context cx = Context.enter();
        try {
            cx.setWrapFactory(new EnhancedWrapFactory());

            scope = new ImporterTopLevel(cx);

            scope.putConst("Buy", scope, BaseOrderFunction.Buy);
            scope.putConst("Sell", scope, BaseOrderFunction.Sell);
            scope.putConst("Limit", scope, BaseOrderFunction.Limit);
            scope.putConst("Market", scope, BaseOrderFunction.Market);
            scope.putConst("Above", scope, 1);
            scope.putConst("Below", scope, -1);

            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, account);
            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_BROKER, broker);
            ScriptableObject.putProperty(scope, PROPERTY_INSTRUMENTS, instrumentsMap);
            ScriptableObject.putProperty(scope, PROPERTY_POSITIONS, positionsMap);

            ScriptableObject.putProperty(scope, "out", Context.javaToJS(System.out, scope));

            updateInstrumentsMap();
            updatePositionsMap();

            for (ITradingInstrument instrument : tradingSystem.getInstruments()) {
                JavaScriptEngineInstrument engineInstrument = new JavaScriptEngineInstrument(scope, instrument.getInstrument(), strategy);
                engineInstrument.onStrategyStart();
                contextsMap.put(instrument.getInstrument(), engineInstrument);
            }

            account.addPositionListener(positionListener);
            pricingEnvironment.addPricingListener(pricingListener);
        } finally {
            Context.exit();
        }
    }

    public void stop() {
        contextsMap.clear();

        account.removePositionListener(positionListener);
        pricingEnvironment.removePricingListener(pricingListener);

        scope = null;
    }

    public void dispose() {

    }

    void doPricingUpdate(PricingEvent event) {
        for (PricingDelta delta : event.getDelta()) {
            JavaScriptEngineInstrument instrument = contextsMap.get(delta.getSecurity());
            if (instrument == null) {
                instrument = contextsMap.get(event.getSecurity());
                if (instrument == null) {
                    continue;
                }
            }
            Object value = delta.getNewValue();
            if (value instanceof IQuote) {
                instrument.onQuote((IQuote) value);
            }
            else if (value instanceof ITrade) {
                instrument.onTrade((ITrade) value);
            }
            else if (value instanceof IBarOpen) {
                instrument.onBarOpen((IBarOpen) value);
            }
            else if (value instanceof IBar) {
                instrument.onBar((IBar) value);
            }
        }
    }

    private void updateInstrumentsMap() {
        instrumentsMap.clear();
        for (ISecurity security : strategy.getInstruments()) {
            if (security.getIdentifier() != null) {
                instrumentsMap.put(security.getIdentifier().getSymbol(), security);
            }
        }
    }

    void updatePositionsMap() {
        positionsMap.clear();
        for (IPosition position : account.getPositions()) {
            ISecurity security = position.getSecurity();
            if (security.getIdentifier() != null) {
                positionsMap.put(security.getIdentifier().getSymbol(), position);
            }
        }
    }

    public Object get(String name) {
        return scope.get(name, scope);
    }

    public ScriptableObject getScope() {
        return scope;
    }

    public JavaScriptEngineInstrument getContextFor(ISecurity security) {
        return contextsMap.get(security);
    }
}
