/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
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
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SummaryDateItem {

    Label label;
    DateFormat dateFormat;

    public SummaryDateItem(Composite parent, int style) {
        label = new Label(parent, SWT.NONE);

        if ((style & (SWT.DATE | SWT.TIME)) == (SWT.DATE | SWT.TIME)) {
            dateFormat = DateFormat.getDateTimeInstance();
        }
        else if ((style & SWT.DATE) == SWT.DATE) {
            dateFormat = DateFormat.getDateInstance();
        }
        else if ((style & SWT.TIME) == SWT.TIME) {
            dateFormat = DateFormat.getTimeInstance();
        }
    }

    public void setDate(Date date) {
        label.setText(date != null ? dateFormat.format(date) : ""); //$NON-NLS-1$
        label.getParent().layout();
    }
}
