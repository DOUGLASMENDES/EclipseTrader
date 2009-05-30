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

package org.eclipsetrader.ui.charts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SummaryDateItem {
	Label label;
	DateFormat dateFormat;

	public SummaryDateItem(Composite parent, int style) {
		label = new Label(parent, SWT.NONE);

		if ((style & (SWT.DATE | SWT.TIME)) == (SWT.DATE | SWT.TIME))
			dateFormat = SimpleDateFormat.getDateTimeInstance();
		else if ((style & SWT.DATE) == SWT.DATE)
			dateFormat = SimpleDateFormat.getDateInstance();
		else if ((style & SWT.TIME) == SWT.TIME)
			dateFormat = SimpleDateFormat.getTimeInstance();
	}

	public void setDate(Date date) {
		label.setText(date != null ? dateFormat.format(date) : "");
		label.getParent().layout();
	}
}
