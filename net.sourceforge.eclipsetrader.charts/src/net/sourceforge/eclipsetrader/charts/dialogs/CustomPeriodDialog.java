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
    private Date beginDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
    private SimpleDateFormat dateParse = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$

    public CustomPeriodDialog(Date beginDate, Date endDate)
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
        if (beginDate != null)
            begin.setText(dateFormat.format(beginDate));
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
        if (endDate != null)
            end.setText(dateFormat.format(endDate));
        end.setLayoutData(new GridData(80, SWT.DEFAULT));
        end.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                reformatDate(end);
            }
        });

        return super.createDialogArea(parent);
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
        try
        {
            beginDate = dateParse.parse(begin.getText());
            endDate = dateParse.parse(end.getText());
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
        super.okPressed();
    }

    public Date getBeginDate()
    {
        return beginDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }
}
