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

package org.eclipsetrader.ui.internal.charts;

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
import org.eclipse.swt.widgets.Text;
import org.eclipsetrader.core.feed.TimeSpan;

public class PeriodDialog extends Dialog {

    private Text description;
    private Spinner periodLength;
    private ComboViewer periodUnits;
    private Spinner resolutionLength;
    private ComboViewer resolutionUnits;

    private Period selection;

    public PeriodDialog(Shell parentShell) {
        super(parentShell);
    }

    public PeriodDialog(Shell parentShell, Period selection) {
        super(parentShell);
        this.selection = selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Period");
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
        label.setText("Description");
        description = new Text(composite, SWT.BORDER);
        description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        ((GridData) description.getLayoutData()).widthHint = convertHorizontalDLUsToPixels(250);

        label = new Label(composite, SWT.NONE);
        label.setText("Last");
        periodLength = new Spinner(composite, SWT.BORDER);
        periodLength.setValues(1, 1, 9999, 0, 1, 1);
        periodUnits = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        periodUnits.setContentProvider(new ArrayContentProvider());
        periodUnits.setLabelProvider(new LabelProvider());
        periodUnits.setSorter(new ViewerSorter());
        periodUnits.setInput(new Object[] {
            TimeSpan.Units.Days,
            TimeSpan.Units.Months,
            TimeSpan.Units.Years,
        });
        periodUnits.setSelection(new StructuredSelection(TimeSpan.Units.Years));

        label = new Label(composite, SWT.NONE);
        label.setText("Bar Size");
        resolutionLength = new Spinner(composite, SWT.BORDER);
        resolutionLength.setValues(1, 1, 9999, 0, 1, 1);
        resolutionUnits = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        resolutionUnits.setContentProvider(new ArrayContentProvider());
        resolutionUnits.setLabelProvider(new LabelProvider());
        resolutionUnits.setSorter(new ViewerSorter());
        resolutionUnits.setInput(new Object[] {
            TimeSpan.Units.Minutes,
            TimeSpan.Units.Days,
        });
        resolutionUnits.setSelection(new StructuredSelection(TimeSpan.Units.Days));

        if (selection != null) {
            description.setText(selection.getDescription());
            periodLength.setSelection(selection.getPeriod().getLength());
            periodUnits.setSelection(new StructuredSelection(selection.getPeriod().getUnits()));
            resolutionLength.setSelection(selection.getResolution().getLength());
            resolutionUnits.setSelection(new StructuredSelection(selection.getResolution().getUnits()));
        }

        resolutionUnits.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TimeSpan.Units units = (TimeSpan.Units) ((IStructuredSelection) event.getSelection()).getFirstElement();
                if (units == TimeSpan.Units.Days) {
                    resolutionLength.setValues(1, 1, 1, 0, 1, 1);
                }
                else {
                    resolutionLength.setValues(1, 1, 9999, 0, 1, 1);
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
        TimeSpan period = new TimeSpan(
            (TimeSpan.Units) ((IStructuredSelection) periodUnits.getSelection()).getFirstElement(),
            periodLength.getSelection());

        TimeSpan barSize = new TimeSpan(
            (TimeSpan.Units) ((IStructuredSelection) resolutionUnits.getSelection()).getFirstElement(),
            resolutionLength.getSelection());

        if (selection == null) {
            selection = new Period(description.getText(), period, barSize);
        }
        else {
            selection.setDescription(description.getText());
            selection.setPeriod(period);
            selection.setResolution(barSize);
        }

        super.okPressed();
    }

    public Period getSelection() {
        return selection;
    }
}
