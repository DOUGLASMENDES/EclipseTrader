/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts.dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ui.PlatformUI;

public class CustomPeriodDialog extends Dialog
{
    private Text begin;
    private Text end;
    private String beginDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private SimpleDateFormat dateParse = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$

    public CustomPeriodDialog(String beginDate, String endDate)
    {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Custom Period");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Begin Date");
        label.setLayoutData(new GridData(80, SWT.DEFAULT));
        begin = new Text(content, SWT.BORDER);
        begin.setText(beginDate);
        begin.setLayoutData(new GridData(80, SWT.DEFAULT));
        begin.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                reformatDate(begin);
            }
        });
        
        label = new Label(content, SWT.NONE);
        label.setText("End Date");
        label.setLayoutData(new GridData(80, SWT.DEFAULT));
        end = new Text(content, SWT.BORDER);
        end.setText(endDate);
        end.setLayoutData(new GridData(80, SWT.DEFAULT));
        end.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                reformatDate(end);
            }
        });

        return super.createDialogArea(parent);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected Control createButtonBar(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER|GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());
        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    private void reformatDate(Text text)
    {
        if (text.getText().length() != 0)
            try
            {
                Date date = dateParse.parse(text.getText());
                text.setText(dateFormat.format(date));
            }
            catch (ParseException e) {
                CorePlugin.logException(e);
            }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        beginDate = begin.getText();
        endDate = end.getText();
        super.okPressed();
    }

    public String getBeginDate()
    {
        return beginDate;
    }

    public String getEndDate()
    {
        return endDate;
    }
}
