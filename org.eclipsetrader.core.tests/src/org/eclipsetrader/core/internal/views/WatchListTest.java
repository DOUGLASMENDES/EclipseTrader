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

package org.eclipsetrader.core.internal.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import junit.framework.TestCase;

import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.core.views.IWatchListColumn;
import org.eclipsetrader.core.views.WatchList;

public class WatchListTest extends TestCase {

    private PropertyChangeEvent event;

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            event = evt;
        }
    };

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        event = null;
    }

    public void testFireNamePropertyChange() throws Exception {
        WatchList list = new WatchList("List", new IWatchListColumn[0]);
        ((PropertyChangeSupport) list.getAdapter(PropertyChangeSupport.class)).addPropertyChangeListener(propertyChangeListener);
        list.setName("List 1");
        assertNotNull(event);
        assertEquals(IWatchList.NAME, event.getPropertyName());
        assertEquals("List", event.getOldValue());
        assertEquals("List 1", event.getNewValue());
        assertSame(list, event.getSource());
    }
}
