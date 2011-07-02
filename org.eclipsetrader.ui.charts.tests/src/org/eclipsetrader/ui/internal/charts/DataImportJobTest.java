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

package org.eclipsetrader.ui.internal.charts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;

public class DataImportJobTest extends TestCase {

    public void testFilterSecuritiesWithoutFeed() throws Exception {
        ISecurity[] security = new ISecurity[] {
                new Security("Test1", null),
                new Security("Test2", new FeedIdentifier("TEST", new FeedProperties())),
        };

        DataImportJob job = new DataImportJob(security, 0, null, null, null);
        ISecurity[] filtered = job.getFilteredSecurities(security);

        assertEquals(1, filtered.length);
    }

    public void testSortFilterSecurities() throws Exception {
        ISecurity[] security = new ISecurity[] {
                new Security("Test2", new FeedIdentifier("TEST", new FeedProperties())),
                new Security("Test1", new FeedIdentifier("TEST", new FeedProperties())),
        };

        DataImportJob job = new DataImportJob(security, 0, null, null, null);
        ISecurity[] filtered = job.getFilteredSecurities(security);

        assertSame(filtered[0], security[1]);
        assertSame(filtered[1], security[0]);
    }

    public void testGetDefaultYearsStartDate() throws Exception {
        PreferenceStore preferences = new PreferenceStore();
        preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD, 1);
        preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS, 5);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.YEAR, -preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS));

        DataImportJob job = new DataImportJob(new Security("Test", null), 0, null, null, null);
        job.preferences = preferences;

        assertEquals(c.getTime(), job.getDefaultStartDate());
    }

    public void testGetDefaultStartDate() throws Exception {
        PreferenceStore preferences = new PreferenceStore();
        preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD, 0);
        preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE, "20010512");

        Date expectedDate = new SimpleDateFormat("yyyyMMdd").parse(preferences.getString(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE));

        DataImportJob job = new DataImportJob(new Security("Test", null), 0, null, null, null);
        job.preferences = preferences;

        assertEquals(expectedDate, job.getDefaultStartDate());
    }
}
