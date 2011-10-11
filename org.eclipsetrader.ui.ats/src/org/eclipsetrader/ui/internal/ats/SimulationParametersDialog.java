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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.ui.Util;

public class SimulationParametersDialog extends Dialog {

    public static final String K_BEGIN_DATE = "BEGIN_DATE";
    public static final String K_END_DATE = "END_DATE";
    public static final String TODAY = "Today";

    private CDateTime begin;
    private CDateTime end;

    private Date beginDate;
    private Date endDate;

    private final IDialogSettings rootDialogSettings;

    public SimulationParametersDialog(Shell parentShell) {
        super(parentShell);

        rootDialogSettings = Activator.getDefault().getDialogSettings();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Run Simulation");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Begin");
        begin = new CDateTime(composite, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        begin.setPattern(Util.getDateFormatPattern());
        begin.setLayoutData(new GridData(convertHorizontalDLUsToPixels("gtk".equals(SWT.getPlatform()) ? 80 : 65), SWT.DEFAULT));
        label = new Label(composite, SWT.NONE);
        label.setText("End");
        end = new CDateTime(composite, CDT.BORDER | CDT.DATE_SHORT | CDT.DROP_DOWN | CDT.TAB_FIELDS);
        end.setPattern(Util.getDateFormatPattern());
        end.setNullText(TODAY);
        end.setLayoutData(new GridData(convertHorizontalDLUsToPixels("gtk".equals(SWT.getPlatform()) ? 80 : 65), SWT.DEFAULT));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -30);
        begin.setSelection(calendar.getTime());

        IDialogSettings dialogSettings = rootDialogSettings.getSection(getClass().getName());
        if (dialogSettings != null) {
            if (dialogSettings.get(K_BEGIN_DATE) != null) {
                calendar.setTimeInMillis(dialogSettings.getLong(K_BEGIN_DATE));
                begin.setSelection(calendar.getTime());
            }
            if (dialogSettings.get(K_END_DATE) != null && !TODAY.equals(dialogSettings.get(K_END_DATE))) {
                calendar.setTimeInMillis(dialogSettings.getLong(K_END_DATE));
                end.setSelection(calendar.getTime());
            }
        }

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        beginDate = begin.getSelection();
        endDate = end.getSelection();

        IDialogSettings dialogSettings = rootDialogSettings.getSection(getClass().getName());
        if (dialogSettings == null) {
            dialogSettings = rootDialogSettings.addNewSection(getClass().getName());
        }
        dialogSettings.put(K_BEGIN_DATE, beginDate.getTime());
        if (endDate == null) {
            dialogSettings.put(K_END_DATE, TODAY);
            endDate = Calendar.getInstance().getTime();
        }
        else {
            dialogSettings.put(K_END_DATE, endDate.getTime());
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
