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

package org.eclipsetrader.news.internal.ui;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.internal.repository.HeadLine;

public class HeadLineViewerTest extends TestCase {

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

    public void testCreatePartControl() throws Exception {
        HeadLineViewer view = new HeadLineViewer() {

            @Override
            protected IHeadLine[] getHeadLines() {
                return new IHeadLine[0];
            }

            @Override
            protected void updateTitleImage() {
                // Do nothing
            }
        };
        view.createPartControl(shell);
        assertEquals(0, view.getViewer().getTable().getItemCount());
    }

    public void testSortHeadLines() throws Exception {
        final HeadLine h1 = new HeadLine(getTime(2008, Calendar.MAY, 27, 11, 50), "Source", "Title1", null, "link1");
        final HeadLine h2 = new HeadLine(getTime(2008, Calendar.MAY, 27, 11, 55), "Source", "Title2", null, "link2");
        HeadLineViewer view = new HeadLineViewer() {

            @Override
            protected IHeadLine[] getHeadLines() {
                return new IHeadLine[] {
                    h1, h2,
                };
            }

            @Override
            protected void updateTitleImage() {
                // Do nothing
            }
        };
        view.createPartControl(shell);
        assertEquals(2, view.getViewer().getTable().getItemCount());
        assertSame(h2, view.getViewer().getTable().getItem(0).getData());
        assertSame(h1, view.getViewer().getTable().getItem(1).getData());
    }

    public void testDisplayRecentNewsFirst() throws Exception {
        final HeadLine h1 = new HeadLine(getTime(2008, Calendar.MAY, 27, 11, 50), "Source", "Title1", null, "link1");
        h1.setRecent(true);
        final HeadLine h2 = new HeadLine(getTime(2008, Calendar.MAY, 27, 11, 55), "Source", "Title2", null, "link2");
        h2.setRecent(false);
        HeadLineViewer view = new HeadLineViewer() {

            @Override
            protected IHeadLine[] getHeadLines() {
                return new IHeadLine[] {
                    h1, h2,
                };
            }

            @Override
            protected void updateTitleImage() {
                // Do nothing
            }
        };
        view.createPartControl(shell);
        assertEquals(2, view.getViewer().getTable().getItemCount());
        assertSame(h1, view.getViewer().getTable().getItem(0).getData());
        assertSame(h2, view.getViewer().getTable().getItem(1).getData());
    }

    private Date getTime(int year, int month, int day, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day, hour, minute, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }
}
