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

package org.eclipsetrader.ui.charts;

import junit.framework.TestCase;

import org.eclipsetrader.core.charts.repository.IChartTemplate;
import org.eclipsetrader.core.internal.charts.repository.ChartTemplate;
import org.eclipsetrader.core.views.IViewChangeListener;
import org.eclipsetrader.core.views.ViewEvent;
import org.eclipsetrader.core.views.ViewItemDelta;

public class ChartViewTest extends TestCase {

    private ViewEvent viewEvent;
    private IViewChangeListener listener = new IViewChangeListener() {

        @Override
        public void viewChanged(ViewEvent event) {
            viewEvent = event;
        }
    };

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        viewEvent = null;
    }

    public void testCreateFromEmptyTemplate() throws Exception {
        ChartTemplate template = new ChartTemplate("Test");
        ChartView view = new ChartView(template);
        assertEquals("Test", view.getName());
        assertEquals(0, view.getItems().length);
    }

    public void testGetTemplateFromEmptyDocument() throws Exception {
        ChartView view = new ChartView("Test");
        IChartTemplate template = view.getTemplate();
        assertEquals("Test", template.getName());
        assertEquals(0, template.getSections().length);
    }

    public void testNotifyRowAdded() throws Exception {
        ChartView view = new ChartView();
        ChartRowViewItem viewItem = new ChartRowViewItem(view, "Test");
        view.addViewChangeListener(listener);
        view.addRow(viewItem);
        assertNotNull(viewEvent);
        assertEquals(1, viewEvent.getDelta().length);
        assertEquals(ViewItemDelta.ADDED, viewEvent.getDelta()[0].getKind());
        assertSame(viewItem, viewEvent.getDelta()[0].getViewItem());
    }

    public void testNotifyRowRemoved() throws Exception {
        ChartView view = new ChartView();
        ChartRowViewItem viewItem = new ChartRowViewItem(view, "Test");
        view.addRow(viewItem);
        view.addViewChangeListener(listener);
        view.removeRow(viewItem);
        assertNotNull(viewEvent);
        assertEquals(1, viewEvent.getDelta().length);
        assertEquals(ViewItemDelta.REMOVED, viewEvent.getDelta()[0].getKind());
        assertSame(viewItem, viewEvent.getDelta()[0].getViewItem());
    }
}
