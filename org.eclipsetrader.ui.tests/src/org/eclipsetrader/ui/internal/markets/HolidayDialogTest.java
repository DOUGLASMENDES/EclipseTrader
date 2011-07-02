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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class HolidayDialogTest extends TestCase {

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

    public void testIsValidWithDefaultFields() throws Exception {
        HolidayDialog dlg = new HolidayDialog(shell, null);
        dlg.createContents(shell);
        assertFalse(dlg.isValid());
        dlg.description.setText("Description");
        assertTrue(dlg.isValid());
    }

    public void testIsValidWithOpenAndCloseTime() throws Exception {
        HolidayDialog dlg = new HolidayDialog(shell, null);
        dlg.createContents(shell);
        dlg.description.setText("Description");
        assertTrue(dlg.isValid());
        dlg.closed.setSelection(false);
        dlg.open.setSelection(true);
        assertFalse(dlg.isValid());
        dlg.openTime.setSelection(getTime(9, 30));
        assertFalse(dlg.isValid());
        dlg.closeTime.setSelection(getTime(16, 0));
        assertTrue(dlg.isValid());
    }

    public void testGetElement() throws Exception {
        HolidayDialog dlg = new HolidayDialog(shell, null);
        dlg.createContents(shell);
        dlg.description.setText("Description");
        dlg.commitChanges();
        MarketHolidayElement element = dlg.getElement();
        assertNotNull(element);
        assertEquals(dlg.date.getSelection(), element.getDate());
        assertEquals(dlg.description.getText(), element.getDescription());
        assertNull(element.getOpenTime());
        assertNull(element.getCloseTime());
    }

    public void testGetElementWithOpenAndCloseTime() throws Exception {
        HolidayDialog dlg = new HolidayDialog(shell, null);
        dlg.createContents(shell);
        dlg.description.setText("Description");
        dlg.closed.setSelection(false);
        dlg.open.setSelection(true);
        dlg.openTime.setSelection(getTime(9, 30));
        dlg.closeTime.setSelection(getTime(16, 0));
        dlg.commitChanges();
        MarketHolidayElement element = dlg.getElement();
        assertNotNull(element);
        assertEquals(dlg.date.getSelection(), element.getDate());
        assertEquals(dlg.description.getText(), element.getDescription());
        assertEquals(dlg.openTime.getSelection(), element.getOpenTime());
        assertEquals(dlg.closeTime.getSelection(), element.getCloseTime());
    }

    private Date getTime(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        return date.getTime();
    }
}
