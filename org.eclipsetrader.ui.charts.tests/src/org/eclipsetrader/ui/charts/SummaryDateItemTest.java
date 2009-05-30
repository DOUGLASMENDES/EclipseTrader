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

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SummaryDateItemTest extends TestCase {
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

	public void testConstructor() throws Exception {
		SummaryDateItem control = new SummaryDateItem(shell, SWT.DATE);
		assertEquals("", control.label.getText());
	}

	public void testConstructorSetsDateFormat() throws Exception {
		SummaryDateItem control = new SummaryDateItem(shell, SWT.DATE);
		assertNotNull(control.dateFormat);
	}

	public void testSetDate() throws Exception {
		SummaryDateItem control = new SummaryDateItem(shell, SWT.DATE);
		control.setDate(new Date());
		assertEquals(control.dateFormat.format(new Date()), control.label.getText());
	}

	public void testSetNullDate() throws Exception {
		SummaryDateItem control = new SummaryDateItem(shell, SWT.DATE);
		control.setDate(null);
		assertEquals("", control.label.getText());
	}

	public void testNullDateBlacksLabel() throws Exception {
		SummaryDateItem control = new SummaryDateItem(shell, SWT.DATE);
		control.setDate(new Date());
		control.setDate(null);
		assertEquals("", control.label.getText());
	}
}
