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

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class MAPreferences extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private Combo lineType;
    private ColorSelector colorSelector;
    private Spinner period;
    private Combo type;

    public MAPreferences()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        setControl(content);

        Label label = new Label(content, SWT.NONE);
        label.setText("Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", MA.DEFAULT_LABEL));

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", MA.DEFAULT_LINETYPE).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Color");
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", MA.DEFAULT_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(2);
        period.setMaximum(99);
        period.setSelection(getSettings().getInteger("period", MA.DEFAULT_PERIOD).intValue());

        type = createMovingAverageCombo(content, "Type", getSettings().getInteger("type", MA.DEFAULT_TYPE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", lineLabel.getText());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("color", colorSelector.getColorValue());
        getSettings().set("period", period.getSelection());
        getSettings().set("type", type.getSelectionIndex());
    }
}
