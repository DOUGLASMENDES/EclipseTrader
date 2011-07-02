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

package org.eclipsetrader.ui.internal.markets;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.internal.markets.MarketTime;

public class TimeScheduleEditorTest extends TestCase {

    private Shell shell;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getCurrent());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        shell.dispose();
    }

    public void testCreateEmpty() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        assertEquals(0, editor.getViewer().getTable().getItemCount());
        assertEquals(0, editor.getSchedule().length);
    }

    public void testSetScheduleInProperOrder() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        MarketTime[] schedule = new MarketTime[] {
                new MarketTime(getTime(18, 0), getTime(20, 0)),
                new MarketTime(getTime(9, 0), getTime(16, 0)),
        };
        editor.setSchedule(schedule);
        assertEquals(schedule[1].getOpenTime(), ((MarketTimeElement) editor.getViewer().getTable().getItem(0).getData()).getOpenTime());
        assertEquals(schedule[0].getOpenTime(), ((MarketTimeElement) editor.getViewer().getTable().getItem(1).getData()).getOpenTime());
    }

    public void testEditOpenTime() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.setSchedule(new MarketTime[] {
            new MarketTime(getTime(9, 0), getTime(16, 0)),
        });
        MarketTimeElement element = (MarketTimeElement) editor.getViewer().getTable().getItem(0).getData();
        ICellModifier cellModifier = editor.getViewer().getCellModifier();
        assertTrue(cellModifier.canModify(element, "0"));
        assertEquals(element.getOpenTime(), cellModifier.getValue(element, "0"));
        cellModifier.modify(element, "0", getTime(10, 30));
        assertEquals(getTime(10, 30), element.getOpenTime());
    }

    public void testEditCloseTime() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.setSchedule(new MarketTime[] {
            new MarketTime(getTime(9, 0), getTime(16, 0)),
        });
        MarketTimeElement element = (MarketTimeElement) editor.getViewer().getTable().getItem(0).getData();
        ICellModifier cellModifier = editor.getViewer().getCellModifier();
        assertTrue(cellModifier.canModify(element, "1"));
        assertEquals(element.getCloseTime(), cellModifier.getValue(element, "1"));
        cellModifier.modify(element, "1", getTime(10, 30));
        assertEquals(getTime(10, 30), element.getCloseTime());
    }

    public void testEditDescription() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.setSchedule(new MarketTime[] {
            new MarketTime(getTime(9, 0), getTime(16, 0)),
        });
        MarketTimeElement element = (MarketTimeElement) editor.getViewer().getTable().getItem(0).getData();
        ICellModifier cellModifier = editor.getViewer().getCellModifier();
        assertTrue(cellModifier.canModify(element, "2"));
        assertEquals("", cellModifier.getValue(element, "2"));
        cellModifier.modify(element, "2", "New Description");
        assertEquals("New Description", element.getDescription());
    }

    public void testEditDescriptionToNull() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.setSchedule(new MarketTime[] {
            new MarketTime(getTime(9, 0), getTime(16, 0), "Description"),
        });
        MarketTimeElement element = (MarketTimeElement) editor.getViewer().getTable().getItem(0).getData();
        ICellModifier cellModifier = editor.getViewer().getCellModifier();
        assertTrue(cellModifier.canModify(element, "2"));
        assertEquals("Description", cellModifier.getValue(element, "2"));
        cellModifier.modify(element, "2", "");
        assertNull(element.getDescription());
    }

    public void testGetSchedule() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.getInput().add(new MarketTimeElement(getTime(9, 0), getTime(16, 0)));
        editor.getInput().get(0).setDescription("Description");
        MarketTime[] schedule = editor.getSchedule();
        assertEquals(1, schedule.length);
    }

    public void testSetSchedule() throws Exception {
        TimeScheduleEditor editor = new TimeScheduleEditor(shell);
        editor.setSchedule(new MarketTime[] {
            new MarketTime(getTime(9, 0), getTime(16, 0)),
        });
        assertEquals(1, editor.getViewer().getTable().getItemCount());
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
