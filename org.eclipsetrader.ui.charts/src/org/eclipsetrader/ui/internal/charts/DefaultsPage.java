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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DefaultsPage extends PreferencePage implements IWorkbenchPreferencePage {
	Button useStartDate;
	CDateTime startDate;
	Button useYears;
	Spinner years;

	SimpleDateFormat prefsDateFormat = new SimpleDateFormat("yyyyMMdd");

	public DefaultsPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(ChartsUIActivator.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = convertVerticalDLUsToPixels(2);
		content.setLayout(gridLayout);

		Group group = new Group(content, SWT.BORDER);
		group.setText("Initial backfill");
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		gridLayout = new GridLayout(3, false);
		gridLayout.verticalSpacing = convertVerticalDLUsToPixels(2);
		group.setLayout(gridLayout);

		useStartDate = new Button(group, SWT.RADIO);
		useStartDate.setText("Start Date");
		startDate = new CDateTime(group, CDT.DATE_SHORT | CDT.BORDER);
		startDate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		((GridData) startDate.getLayoutData()).widthHint = convertHorizontalDLUsToPixels(62);

		useYears = new Button(group, SWT.RADIO);
		useYears.setText("Last");
		years = new Spinner(group, SWT.BORDER);
		years.setValues(1, 1, 99999, 0, 1, 5);
		Label label = new Label(group, SWT.NONE);
		label.setText("year(s)");

		performDefaults();

		return content;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore preferences = getPreferenceStore();

		int v = preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD);
		useStartDate.setSelection(v == 0);
		useYears.setSelection(v == 1);

		String s = preferences.getString(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE);
		if (!s.equals("")) {
			try {
				Date date = prefsDateFormat.parse(s);
				startDate.setSelection(date);
			} catch (ParseException e) {
				Status status = new Status(Status.ERROR, ChartsUIActivator.PLUGIN_ID, 0, "Error parsing start date " + s, e);
				ChartsUIActivator.log(status);
			}
		}

		years.setSelection(preferences.getInt(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS));

		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore preferences = getPreferenceStore();

		if (useStartDate.getSelection())
			preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD, 0);
		else if (useYears.getSelection())
			preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_METHOD, 1);

		if (startDate.getSelection() != null)
			preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE, prefsDateFormat.format(startDate.getSelection()));
		else
			preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_START_DATE, "");

		preferences.setValue(ChartsUIActivator.PREFS_INITIAL_BACKFILL_YEARS, years.getSelection());

		return super.performOk();
	}
}
