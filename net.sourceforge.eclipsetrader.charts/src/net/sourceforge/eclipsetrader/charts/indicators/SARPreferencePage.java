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

package net.sourceforge.eclipsetrader.charts.indicators;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;
import net.sourceforge.eclipsetrader.core.CorePlugin;

public class SARPreferencePage extends IndicatorPluginPreferencePage
{
    private Text initial;
    private Text add;
    private Text limit;
    private NumberFormat nf = NumberFormat.getInstance();

    public SARPreferencePage()
    {
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);
        
        addColorSelector(content, "color", "Color", SAR.DEFAULT_COLOR);
        addLabelField(content, "label", "Label", SAR.DEFAULT_LABEL);
        addLineTypeSelector(content, "lineType", "Line Type", SAR.DEFAULT_LINETYPE);

        Label label = new Label(content, SWT.NONE);
        label.setText("Initial");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        initial = new Text(content, SWT.BORDER);
        initial.setLayoutData(new GridData(80, SWT.DEFAULT));
        initial.setText(nf.format(getSettings().getDouble("initial", SAR.DEFAULT_INITIAL)));

        label = new Label(content, SWT.NONE);
        label.setText("Add");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        add = new Text(content, SWT.BORDER);
        add.setLayoutData(new GridData(80, SWT.DEFAULT));
        add.setText(nf.format(getSettings().getDouble("add", SAR.DEFAULT_ADD)));

        label = new Label(content, SWT.NONE);
        label.setText("Limit");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        limit = new Text(content, SWT.BORDER);
        limit.setLayoutData(new GridData(80, SWT.DEFAULT));
        limit.setText(nf.format(getSettings().getDouble("limit", SAR.DEFAULT_LIMIT)));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        try
        {
            getSettings().set("initial", nf.parse(initial.getText()).doubleValue());
            getSettings().set("add", nf.parse(add.getText()).doubleValue());
            getSettings().set("limit", nf.parse(limit.getText()).doubleValue());
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
        super.performFinish();
    }
}
