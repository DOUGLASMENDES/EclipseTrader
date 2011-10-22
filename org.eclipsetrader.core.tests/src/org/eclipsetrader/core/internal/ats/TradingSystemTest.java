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

package org.eclipsetrader.core.internal.ats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.eclipsetrader.core.ats.ScriptStrategy;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

public class TradingSystemTest extends TestCase {

    public void testAddNewInstruments() throws Exception {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");
        strategy.setInstruments(new ISecurity[] {
            instrument1
        });

        TradingSystem tradingSystem = new TradingSystem(strategy);
        assertEquals(1, tradingSystem.getInstruments().length);

        strategy.setInstruments(new ISecurity[] {
            instrument1, instrument2
        });
        assertEquals(2, tradingSystem.getInstruments().length);
    }

    public void testRemoveOldInstruments() throws Exception {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");
        strategy.setInstruments(new ISecurity[] {
            instrument1, instrument2
        });

        TradingSystem tradingSystem = new TradingSystem(strategy);
        assertEquals(2, tradingSystem.getInstruments().length);

        strategy.setInstruments(new ISecurity[] {
            instrument1
        });
        assertEquals(1, tradingSystem.getInstruments().length);
    }

    public void testNotifyInstrumentChanges() throws Exception {
        Security instrument1 = new Security("Test1", new FeedIdentifier("TST1", null));
        Security instrument2 = new Security("Test2", new FeedIdentifier("TST2", null));

        PropertyChangeListener changeListener = EasyMock.createMock(PropertyChangeListener.class);
        changeListener.propertyChange(EasyMock.isA(PropertyChangeEvent.class));
        EasyMock.replay(changeListener);

        ScriptStrategy strategy = new ScriptStrategy("Test Strategy");

        TradingSystem tradingSystem = new TradingSystem(strategy);
        tradingSystem.addPropertyChangeListener(changeListener);

        strategy.setInstruments(new ISecurity[] {
            instrument1, instrument2
        });

        EasyMock.verify(changeListener);
    }
}
