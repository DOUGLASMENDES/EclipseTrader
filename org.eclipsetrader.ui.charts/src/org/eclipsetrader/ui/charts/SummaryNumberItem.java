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

import java.text.NumberFormat;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SummaryNumberItem {
    private Label label;

    private Color color;
    private NumberFormat numberFormat = NumberFormat.getInstance();

	public SummaryNumberItem(Composite parent, int style) {
		label = new Label(parent, SWT.NONE);
		label.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (color != null)
					color.dispose();
			}
		});

		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(4);
		numberFormat.setGroupingUsed(true);
	}

	public void setValue(String text, Number number) {
		label.setText(NLS.bind(Messages.SummaryNumberItem_Label, new Object[] {
				text,
				number != null ? numberFormat.format(number) : "", //$NON-NLS-1$
		}));
		label.getParent().layout();
	}

	public void setForeground(Color color) {
		label.setForeground(color);
	}

	public void setBackground(Color color) {
		label.setBackground(color);
	}

	public void setForeground(RGB rgb) {
		if (color != null)
			color.dispose();
		color = new Color(label.getDisplay(), rgb);
		label.setForeground(color);
	}
}
