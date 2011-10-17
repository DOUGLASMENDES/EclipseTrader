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

package org.eclipsetrader.ui.internal.charts.views;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.History;
import org.eclipsetrader.core.feed.IHistory;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.ui.Helper;

public class ChartLoadJobTest extends TestCase {

    public void testLoadDailyHistory() throws Exception {
        final History originalHistory = new History(null, Helper.dailyHistory(30));

        ChartLoadJob job = new ChartLoadJob(null) {

            @Override
            IHistory getHistoryFor(ISecurity security) {
                return originalHistory;
            }
        };

        job.buildHistory();

        assertSame(originalHistory, job.history);
        assertSame(originalHistory, job.subsetHistory);
    }

    public void testLoadDailySubsetHistory() throws Exception {
        final History originalHistory = new History(null, Helper.dailyHistory(60));

        ChartLoadJob job = new ChartLoadJob(null) {

            @Override
            IHistory getHistoryFor(ISecurity security) {
                return originalHistory;
            }
        };
        job.setTimeSpan(TimeSpan.days(30));
        job.setResolutionTimeSpan(TimeSpan.days(1));

        job.buildHistory();

        assertSame(originalHistory, job.history);
        assertNotSame(originalHistory, job.subsetHistory);
        assertEquals(30, job.subsetHistory.getOHLC().length);
    }
}
