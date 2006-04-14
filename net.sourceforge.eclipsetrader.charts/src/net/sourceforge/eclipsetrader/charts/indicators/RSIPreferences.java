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

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

public class RSIPreferences extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private Combo lineType;
    private ColorSelector color;
    private ColorSelector buyColor;
    private ColorSelector sellColor;
    private Spinner period;
    private Spinner smoothing;
    private Combo type;
    private Spinner buyLine;
    private Spinner sellLine;
    private Combo input;

    public RSIPreferences()
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
        lineLabel.setText(getSettings().getString("label", RSI.DEFAULT_LABEL));
        
        label = new Label(content, SWT.NONE);
        label.setText("Color");
        color = new ColorSelector(content);
        color.setColorValue(getSettings().getColor("color", RSI.DEFAULT_COLOR).getRGB());

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", RSI.DEFAULT_LINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(2);
        period.setMaximum(99);
        period.setSelection(getSettings().getInteger("period", RSI.DEFAULT_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Smoothing");
        smoothing = new Spinner(content, SWT.BORDER);
        smoothing.setLayoutData(new GridData(25, SWT.DEFAULT));
        smoothing.setMinimum(2);
        smoothing.setMaximum(99);
        smoothing.setSelection(getSettings().getInteger("period", RSI.DEFAULT_SMOOTHING).intValue());

        type = createMovingAverageCombo(content, "Smoothing Type", getSettings().getInteger("type", RSI.DEFAULT_SMOOTHING_TYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Input");
        input = new Combo(content, SWT.READ_ONLY);
        input.add("OPEN");
        input.add("HIGH");
        input.add("LOW");
        input.add("CLOSE");
        input.select(getSettings().getInteger("input", RSI.DEFAULT_INPUT).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Buy Line");
        buyLine = new Spinner(content, SWT.BORDER);
        buyLine.setLayoutData(new GridData(25, SWT.DEFAULT));
        buyLine.setMinimum(2);
        buyLine.setMaximum(99);
        buyLine.setSelection(getSettings().getInteger("buyLine", RSI.DEFAULT_BUYLINE).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Buy Line Color");
        buyColor = new ColorSelector(content);
        buyColor.setColorValue(getSettings().getColor("buyLineColor", RSI.DEFAULT_BUYLINE_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Sell Line");
        sellLine = new Spinner(content, SWT.BORDER);
        sellLine.setLayoutData(new GridData(25, SWT.DEFAULT));
        sellLine.setMinimum(2);
        sellLine.setMaximum(99);
        sellLine.setSelection(getSettings().getInteger("sellLine", RSI.DEFAULT_SELLLINE).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Sell Line Color");
        sellColor = new ColorSelector(content);
        sellColor.setColorValue(getSettings().getColor("sellLineColor", RSI.DEFAULT_SELLLINE_COLOR).getRGB());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", lineLabel.getText());
        getSettings().set("color", color.getColorValue());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("period", period.getSelection());
        getSettings().set("smoothing", smoothing.getSelection());
        getSettings().set("smoothingType", type.getSelectionIndex());
        getSettings().set("input", input.getSelectionIndex());
        getSettings().set("buyLine", buyLine.getSelection());
        getSettings().set("buyLineColor", buyColor.getColorValue());
        getSettings().set("sellLine", sellLine.getSelection());
        getSettings().set("sellLineColor", sellColor.getColorValue());
    }
}
