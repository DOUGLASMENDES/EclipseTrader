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

import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.feed.TimeSpan;

public class CustomPeriodDialog extends Dialog {
	private CDateTime from;
	private CDateTime to;
	private ComboViewer resolution;

	private Date firstDate;
	private Date lastDate;
	private TimeSpan resolutionTimeSpan;

	public CustomPeriodDialog(Shell parentShell, Date firstDate, Date lastDate) {
		super(parentShell);
		this.firstDate = firstDate;
		this.lastDate = lastDate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
    protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Custom Period");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label label = new Label(content, SWT.NONE);
		label.setText("Begin Date");
		from = new CDateTime(content, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN);

		label = new Label(content, SWT.NONE);
		label.setText("End Date");
		to = new CDateTime(content, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN);

		label = new Label(content, SWT.NONE);
		label.setText("Resolution");
		resolution = new ComboViewer(content, SWT.DROP_DOWN | SWT.READ_ONLY);
		resolution.setLabelProvider(new LabelProvider());
		resolution.setContentProvider(new ArrayContentProvider());
		resolution.setSorter(new ViewerSorter());
		resolution.setInput(new Object[] {
				TimeSpan.days(1),
				TimeSpan.minutes(1),
				TimeSpan.minutes(5),
				TimeSpan.minutes(10),
				TimeSpan.minutes(15),
				TimeSpan.minutes(30),
				TimeSpan.minutes(60),
			});

		from.setSelection(firstDate);
		to.setSelection(lastDate);
		resolution.setSelection(new StructuredSelection(resolutionTimeSpan));

		return content;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
    protected void okPressed() {
		firstDate = from.getSelection();
		lastDate = to.getSelection();
		resolutionTimeSpan = (TimeSpan) ((IStructuredSelection) resolution.getSelection()).getFirstElement();
		super.okPressed();
	}

	public Date getFirstDate() {
		return firstDate;
	}

	public Date getLastDate() {
		return lastDate;
	}
}
