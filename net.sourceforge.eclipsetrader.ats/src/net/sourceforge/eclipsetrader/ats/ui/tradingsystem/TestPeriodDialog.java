/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.ats.ui.tradingsystem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TestPeriodDialog extends Dialog {
	Text begin;

	Text end;

	static private Date beginDate, endDate;

	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

	SimpleDateFormat dateParse = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$

	public TestPeriodDialog(Shell parentShell) {
		super(parentShell);

		if (beginDate == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.add(Calendar.YEAR, -1);
			beginDate = calendar.getTime();
		}
		if (endDate == null)
			endDate = Calendar.getInstance().getTime();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Backtest Period");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite content = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		content.setLayout(gridLayout);
		content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label label = new Label(content, SWT.NONE);
		label.setText("Begin Date");
		begin = new Text(content, SWT.BORDER);
		begin.setLayoutData(new GridData(70, SWT.DEFAULT));
		begin.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				Text text = (Text) e.widget;
				try {
					Date date = dateParse.parse(text.getText());
					text.setText(dateFormat.format(date));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		label = new Label(content, SWT.NONE);
		label.setText("End Date");
		end = new Text(content, SWT.BORDER);
		end.setLayoutData(new GridData(70, SWT.DEFAULT));
		end.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				Text text = (Text) e.widget;
				try {
					Date date = dateParse.parse(text.getText());
					text.setText(dateFormat.format(date));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		begin.setText(dateFormat.format(beginDate));
		end.setText(dateFormat.format(endDate));

		return content.getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		try {
			beginDate = dateParse.parse(begin.getText());
			endDate = dateParse.parse(end.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.okPressed();
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
