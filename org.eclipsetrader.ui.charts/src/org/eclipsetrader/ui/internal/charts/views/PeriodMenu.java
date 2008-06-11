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

package org.eclipsetrader.ui.internal.charts.views;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.feed.TimeSpan;

public class PeriodMenu extends MenuManager {
	private Shell shell;

	private Action allAction;

	private TimeSpan[] timeSpan;
	private Action[] actions;

	private Action customAction;
	private Date firstDate;
	private Date lastDate;

	public PeriodMenu(Shell parentShell, TimeSpan[] timeSpan) {
		super("Period");

		this.shell = parentShell;
		this.timeSpan = timeSpan;

		allAction = new Action("All", Action.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                selectionChanged(null);
            }
		};
		add(allAction);

		add(new Separator());

		actions = new Action[timeSpan.length];
		for (int i = 0; i < timeSpan.length; i++) {
			String unit = "";
			switch(timeSpan[i].getUnits()) {
				case Months:
					unit = "Month(s)";
					break;
				case Years:
					unit = "Year(s)";
					break;
			}
			String text = NLS.bind("{0} {1}", new Object[] { String.valueOf(timeSpan[i].getLength()), unit });
			final TimeSpan actionTimeSpan = timeSpan[i];
			actions[i] = new Action(text, Action.AS_RADIO_BUTTON) {
                @Override
                public void run() {
	                selectionChanged(actionTimeSpan);
                }
			};
			add(actions[i]);
		}

		add(new Separator());

    	Calendar c = Calendar.getInstance();
    	lastDate = c.getTime();
    	c.add(Calendar.YEAR, -2);
    	firstDate = c.getTime();

    	customAction = new Action("Custom...", Action.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                CustomPeriodDialog dlg = new CustomPeriodDialog(shell, firstDate, lastDate);
                if (dlg.open() == Dialog.OK)
                	customPeriodSelection(dlg.getFirstDate(), dlg.getLastDate());
            }
		};
		add(customAction);
	}

	protected void selectionChanged(TimeSpan selection) {
		setSelection(selection);
	}

	protected void customPeriodSelection(Date beginDate, Date endDate) {
		setCustomSelection(beginDate, endDate);
    }

	public void setSelection(TimeSpan selection) {
		allAction.setChecked(selection == null);
		for (int i = 0; i < actions.length; i++)
			actions[i].setChecked(timeSpan[i].equals(selection));
		customAction.setChecked(false);
	}

	public void setCustomSelection(Date firstDate, Date lastDate) {
		this.firstDate = firstDate;
		this.lastDate = lastDate;

		allAction.setChecked(false);
		for (int i = 0; i < actions.length; i++)
			actions[i].setChecked(timeSpan[i].equals(false));
		customAction.setChecked(true);
	}

	public TimeSpan getTimeSpan() {
		for (int i = 0; i < actions.length; i++) {
			if (actions[i].isChecked())
				return timeSpan[i];
		}
    	return null;
    }

	public boolean isCustomPeriod() {
		return customAction.isChecked();
	}

	public Date getFirstDate() {
    	return firstDate;
    }

	public Date getLastDate() {
    	return lastDate;
    }
}
