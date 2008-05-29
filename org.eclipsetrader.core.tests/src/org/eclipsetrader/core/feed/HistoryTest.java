/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.feed;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.core.repositories.IStoreProperties;
import org.eclipsetrader.core.repositories.StoreProperties;

public class HistoryTest extends TestCase {

	public void testGetFirst() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[2], history.getFirst());
    }

	public void testGetLast() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[0], history.getLast());
    }

	public void testGetHighest() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[2], history.getHighest());
    }

	public void testGetLowest() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		assertSame(bars[1], history.getLowest());
    }

	public void testGetSubset() throws Exception {
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2007, Calendar.NOVEMBER, 11), 400.0, 410.0, 390.0, 395.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 12), 100.0, 110.0, 90.0, 95.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 13), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 14), 200.0, 210.0, 190.0, 195.0, 100000L),
				new OHLC(getTime(2007, Calendar.NOVEMBER, 15), 200.0, 210.0, 190.0, 195.0, 100000L),
			};
		History history = new History(new Security("Test", null), bars);
		IHistory subset = history.getSubset(getTime(2007, Calendar.NOVEMBER, 12), getTime(2007, Calendar.NOVEMBER, 14));
		assertEquals(3, subset.getOHLC().length);
		assertSame(bars[1], subset.getOHLC()[0]);
		assertSame(bars[2], subset.getOHLC()[1]);
		assertSame(bars[3], subset.getOHLC()[2]);
    }

	public void testGetAggregatedSubsetFromStore() throws Exception {
		StoreProperties aggregateStoreProperties1 = new StoreProperties();
		aggregateStoreProperties1.setProperty(IPropertyConstants.TIME_SPAN, TimeSpan.minutes(1));
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
			};
		aggregateStoreProperties1.setProperty(IPropertyConstants.BARS, bars);

		StoreProperties day22StoreProperties = new StoreProperties();
		day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
		day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, getTime(2008, Calendar.MAY, 22));

		StoreProperties day23StoreProperties = new StoreProperties();
		day23StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
		day23StoreProperties.setProperty(IPropertyConstants.BARS_DATE, getTime(2008, Calendar.MAY, 23));

	    TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
	    		new TestStore(day22StoreProperties, new IStore[] {
	    				new TestStore(aggregateStoreProperties1),
		    		}),
	    		new TestStore(day23StoreProperties, new IStore[] {
	    				new TestStore(aggregateStoreProperties1),
		    		}),
	    	});

	    History history = new History(historyStore, historyStore.fetchProperties(null));
	    IHistory subset = history.getSubset(getTime(2008, Calendar.MAY, 22), getTime(2008, Calendar.MAY, 22), TimeSpan.minutes(1));
	    assertEquals(3, subset.getOHLC().length);
		assertSame(bars[0], subset.getOHLC()[0]);
		assertSame(bars[1], subset.getOHLC()[1]);
		assertSame(bars[2], subset.getOHLC()[2]);
    }

	public void testGetMultidayAggregatedSubsetFromStore() throws Exception {
		StoreProperties aggregateStoreProperties1 = new StoreProperties();
		aggregateStoreProperties1.setProperty(IPropertyConstants.TIME_SPAN, TimeSpan.minutes(1));
		IOHLC[] bars = new IOHLC[] {
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 5), 26.55, 26.6, 26.51, 26.52, 35083L),
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 6), 26.52, 26.52, 26.47, 26.47, 41756L),
				new OHLC(getTime(2008, Calendar.MAY, 22, 9, 7), 26.47, 26.47, 26.37, 26.39, 144494L),
			};
		aggregateStoreProperties1.setProperty(IPropertyConstants.BARS, bars);

		StoreProperties day22StoreProperties = new StoreProperties();
		day22StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
		day22StoreProperties.setProperty(IPropertyConstants.BARS_DATE, getTime(2008, Calendar.MAY, 22));

		StoreProperties day23StoreProperties = new StoreProperties();
		day23StoreProperties.setProperty(IPropertyConstants.OBJECT_TYPE, IHistory.class.getName());
		day23StoreProperties.setProperty(IPropertyConstants.BARS_DATE, getTime(2008, Calendar.MAY, 23));

	    TestStore historyStore = new TestStore(new StoreProperties(), new IStore[] {
	    		new TestStore(day22StoreProperties, new IStore[] {
	    				new TestStore(aggregateStoreProperties1),
		    		}),
	    		new TestStore(day23StoreProperties, new IStore[] {
	    				new TestStore(aggregateStoreProperties1),
		    		}),
	    	});

	    History history = new History(historyStore, historyStore.fetchProperties(null));
	    IHistory subset = history.getSubset(getTime(2008, Calendar.MAY, 22), getTime(2008, Calendar.MAY, 23), TimeSpan.minutes(1));
	    assertEquals(6, subset.getOHLC().length);
    }

	private Date getTime(int year, int month, int day) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, 0, 0, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}

	private Date getTime(int year, int month, int day, int hour, int minute) {
	    Calendar date = Calendar.getInstance();
	    date.set(year, month, day, hour, minute, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
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
		public void delete(IProgressMonitor monitor) throws CoreException {
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.repositories.IStore#fetchProperties(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStoreProperties fetchProperties(IProgressMonitor monitor) {
			return storeProperties;
		}

		/* (non-Javadoc)
	     * @see org.eclipsetrader.core.repositories.IStore#fetchChilds(org.eclipse.core.runtime.IProgressMonitor)
	     */
	    public IStore[] fetchChilds(IProgressMonitor monitor) {
	        return childs;
	    }

		/* (non-Javadoc)
	     * @see org.eclipsetrader.core.repositories.IStore#createChild()
	     */
	    public IStore createChild() {
	        return null;
	    }

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.repositories.IStore#getRepository()
		 */
		public IRepository getRepository() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.repositories.IStore#putProperties(org.eclipsetrader.core.repositories.IStoreProperties, org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void putProperties(IStoreProperties properties, IProgressMonitor monitor) {
			this.storeProperties = properties;
		}

		/* (non-Javadoc)
		 * @see org.eclipsetrader.core.repositories.IStore#toURI()
		 */
		public URI toURI() {
			return null;
		}
	}
}
