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

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.ChartViewItem;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;

import junit.framework.TestCase;

public class ChartViewItemPropertyTesterTest extends TestCase {
	private ChartViewItemPropertyTester tester = new ChartViewItemPropertyTester();

	public void testIdProperty() throws Exception {
		ChartViewItem viewItem = new ChartViewItem(null, new ChartObjectFactoryMock(), "viewItem.id");
		assertTrue(tester.test(viewItem, "id", new Object[0], "viewItem.id"));
		assertFalse(tester.test(viewItem, "id", new Object[0], "id"));
    }

	public void testObjectFactoryProperty() throws Exception {
		ChartViewItem viewItem = new ChartViewItem(null, new ChartObjectFactoryMock(), "viewItem.id");
		assertTrue(tester.test(viewItem, "chartObjectFactory", new Object[0], "test.factory"));
		assertFalse(tester.test(viewItem, "chartObjectFactory", new Object[0], "factory"));
    }

	public void testUnknownProperty() throws Exception {
		ChartViewItem viewItem = new ChartViewItem(null, new ChartObjectFactoryMock(), "viewItem.id");
		assertFalse(tester.test(viewItem, "unknownProperty", new Object[0], "viewItem.id"));
    }

	public void testNotChartViewItemObject() throws Exception {
		assertFalse(tester.test("test", "id", new Object[0], "test"));
		assertFalse(tester.test("test", "chartObjectFactory", new Object[0], "test"));
    }

	public void testAdaptsProperty() throws Exception {
		ChartViewItem viewItem = new ChartViewItem(null, new ChartObjectFactoryMock("Adapter Object"), "viewItem.id");
		assertTrue(tester.test(viewItem, "adapts", new Object[0], "java.lang.String"));
		assertTrue(tester.test(viewItem, "adapts", new Object[0], IAdaptable.class.getName()));
		assertFalse(tester.test(viewItem, "adapts", new Object[0], "java.lang.Double"));
    }

	private class ChartObjectFactoryMock implements IChartObjectFactory, IAdaptable {
		private Object reference;

		public ChartObjectFactoryMock() {
        }

		public ChartObjectFactoryMock(Object reference) {
	        this.reference = reference;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
         */
        public IChartObject createObject(IDataSeries source) {
	        return null;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
         */
        public String getId() {
	        return "test.factory";
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
         */
        public String getName() {
	        return null;
        }

    	/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
         */
        public IChartParameters getParameters() {
        	ChartParameters parameters = new ChartParameters();
    	    return parameters;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
         */
        public void setParameters(IChartParameters parameters) {
        }

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
        	if (reference != null && adapter.isAssignableFrom(reference.getClass()))
        		return reference;
	        return null;
        }
	}
}
