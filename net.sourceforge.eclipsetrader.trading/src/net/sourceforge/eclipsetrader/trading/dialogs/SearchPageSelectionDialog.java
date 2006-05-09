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

package net.sourceforge.eclipsetrader.trading.dialogs;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.trading.views.IPatternSearchPage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SearchPageSelectionDialog extends Dialog
{
    private List pages;
    private Table table;
    private IPatternSearchPage selectedPage;

    public SearchPageSelectionDialog(Shell parentShell, List pages, IPatternSearchPage selectedPage)
    {
        super(parentShell);
        this.pages = pages;
        this.selectedPage = selectedPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Search Page Selection");
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
        
        table = new Table(content, SWT.FULL_SELECTION|SWT.SINGLE);
        table.setHeaderVisible(false);
        table.setLinesVisible(false);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.widthHint = 250;
        gridData.heightHint = 250;
        table.setLayoutData(gridData);
        new TableColumn(table, SWT.LEFT);
        
        for (Iterator iter = pages.iterator(); iter.hasNext(); )
        {
            IPatternSearchPage page = (IPatternSearchPage) iter.next();
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, page.getShortDescription());
            tableItem.setData(page);
            if (page == selectedPage)
                table.select(table.indexOf(tableItem));
        }
        
        table.getColumn(0).pack();
        
        return super.createDialogArea(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        TableItem[] selection = table.getSelection();
        if (selection.length == 1)
            selectedPage = (IPatternSearchPage) selection[0].getData();
        super.okPressed();
    }

    public IPatternSearchPage getSelectedPage()
    {
        return selectedPage;
    }
}
