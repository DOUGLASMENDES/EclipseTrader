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
import org.eclipsetrader.core.feed.OHLC;

public class SummaryOHLCItemTest extends TestCase {
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
		SummaryOHLCItem control = new SummaryOHLCItem(shell, SWT.DATE);
		assertEquals("", control.label.getText());
		assertEquals("", control.changeLabel.getText());
	}

	public void testSetOHLC() throws Exception {
		SummaryOHLCItem control = new SummaryOHLCItem(shell, SWT.DATE);
		control.setOHLC(new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 100L), new OHLC(new Date(), 5.0, 6.0, 7.0, 8.0, 100L));
		assertEquals("O=1 H=2 L=3 C=4", control.label.getText());
		assertEquals(control.percentFormat.format(-50.0) + "%", control.changeLabel.getText());
	}

	public void testSetNullValues() throws Exception {
		SummaryOHLCItem control = new SummaryOHLCItem(shell, SWT.DATE);
		control.setOHLC(null, null);
		assertEquals("", control.label.getText());
		assertEquals("", control.changeLabel.getText());
	}

	public void testSetNullOHLC() throws Exception {
		SummaryOHLCItem control = new SummaryOHLCItem(shell, SWT.DATE);
		control.setOHLC(null, new OHLC(new Date(), 5.0, 6.0, 7.0, 8.0, 100L));
		assertEquals("", control.label.getText());
		assertEquals("", control.changeLabel.getText());
	}

	public void testSetNullPreviousOHLC() throws Exception {
		SummaryOHLCItem control = new SummaryOHLCItem(shell, SWT.DATE);
		control.setOHLC(new OHLC(new Date(), 1.0, 2.0, 3.0, 4.0, 100L), null);
		assertEquals("O=1 H=2 L=3 C=4", control.label.getText());
		assertEquals("", control.changeLabel.getText());
	}
}
