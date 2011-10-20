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

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DataLoadPanel {

    Composite control;
    Label label;

    public DataLoadPanel(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        FillLayout layout = new FillLayout();
        layout.marginWidth = layout.marginHeight = 5;
        control.setLayout(layout);

        label = new Label(control, SWT.NONE);
        label.setText("Loading...");
    }

    public Composite getControl() {
        return control;
    }

    public void dispose() {
        control.dispose();
    }
}
