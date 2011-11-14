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

package org.eclipsetrader.core.feed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;
import org.eclipsetrader.tests.Helper;

public class HistoryTest extends TestCase {

    public void testGetFirst() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        assertSame(bars[2], history.getFirst());
    }

    public void testGetLast() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        assertSame(bars[0], history.getLast());
    }

    public void testGetHighest() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        assertSame(bars[2], history.getHighest());
    }

    public void testGetLowest() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        assertSame(bars[1], history.getLowest());
    }

    public void testGetSubset() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 14), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 15), 200.0, 210.0, 190.0, 195.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);
        IHistory subset = history.getSubset(Helper.getTime(2007, Calendar.NOVEMBER, 12), Helper.getTime(2007, Calendar.NOVEMBER, 14));
        assertEquals(3, subset.getOHLC().length);
        assertSame(bars[1], subset.getOHLC()[0]);
        assertSame(bars[2], subset.getOHLC()[1]);
        assertSame(bars[3], subset.getOHLC()[2]);
    }

    public void testGetAggregatedSubsetFromStore() throws Exception {
        StoreProperties day22StoreProperties = new StoreProperties();
        day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 22));
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day22StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars);

        StoreProperties day23StoreProperties = new StoreProperties();
        day23StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day23StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 23));

        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
            new TestStore(day22StoreProperties, null),
            new TestStore(day23StoreProperties, null),
        });

        History history = new History(historyStore, historyStore.fetchProperties(null));
        IHistory subset = history.getSubset(Helper.getTime(2008, Calendar.MAY, 22), Helper.getTime(2008, Calendar.MAY, 22), TimeSpan.minutes(1));
        assertEquals(3, subset.getOHLC().length);
        assertSame(bars[0], subset.getOHLC()[0]);
        assertSame(bars[1], subset.getOHLC()[1]);
        assertSame(bars[2], subset.getOHLC()[2]);
    }

    public void testGetMultidayAggregatedSubsetFromStore() throws Exception {
        StoreProperties day22StoreProperties = new StoreProperties();
        day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 22));
        IOHLC[] bars22 = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day22StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars22);

        StoreProperties day23StoreProperties = new StoreProperties();
        day23StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day23StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 23));
        IOHLC[] bars23 = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day23StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars23);

        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
            new TestStore(day22StoreProperties, null),
            new TestStore(day23StoreProperties, null),
        });

        History history = new History(historyStore, historyStore.fetchProperties(null));
        IHistory subset = history.getSubset(Helper.getTime(2008, Calendar.MAY, 22), Helper.getTime(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
        assertEquals(6, subset.getOHLC().length);
    }

    public void testBuildMissingAggregatedSubset() throws Exception {
        StoreProperties day22StoreProperties = new StoreProperties();
        day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 22));
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 8), 26.38, 26.41, 26.38, 26.41, 58018L),
        };
        day22StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars);

        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
            new TestStore(day22StoreProperties, null),
        });

        History history = new History(historyStore, historyStore.fetchProperties(null));
        IHistory subset = history.getSubset(Helper.getTime(2008, Calendar.MAY, 22), Helper.getTime(2008, Calendar.MAY, 22), TimeSpan.minutes(2));
        assertEquals(3, subset.getOHLC().length);
        assertEquals(new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 3), 26.56, 26.56, 26.56, 26.56, 3043159L), subset.getOHLC()[0]);
        assertEquals(new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.47, 26.47, 76839L), subset.getOHLC()[1]);
        assertEquals(new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.41, 202512L), subset.getOHLC()[2]);
    }

    public void testGetSplitsAdjustedHistory() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25, 48.50, 46.77, 48.30, 90446400L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62, 24.99, 24.40, 24.96, 57415500L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L),
        };
        ISplit[] splits = new ISplit[] {
            new Split(Helper.getTime(2003, Calendar.FEBRUARY, 18), 1.0, 2.0),
        };
        History history = new History(new Security("Test", null), bars, splits, null);
        IOHLC[] adjustedBars = history.getAdjustedOHLC();
        assertEquals(3, adjustedBars.length);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25 / 2, 48.50 / 2, 46.77 / 2, 48.30 / 2, 90446400L * 2), adjustedBars[0]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62, 24.99, 24.40, 24.96, 57415500L), adjustedBars[1]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L), adjustedBars[2]);
    }

    public void testGetDividendsAdjustedHistory() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25, 48.50, 46.77, 48.30, 90446400L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62, 24.99, 24.40, 24.96, 57415500L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L),
        };
        IDividend[] dividends = new IDividend[] {
            new Dividend(Helper.getTime(2003, Calendar.FEBRUARY, 19), 0.08),
        };

        Stock security = new Stock("Test", null, Currency.getInstance("USD"));
        security.setDividends(dividends);
        History history = new History(security, bars, null, null);

        IOHLC[] adjustedBars = history.getAdjustedOHLC();
        assertEquals(3, adjustedBars.length);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25 - 0.08, 48.50 - 0.08, 46.77 - 0.08, 48.30 - 0.08, 90446400L), adjustedBars[0]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62 - 0.08, 24.99 - 0.08, 24.40 - 0.08, 24.96 - 0.08, 57415500L), adjustedBars[1]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L), adjustedBars[2]);
    }

    public void testGetDividendsAndSplitsAdjustedHistory() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25, 48.50, 46.77, 48.30, 90446400L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62, 24.99, 24.40, 24.96, 57415500L),
            new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L),
        };
        ISplit[] splits = new ISplit[] {
            new Split(Helper.getTime(2003, Calendar.FEBRUARY, 18), 1.0, 2.0),
        };
        IDividend[] dividends = new IDividend[] {
            new Dividend(Helper.getTime(2003, Calendar.FEBRUARY, 19), 0.08),
        };

        Stock security = new Stock("Test", null, Currency.getInstance("USD"));
        security.setDividends(dividends);
        History history = new History(security, bars, splits, null);

        IOHLC[] adjustedBars = history.getAdjustedOHLC();
        assertEquals(3, adjustedBars.length);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 14), 47.25 / 2 - 0.08, 48.50 / 2 - 0.08, 46.77 / 2 - 0.08, 48.30 / 2 - 0.08, 90446400L * 2), adjustedBars[0]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 18), 24.62 - 0.08, 24.99 - 0.08, 24.40 - 0.08, 24.96 - 0.08, 57415500L), adjustedBars[1]);
        assertEquals(new OHLC(Helper.getTime(2003, Calendar.FEBRUARY, 19), 24.82, 24.88, 24.17, 24.53, 46902700L), adjustedBars[2]);
    }

    public void testNotifyUpdatesOfIntradaySubsets() throws Exception {
        StoreProperties day22StoreProperties = new StoreProperties();
        day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 22));
        IOHLC[] bars22 = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day22StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars22);

        StoreProperties day23StoreProperties = new StoreProperties();
        day23StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
        day23StoreProperties.setProperty(IPropertyConstants.BARS_DATE, Helper.getTime(2008, Calendar.MAY, 23));
        IOHLC[] bars23 = new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
        };
        day23StoreProperties.setProperty(TimeSpan.minutes(1).toString(), bars23);

        TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
            new TestStore(day22StoreProperties, null),
            new TestStore(day23StoreProperties, null),
        });

        History history = new History(historyStore, historyStore.fetchProperties(null));

        final Set<IHistory> updates = new HashSet<IHistory>();

        IHistory subset1 = history.getSubset(Helper.getTime(2008, Calendar.MAY, 22), Helper.getTime(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
        assertEquals(6, subset1.getOHLC().length);
        PropertyChangeSupport propertyChangeSupport = (PropertyChangeSupport) subset1.getAdapter(PropertyChangeSupport.class);
        propertyChangeSupport.addPropertyChangeListener(IPropertyConstants.BARS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updates.add((IHistory) evt.getSource());
            }
        });

        IHistory subset2 = history.getSubset(Helper.getTime(2008, Calendar.MAY, 23), Helper.getTime(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
        assertEquals(3, subset2.getOHLC().length);
        propertyChangeSupport = (PropertyChangeSupport) subset2.getAdapter(PropertyChangeSupport.class);
        propertyChangeSupport.addPropertyChangeListener(IPropertyConstants.BARS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updates.add((IHistory) evt.getSource());
            }
        });

        ((HistoryDay) subset2).setOHLC(new IOHLC[] {
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
            new OHLC(Helper.getTime(2008, Calendar.MAY, 23, 9, 9), 26.47, 26.47, 26.37, 26.39, 144494L),
        });
        assertEquals(4, subset2.getOHLC().length);
        assertEquals(7, subset1.getOHLC().length);

        assertEquals(2, updates.size());
        assertTrue(updates.contains(subset1));
        assertTrue(updates.contains(subset2));
    }

    public void testNotifyOHLCUpdates() throws Exception {
        PropertyChangeListener listener = EasyMock.createMock(PropertyChangeListener.class);
        listener.propertyChange(EasyMock.isA(PropertyChangeEvent.class));
        EasyMock.replay(listener);

        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);

        PropertyChangeSupport changeSupport = (PropertyChangeSupport) history.getAdapter(PropertyChangeSupport.class);
        changeSupport.addPropertyChangeListener(listener);

        IOHLC[] newBars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        history.setOHLC(newBars);

        EasyMock.verify(listener);
    }

    public void testNotifyOHLCSubsetUpdates() throws Exception {
        PropertyChangeListener listener = EasyMock.createMock(PropertyChangeListener.class);
        listener.propertyChange(EasyMock.isA(PropertyChangeEvent.class));
        EasyMock.replay(listener);

        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);

        IHistory subsetHistory = history.getSubset(Helper.getTime(2007, Calendar.NOVEMBER, 12), Helper.getTime(2007, Calendar.NOVEMBER, 13));

        assertEquals(1, subsetHistory.getOHLC().length);

        PropertyChangeSupport changeSupport = (PropertyChangeSupport) subsetHistory.getAdapter(PropertyChangeSupport.class);
        changeSupport.addPropertyChangeListener(listener);

        IOHLC[] newBars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        history.setOHLC(newBars);

        assertEquals(2, subsetHistory.getOHLC().length);

        EasyMock.verify(listener);
    }

    public void testGetSameSubsetInstance() throws Exception {
        IOHLC[] bars = new IOHLC[] {
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
            new OHLC(Helper.getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
        };
        History history = new History(new Security("Test", null), bars);

        IHistory subsetHistory = history.getSubset(Helper.getTime(2007, Calendar.NOVEMBER, 12), Helper.getTime(2007, Calendar.NOVEMBER, 13));

        IHistory subsetHistory2 = history.getSubset(Helper.getTime(2007, Calendar.NOVEMBER, 12), Helper.getTime(2007, Calendar.NOVEMBER, 13));

        assertSame(subsetHistory, subsetHistory2);
    }

    public class TestStore implements IStore {

        private IStoreProperties storeProperties;
        private IStore[] childs;

        public TestStore() {
        }

        public TestStore(IStoreProperties storeProperties) {
            this.storeProperties = storeProperties;
            this.childs = new IStore[0];
        }

        public TestStore(IStore[] childs) {
            this.storeProperties = new StoreProperties();
            this.childs = childs;
        }

        public TestStore(IStoreProperties storeProperties, IStore[] childs) {
            this.storeProperties = storeProperties;
            this.childs = childs;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#delete(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void delete(IProgressMonitor monitor) throws CoreException {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStoreProperties fetchProperties(IProgressMonitor monitor) {
            return storeProperties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStore[] fetchChilds(IProgressMonitor monitor) {
            return childs;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#createChild()
         */
        @Override
        public IStore createChild() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#getRepository()
         */
        @Override
        public IRepository getRepository() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
            this.storeProperties = properties;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.repositories.IStore#toURI()
         */
        @Override
        public URI toURI() {
            return null;
        }
    }
}
