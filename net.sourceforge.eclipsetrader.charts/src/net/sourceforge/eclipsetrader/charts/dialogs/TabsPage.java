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

import net.sourceforge.eclipsetrader.charts.internal.Messages;
import net.sourceforge.eclipsetrader.core.db.ChartTab;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TabsPage extends PreferencePage
{
    private ChartTab chartTab;
    private Text tabLabel;

    public TabsPage(ChartTab chartTab)
    {
        super(chartTab.getLabel());
        this.chartTab = chartTab;
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
        label.setText(Messages.TabsPage_Label);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        tabLabel = new Text(content, SWT.BORDER);
        tabLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        tabLabel.setText(chartTab.getLabel());

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (tabLabel != null)
            chartTab.setLabel(tabLabel.getText());
        
        return super.performOk();
    }
}
