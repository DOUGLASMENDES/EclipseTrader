/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.dialogs;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.History;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TestPeriodDialog extends Dialog
{
    private Combo begin;
    private Combo end;
    private History history;
    static private Date beginDate, endDate;

    public TestPeriodDialog(Shell parentShell, History history)
    {
        super(parentShell);
        this.history = history;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Simulation Period");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite content = new Composite((Composite)super.createDialogArea(parent), SWT.NONE);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Begin Date");
        begin = new Combo(content, SWT.READ_ONLY);
        begin.setVisibleItemCount(25);
        
        label = new Label(content, SWT.NONE);
        label.setText("End Date");
        end = new Combo(content, SWT.READ_ONLY);
        end.setVisibleItemCount(25);
        
        int first = -1, last = -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
        for (int i = 0; i < history.size(); i++)
        {
            Bar bar = (Bar)history.get(i);
            begin.add(dateFormat.format(bar.getDate()));
            end.add(dateFormat.format(bar.getDate()));
            if (first == -1 && beginDate != null && (bar.getDate().equals(beginDate) || bar.getDate().after(beginDate)))
                first = i;
            if (last == -1 && endDate != null && (bar.getDate().equals(endDate) || bar.getDate().after(endDate)))
                last = i;
        }
        begin.select(first != -1 ? first : 0);
        end.select(last != -1 ? last : end.getItemCount() - 1);

        return content.getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        beginDate = ((Bar)history.get(begin.getSelectionIndex())).getDate();
        endDate = ((Bar)history.get(end.getSelectionIndex())).getDate();
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
