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

import net.sourceforge.eclipsetrader.core.db.Chart;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GeneralPage extends PreferencePage
{
    private Chart chart;
    private Text chartTitle;
    private Button clearDataButton;
    private Button saveAsDefaultButton;
    private boolean clearData = false;
    private boolean saveAsDefault = false;

    public GeneralPage(Chart chart)
    {
        super("General");
        this.chart = chart;
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Title");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        chartTitle = new Text(content, SWT.BORDER);
        chartTitle.setText(chart.getTitle());
        chartTitle.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        
        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true, 2, 1));
        
        clearDataButton = new Button(content, SWT.CHECK);
        clearDataButton.setText("Clear chart data");
        clearDataButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));

        saveAsDefaultButton = new Button(content, SWT.CHECK);
        saveAsDefaultButton.setText("Save as default");
        saveAsDefaultButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (chartTitle != null)
            chart.setTitle(chartTitle.getText());
        clearData = clearDataButton != null ? clearDataButton.getSelection() : false;
        saveAsDefault = saveAsDefaultButton != null ? saveAsDefaultButton.getSelection() : false;
        
        return super.performOk();
    }
    
    public boolean getSaveAsDefault()
    {
        return saveAsDefault;
    }
    
    public boolean getClearData()
    {
        return clearData;
    }
}
