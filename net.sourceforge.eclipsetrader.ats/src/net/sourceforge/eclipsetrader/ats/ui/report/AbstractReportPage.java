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

package net.sourceforge.eclipsetrader.ats.ui.report;

import net.sourceforge.eclipsetrader.ats.core.internal.Backtest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public abstract class AbstractReportPage {
	private String title;

	private FormToolkit toolkit;

	private ScrolledForm form;

	public AbstractReportPage(String title) {
		this.title = title;
	}

	public Control createControls(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText(title);
		GridLayout layout = new GridLayout(4, true);
		layout.horizontalSpacing = 20;
		form.getBody().setLayout(layout);
		form.getForm().setTextBackground(new Color[] { toolkit.getColors().getColor(FormColors.TB_GBG), toolkit.getColors().getColor(FormColors.TB_FG), }, new int[] { 100 }, true);

		return form;
	}

	public abstract void setInput(Backtest test);

	protected Composite createSection(Composite parent, String title) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		section.setText(title);
		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(4, true);
		layout.horizontalSpacing = 20;
		client.setLayout(layout);
		section.setClient(client);
		toolkit.paintBordersFor(client);

		return client;
	}

	protected Label createLabel(Composite parent, String description, String defaultValue) {
		Label label = toolkit.createLabel(parent, description);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		label = toolkit.createLabel(parent, defaultValue);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	public ScrolledForm getForm() {
		return form;
	}

	public FormToolkit getToolkit() {
		return toolkit;
	}
}
