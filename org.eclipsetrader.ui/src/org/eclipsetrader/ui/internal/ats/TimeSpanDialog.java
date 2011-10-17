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

package org.eclipsetrader.ui.internal.ats;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipsetrader.core.feed.TimeSpan;

public class TimeSpanDialog extends Dialog {

    private ComboViewer units;
    private Spinner value;

    private TimeSpan selection;

    public TimeSpanDialog(Shell parentShell) {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Time Span");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Value");
        value = new Spinner(composite, SWT.BORDER);
        value.setValues(1, 1, 9999, 0, 1, 10);
        units = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        units.setContentProvider(new ArrayContentProvider());
        units.setLabelProvider(new LabelProvider());
        units.setSorter(new ViewerSorter());
        units.setInput(new Object[] {
            TimeSpan.Units.Minutes,
            TimeSpan.Units.Days
        });
        units.setSelection(new StructuredSelection(TimeSpan.Units.Minutes));

        units.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TimeSpan.Units units = (TimeSpan.Units) ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (units == TimeSpan.Units.Days) {
                    value.setValues(1, 1, 1, 0, 1, 10);
                }
                else {
                    value.setValues(1, 1, 9999, 0, 1, 10);
                }
            }
        });

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) units.getSelection();
        TimeSpan.Units unit = (TimeSpan.Units) selection.getFirstElement();
        this.selection = new TimeSpan(unit, value.getSelection());
        super.okPressed();
    }

    public TimeSpan getSelection() {
        return selection;
    }
}
