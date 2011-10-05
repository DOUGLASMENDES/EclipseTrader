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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipsetrader.core.ats.BarFactoryEvent;
import org.eclipsetrader.core.ats.IBarFactoryListener;
import org.eclipsetrader.core.ats.IScriptStrategy;
import org.eclipsetrader.core.feed.Bar;
import org.eclipsetrader.core.feed.BarOpen;
import org.eclipsetrader.core.feed.IPricingEnvironment;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.ats.BarFactory;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.IPositionListener;
import org.eclipsetrader.core.trading.PositionEvent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptEngineRunner {

    public static final String PROPERTY_INSTRUMENTS = "instruments";
    public static final String PROPERTY_POSITIONS = "positions";

    private final IScriptStrategy strategy;
    private final IPricingEnvironment pricingEnvironment;
    private final IAccount account;
    private final IBroker broker;

    private ScriptableObject scope;
    private Map<String, ISecurity> instrumentsMap = new HashMap<String, ISecurity>();
    private Map<String, IPosition> positionsMap = new HashMap<String, IPosition>();
    private Map<ISecurity, JavaScriptStrategyContext> contextsMap = new HashMap<ISecurity, JavaScriptStrategyContext>();

    private List<BarFactory> barFactory = new ArrayList<BarFactory>();

    private IPricingListener pricingListener = new IPricingListener() {

        @Override
        public void pricingUpdate(PricingEvent event) {
            doPricingUpdate(event);
        }
    };

    private IBarFactoryListener barFactoryListener = new IBarFactoryListener() {

        @Override
        public void barOpen(BarFactoryEvent event) {
            JavaScriptStrategyContext context = contextsMap.get(event.security);
            if (context == null) {
                return;
            }
            context.onBarOpen(new BarOpen(event.date, event.timeSpan, event.open));
        }

        @Override
        public void barClose(BarFactoryEvent event) {
            JavaScriptStrategyContext context = contextsMap.get(event.security);
            if (context == null) {
                return;
            }
            context.onBar(new Bar(event.date, event.timeSpan, event.open, event.high, event.low, event.close, event.volume));
        }
    };

    private IPositionListener positionListener = new IPositionListener() {

        @Override
        public void positionOpened(PositionEvent e) {
            JavaScriptStrategyContext context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionOpen(e.position);
            updatePositionsMap();
        }

        @Override
        public void positionClosed(PositionEvent e) {
            JavaScriptStrategyContext context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionClose(e.position);
            updatePositionsMap();
        }

        @Override
        public void positionChanged(PositionEvent e) {
            JavaScriptStrategyContext context = contextsMap.get(e.position.getSecurity());
            if (context == null) {
                return;
            }
            context.onPositionChange(e.position);
            updatePositionsMap();
        }
    };

    public JavaScriptEngineRunner(IScriptStrategy strategy, IPricingEnvironment pricingEnvironment, IAccount account, IBroker broker) {
        this.strategy = strategy;
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

            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_ACCOUNT, account);
            ScriptableObject.putProperty(scope, BaseOrderFunction.PROPERTY_BROKER, broker);
            ScriptableObject.putProperty(scope, PROPERTY_INSTRUMENTS, instrumentsMap);
            ScriptableObject.putProperty(scope, PROPERTY_POSITIONS, positionsMap);

            ScriptableObject.putProperty(scope, "out", Context.javaToJS(System.out, scope));

            updateInstrumentsMap();
            updatePositionsMap();

            for (ISecurity security : strategy.getInstruments()) {
                JavaScriptStrategyContext context = new JavaScriptStrategyContext(scope, security, strategy);
                context.init();
                contextsMap.put(security, context);
            }

            account.addPositionListener(positionListener);
            pricingEnvironment.addPricingListener(pricingListener);

            for (ISecurity security : strategy.getInstruments()) {
                for (TimeSpan timeSpan : strategy.getBarsTimeSpan()) {
                    BarFactory factory = new BarFactory(security, timeSpan, pricingEnvironment);
                    factory.addBarFactoryListener(barFactoryListener);
                    barFactory.add(factory);
                }
            }
        } finally {
            Context.exit();
        }
    }

    public void stop() {
        contextsMap.clear();

        for (BarFactory factory : barFactory) {
            factory.removeBarFactoryListener(barFactoryListener);
            factory.dispose();
        }
        barFactory.clear();

        account.removePositionListener(positionListener);
        pricingEnvironment.removePricingListener(pricingListener);

        scope = null;
    }

    protected void doPricingUpdate(PricingEvent event) {
        for (PricingDelta delta : event.getDelta()) {
            JavaScriptStrategyContext context = contextsMap.get(delta.getSecurity());
            if (context == null) {
                context = contextsMap.get(event.getSecurity());
                if (context == null) {
                    continue;
                }
            }
            Object value = delta.getNewValue();
            if (value instanceof IQuote) {
                context.onQuote((IQuote) value);
            }
            else if (value instanceof ITrade) {
                context.onTrade((ITrade) value);
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

    public JavaScriptStrategyContext getContextFor(ISecurity security) {
        return contextsMap.get(security);
    }
}
