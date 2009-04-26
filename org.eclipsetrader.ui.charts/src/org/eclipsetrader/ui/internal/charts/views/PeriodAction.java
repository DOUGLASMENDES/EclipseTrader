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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.jface.action.Action;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.TimeSpan;

public class PeriodAction extends Action {
	private ChartViewPart view;
	private TimeSpan period;
	private TimeSpan resolution;

	public PeriodAction(ChartViewPart view, String text, TimeSpan period, TimeSpan resolution) {
		super(text, Action.AS_RADIO_BUTTON);

		this.view = view;
		this.period = period;
		this.resolution = resolution;
	}

	public PeriodAction(ChartViewPart view, TimeSpan period, TimeSpan resolution) {
		super("", Action.AS_RADIO_BUTTON);

		this.view = view;
		this.period = period;
		this.resolution = resolution;

		String unit = "";
		switch (period.getUnits()) {
			case Days:
				unit = "Day(s)";
				break;
			case Months:
				unit = "Month(s)";
				break;
			case Years:
				unit = "Year(s)";
				break;
		}
		setText(NLS.bind("{0} {1}", new Object[] { String.valueOf(period.getLength()), unit }));
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
	    view.setPeriod(period, resolution);
    }

	public TimeSpan getPeriod() {
    	return period;
    }

	public TimeSpan getResolution() {
    	return resolution;
    }
}
