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

package org.eclipsetrader.ui.internal.markets;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipsetrader.core.internal.markets.Market;

public class GeneralPage extends PropertyPage {
	private TimeScheduleEditor schedule;
	private ExclusionScheduleEditor excluded;
	private Button sun;
	private Button mon;
	private Button tue;
	private Button wed;
	private Button thu;
	private Button fri;
	private Button sat;
	private ComboViewer timeZone;

	public GeneralPage() {
		setTitle("General");
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		initializeDialogUnits(content);

		Label label = new Label(content, SWT.NONE);
		label.setText("Schedule");
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		schedule = new TimeScheduleEditor(content);
		((GridData) schedule.getControl().getLayoutData()).horizontalSpan = 2;
		schedule.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            	if (selection.size() == 1) {
            		MarketTimeElement element = (MarketTimeElement) selection.getFirstElement();
            		excluded.setInput(element.getExclude());
            	}
            }
		});

		label = new Label(content, SWT.NONE);
		label.setText("Excluded Days");
		label.setLayoutData(new GridData(convertHorizontalDLUsToPixels(80), SWT.DEFAULT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		excluded = new ExclusionScheduleEditor(content);
		((GridData) excluded.getControl().getLayoutData()).horizontalSpan = 2;

        label = new Label(content, SWT.NONE);
        label.setText("Week Days");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        ((GridData) label.getLayoutData()).verticalIndent = convertHorizontalDLUsToPixels(3);
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(5, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        mon = new Button(group, SWT.CHECK);
        mon.setText("Mon");
        tue = new Button(group, SWT.CHECK);
        tue.setText("Tue");
        wed = new Button(group, SWT.CHECK);
        wed.setText("Wed");
        thu = new Button(group, SWT.CHECK);
        thu.setText("Thu");
        fri = new Button(group, SWT.CHECK);
        fri.setText("Fri");
        sun = new Button(group, SWT.CHECK);
        sun.setText("Sun");
        sat = new Button(group, SWT.CHECK);
        sat.setText("Sat");

		label = new Label(content, SWT.NONE);
		label.setText("Time Zone");
		timeZone = new ComboViewer(content, SWT.READ_ONLY);
		timeZone.getCombo().setVisibleItemCount(15);
		timeZone.setLabelProvider(new LabelProvider());
		timeZone.setSorter(new ViewerSorter());
		timeZone.setContentProvider(new ArrayContentProvider());
		timeZone.setInput(TimeZone.getAvailableIDs());

		if (getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				schedule.setSchedule(market.getSchedule());

				Set<Integer> weekdays = new HashSet<Integer>(Arrays.asList(market.getWeekDays()));
				sun.setSelection(weekdays.contains(Calendar.SUNDAY));
				mon.setSelection(weekdays.contains(Calendar.MONDAY));
				tue.setSelection(weekdays.contains(Calendar.TUESDAY));
				wed.setSelection(weekdays.contains(Calendar.WEDNESDAY));
				thu.setSelection(weekdays.contains(Calendar.THURSDAY));
				fri.setSelection(weekdays.contains(Calendar.FRIDAY));
				sat.setSelection(weekdays.contains(Calendar.SATURDAY));

				if (market.getTimeZone() != null)
					timeZone.setSelection(new StructuredSelection(market.getTimeZone().getID()));
			}
		}

		return content;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#isValid()
     */
    @Override
    public boolean isValid() {
    	if (!sun.getSelection() && !mon.getSelection() && !tue.getSelection() && !wed.getSelection() && !thu.getSelection() && !fri.getSelection() && !sat.getSelection())
    		return false;
	    return true;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
		if (isControlCreated() && getElement() != null) {
			Market market = (Market) getElement().getAdapter(Market.class);
			if (market != null) {
				market.setSchedule(schedule.getSchedule());

				Set<Integer> weekdays = new HashSet<Integer>();
				if (sun.getSelection())
					weekdays.add(Calendar.SUNDAY);
				if (mon.getSelection())
					weekdays.add(Calendar.MONDAY);
				if (tue.getSelection())
					weekdays.add(Calendar.TUESDAY);
				if (wed.getSelection())
					weekdays.add(Calendar.WEDNESDAY);
				if (thu.getSelection())
					weekdays.add(Calendar.THURSDAY);
				if (fri.getSelection())
					weekdays.add(Calendar.FRIDAY);
				if (sat.getSelection())
					weekdays.add(Calendar.SATURDAY);
				market.setWeekDays(weekdays.toArray(new Integer[weekdays.size()]));

				IStructuredSelection selection = (IStructuredSelection) timeZone.getSelection();
				market.setTimeZone(selection.isEmpty() ? null : TimeZone.getTimeZone((String) selection.getFirstElement()));
			}
		}
	    return super.performOk();
    }
}
