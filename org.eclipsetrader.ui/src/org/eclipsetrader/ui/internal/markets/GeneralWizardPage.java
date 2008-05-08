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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.internal.markets.MarketTime;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class GeneralWizardPage extends WizardPage {
	Text name;
	private TimeScheduleEditor schedule;
    Button sun;
    Button mon;
    Button tue;
    Button wed;
    Button thu;
    Button fri;
    Button sat;
    ComboViewer timeZone;

	public GeneralWizardPage() {
		super("general", "General", null);
		setDescription("Set the market's name and time schedule");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		setControl(content);

		initializeDialogUnits(parent);

		Label label = new Label(content, SWT.NONE);
		label.setText("Name");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		name = new Text(content, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		name.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
    			getContainer().updateButtons();
            }
		});

		label = new Label(content, SWT.NONE);
		label.setText("Schedule");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		schedule = new TimeScheduleEditor(content);
		((GridData) schedule.getControl().getLayoutData()).horizontalSpan = 2;
		((GridData) schedule.getControl().getLayoutData()).grabExcessVerticalSpace = true;
		((GridData) schedule.getControl().getLayoutData()).verticalAlignment = SWT.FILL;
		schedule.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
            	setPageComplete(isPageComplete());
            }
		});
		schedule.getInput().add(new MarketTimeElement(getTime(9, 0), getTime(16, 0)));
		schedule.getViewer().refresh();

        label = new Label(content, SWT.NONE);
        label.setText("Week Days");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        ((GridData) label.getLayoutData()).widthHint = convertHorizontalDLUsToPixels(80);
        ((GridData) label.getLayoutData()).verticalIndent = convertHorizontalDLUsToPixels(3);
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(5, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        mon = new Button(group, SWT.CHECK);
        mon.setText("Mon");
        mon.setSelection(true);
        tue = new Button(group, SWT.CHECK);
        tue.setText("Tue");
        tue.setSelection(true);
        wed = new Button(group, SWT.CHECK);
        wed.setText("Wed");
        wed.setSelection(true);
        thu = new Button(group, SWT.CHECK);
        thu.setText("Thu");
        thu.setSelection(true);
        fri = new Button(group, SWT.CHECK);
        fri.setText("Fri");
        fri.setSelection(true);
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
		timeZone.setSelection(new StructuredSelection(TimeZone.getDefault().getID()));

		name.setFocus();
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
    	if (name.getText().equals(""))
    		return false;

    	if (getMarketService().getMarket(name.getText()) != null) {
    		setErrorMessage("Another market with the same name exists. Choose another name.");
    		return false;
    	}

    	if (getErrorMessage() != null)
    		setErrorMessage(null);

    	if (schedule.getInput().size() == 0)
    		return false;
    	if (!sun.getSelection() && !mon.getSelection() && !tue.getSelection() && !wed.getSelection() && !thu.getSelection() && !fri.getSelection() && !sat.getSelection())
    		return false;

    	return true;
    }

	public String getMarketName() {
    	return name.getText();
    }

	public MarketTime[] getSchedule() {
		return schedule.getSchedule();
	}

	public Integer[] getWeekDays() {
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
		return weekdays.toArray(new Integer[weekdays.size()]);
	}

	public TimeZone getTimeZone() {
		if (!timeZone.getSelection().isEmpty()) {
			String id = ((StructuredSelection) timeZone.getSelection()).getFirstElement().toString();
			return TimeZone.getTimeZone(id);
		}
    	return null;
    }

    protected MarketService getMarketService() {
    	try {
    		BundleContext context = UIActivator.getDefault().getBundle().getBundleContext();
    		ServiceReference serviceReference = context.getServiceReference(MarketService.class.getName());
    		MarketService service = (MarketService) context.getService(serviceReference);
    		context.ungetService(serviceReference);
    		return service;
    	} catch(Exception e) {
    		Status status = new Status(Status.ERROR, UIActivator.PLUGIN_ID, 0, "Error reading market service", e);
    		UIActivator.getDefault().getLog().log(status);
    	}
    	return null;
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
