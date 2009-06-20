/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
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
import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DefaultsPageTest extends TestCase {
	Shell shell;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getDefault());
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	public void testDefaultBackfillMethodSelection() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertTrue(page.useStartDate.getSelection());
		assertFalse(page.useYears.getSelection());
	}

	public void testDefaultStartDateSelection() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertNull(page.startDate.getSelection());
	}

	public void testDefaultYearsSelection() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertEquals(1, page.years.getSelection());
	}

	public void testSelectBackfillMethodFromPreferences() throws Exception {
		PreferenceStore preferences = new PreferenceStore();
		preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD, 1);

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertFalse(page.useStartDate.getSelection());
		assertTrue(page.useYears.getSelection());
	}

	public void testSelectStartDateFromPreferences() throws Exception {
		PreferenceStore preferences = new PreferenceStore();
		preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE, "20090620");

		Date expectedDate = new SimpleDateFormat("yyyyMMdd").parse(preferences.getString(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE));

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertEquals(expectedDate, page.startDate.getSelection());
	}

	public void testSelectYearsFromPreferences() throws Exception {
		PreferenceStore preferences = new PreferenceStore();
		preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS, 5);

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.performDefaults();

		assertEquals(5, page.years.getSelection());
	}

	public void testSaveStartDateBackfillMethod() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.useStartDate.setSelection(true);
		page.useYears.setSelection(false);
		page.performOk();

		assertEquals(0, preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD));
	}

	public void testSaveYearsBackfillMethod() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.useStartDate.setSelection(false);
		page.useYears.setSelection(true);
		page.performOk();

		assertEquals(1, preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD));
	}

	public void testSaveStartDate() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.startDate.setSelection(new SimpleDateFormat("yyyyMMdd").parse("20090620"));
		page.performOk();

		assertEquals("20090620", preferences.getString(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE));
	}

	public void testSaveYears() throws Exception {
		PreferenceStore preferences = new PreferenceStore();

		DefaultsPage page = new DefaultsPage();
		page.setPreferenceStore(preferences);
		page.createContents(shell);

		page.years.setSelection(5);
		page.performOk();

		assertEquals(5, preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS));
	}
}
