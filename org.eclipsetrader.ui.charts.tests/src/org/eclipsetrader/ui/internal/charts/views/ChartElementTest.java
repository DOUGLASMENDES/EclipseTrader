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

package org.eclipsetrader.ui.internal.charts.views;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.charts.repository.IElementSection;
import org.eclipsetrader.ui.charts.ChartParameters;
import org.eclipsetrader.ui.charts.IChartIndicator;
import org.eclipsetrader.ui.charts.IChartParameters;

public class ChartElementTest extends TestCase {

	public void testGetTemplate() throws Exception {
		IChartIndicator indicator = new IChartIndicator() {
            public IAdaptable computeElement(IAdaptable source, IChartParameters parameters) {
	            return null;
            }

            public String getId() {
	            return "plugin-id";
            }

            public String getName() {
	            return null;
            }
		};
		ChartParameters parameters = new ChartParameters();
		parameters.setParameter("p1", "v1");
		ChartElement element = new ChartElement("id", parameters, indicator, null);
		IElementSection template = element.getTemplate();
		assertEquals("id", template.getId());
		assertEquals("plugin-id", template.getPluginId());
		assertEquals(1, template.getParameters().length);
    }
}
