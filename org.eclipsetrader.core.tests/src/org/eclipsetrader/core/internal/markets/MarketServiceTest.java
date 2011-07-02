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

package org.eclipsetrader.core.internal.markets;

import java.util.ArrayList;
import java.util.Observer;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class MarketServiceTest extends TestCase {

    public void testNotifyObserversOnAddMarket() throws Exception {
        MarketService service = new MarketService();

        Observer observer = EasyMock.createMock(Observer.class);
        observer.update(service, null);
        EasyMock.replay(observer);

        service.addObserver(observer);
        service.addMarket(new Market("Test", new ArrayList<MarketTime>()));

        EasyMock.verify(observer);
    }

    public void testNotifyObserversOnDeleteMarket() throws Exception {
        Market market = new Market("Test", new ArrayList<MarketTime>());
        MarketService service = new MarketService();
        service.addMarket(market);

        Observer observer = EasyMock.createMock(Observer.class);
        observer.update(service, null);
        EasyMock.replay(observer);

        service.addObserver(observer);
        service.deleteMarket(market);

        EasyMock.verify(observer);
    }
}
