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

package net.sourceforge.eclipsetrader.core.ui.export;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class ExportSelectionPage extends WizardPage
{
    public static final int HISTORICAL_PRICES = 0;
    public static final int INTRADAY_PRICES = 1;
    public static final int LAST_PRICES = 2;
    Button all;
    Button selected;
    List list;
    Button historical;
    Button intraday;
    Button prices;
    Text file;

    public ExportSelectionPage()
    {
        super("Export CSV Data"); //$NON-NLS-1$
        setPageComplete(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        all = new Button(content, SWT.RADIO);
        all.setText(Messages.ExportSelectionPage_All);
        all.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                list.setEnabled(false);
                setPageComplete((list.getSelectionIndex() != -1 || all.getSelection()) && file.getText().length() != 0);
            }
        });

        selected = new Button(content, SWT.RADIO);
        selected.setText(Messages.ExportSelectionPage_Selected);
        selected.setSelection(true);
        selected.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                list.setEnabled(true);
                setPageComplete((list.getSelectionIndex() != -1 || all.getSelection()) && file.getText().length() != 0);
            }
        });

        list = new List(content, SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = list.getItemHeight() * 10;
        list.setLayoutData(gridData);
        list.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setPageComplete((list.getSelectionIndex() != -1 || all.getSelection()) && file.getText().length() != 0);
            }
        });

        java.util.List securities = CorePlugin.getRepository().allSecurities();
        Collections.sort(securities, new Comparator() {
            public int compare(Object arg0, Object arg1)
            {
                return ((Security)arg0).getDescription().compareTo(((Security)arg1).getDescription());
            }
        });

        for (Iterator iter = securities.iterator(); iter.hasNext(); )
        {
            Security security = (Security)iter.next();
            list.add(security.getDescription());
            list.setData(String.valueOf(list.getItemCount() - 1), security);
        }

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.ExportSelectionPage_SelectLabel);

        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

        historical = new Button(group, SWT.RADIO);
        historical.setText(Messages.ExportSelectionPage_Historical);
        historical.setSelection(true);
        intraday = new Button(group, SWT.RADIO);
        intraday.setText(Messages.ExportSelectionPage_intraday);
        prices = new Button(group, SWT.RADIO);
        prices.setText(Messages.ExportSelectionPage_Last);

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ExportSelectionPage_DestinationLabel);

        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        
        label = new Label(group, SWT.NONE);
        label.setText(Messages.ExportSelectionPage_FileLabel);
        file = new Text(group, SWT.BORDER);
        file.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        file.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                setPageComplete((list.getSelectionIndex() != -1 || all.getSelection()) && file.getText().length() != 0);
            }
        });
        Button button = new Button(group, SWT.PUSH);
        button.setText(Messages.ExportSelectionPage_BrowseButton);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dlg = new FileDialog(getShell(), SWT.SAVE|SWT.SINGLE);
                dlg.setText(Messages.ExportSelectionPage_FileDialogTitle);
                String result = dlg.open();
                if (result != null)
                {
                    file.setText(result);
                    setPageComplete((list.getSelectionIndex() != -1 || all.getSelection()) && file.getText().length() != 0);
                }
            }
        });
    }
    
    public Security[] getSelectedSecurity()
    {
        if (selected.getSelection())
        {
            int[] indices = list.getSelectionIndices();
            Security[] selection = new Security[indices.length];
            for (int i = 0; i < selection.length; i++)
                selection[i] = (Security)list.getData(String.valueOf(indices[i]));
            return selection;
        }
        else
            return (Security[])CorePlugin.getRepository().allSecurities().toArray(new Security[0]);
    }
    
    public String getFile()
    {
        return file.getText();
    }
    
    public int getDataToExport()
    {
        if (historical.getSelection())
            return HISTORICAL_PRICES;
        if (intraday.getSelection())
            return INTRADAY_PRICES;
        if (prices.getSelection())
            return LAST_PRICES;
        return -1;
    }
}
