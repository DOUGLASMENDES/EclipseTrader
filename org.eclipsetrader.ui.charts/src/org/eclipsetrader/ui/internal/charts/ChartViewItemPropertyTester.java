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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.ui.charts.ChartViewItem;
import org.eclipsetrader.ui.charts.IChartObjectFactory;

/**
 * Provides property tests for <code>ChartViewItem</code> objects.
 *
 * @since 1.0
 */
public class ChartViewItemPropertyTester extends PropertyTester {

	public ChartViewItemPropertyTester() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IAdaptable)
			receiver = ((IAdaptable) receiver).getAdapter(ChartViewItem.class);

		if (receiver == null || !(receiver instanceof ChartViewItem))
			return false;

		if ("id".equals(property)) {
			String id = ((ChartViewItem) receiver).getId();
			return id.equals(expectedValue);
		}
		else if ("chartObjectFactory".equals(property)) {
			IChartObjectFactory factory = ((ChartViewItem) receiver).getFactory();
			if (factory != null)
				return factory.getId().equals(expectedValue);
		}
		else if ("adapts".equals(property)) {
			if (!(receiver instanceof IAdaptable))
				return false;
			try {
				Class clazz = Class.forName(expectedValue.toString());
				Object o = ((IAdaptable) receiver).getAdapter(clazz);
				if (o != null && clazz.isAssignableFrom(o.getClass()))
					return true;
			} catch (Exception e) {
				e.printStackTrace();
				// Do nothing
			}
		}

		return false;
	}
}
