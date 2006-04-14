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

public class MACDPreferences extends IndicatorPluginPreferencePage
{
    private Text macdLabel;
    private ColorSelector macdColor;
    private Spinner fastPeriod;
    private Spinner slowPeriod;
    private Combo lineType;
    private Combo maType;
    private Combo input;
    private ColorSelector triggerColor;
    private Spinner triggerPeriod;
    private Text triggerLabel;
    private Combo triggerLineType;
    private ColorSelector oscColor;
    private Text oscLabel;
    private Combo oscLineType;

    public MACDPreferences()
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
        label.setText("MACD Color");
        macdColor = new ColorSelector(content);
        macdColor.setColorValue(getSettings().getColor("color", MACD.DEFAULT_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Fast Period");
        fastPeriod = new Spinner(content, SWT.BORDER);
        fastPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));
        fastPeriod.setMinimum(1);
        fastPeriod.setMaximum(99);
        fastPeriod.setSelection(getSettings().getInteger("fastPeriod", MACD.DEFAULT_FAST_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Slow Period");
        slowPeriod = new Spinner(content, SWT.BORDER);
        slowPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));
        slowPeriod.setMinimum(1);
        slowPeriod.setMaximum(99);
        slowPeriod.setSelection(getSettings().getInteger("slowPeriod", MACD.DEFAULT_SLOW_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("MACD Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        macdLabel = new Text(content, SWT.BORDER);
        macdLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        macdLabel.setText(getSettings().getString("label", MACD.DEFAULT_LABEL));

        lineType = createLineTypeCombo(content, "MACD Line Type", getSettings().getInteger("lineType", MACD.DEFAULT_LINETYPE).intValue());

        maType = createMovingAverageCombo(content, "MACD MA Type", getSettings().getInteger("maType", MACD.DEFAULT_MA_TYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("MACD Input");
        input = new Combo(content, SWT.READ_ONLY);
        input.add("OPEN");
        input.add("HIGH");
        input.add("LOW");
        input.add("CLOSE");
        input.select(getSettings().getInteger("input", MACD.DEFAULT_INPUT).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Trigger Color");
        triggerColor = new ColorSelector(content);
        triggerColor.setColorValue(getSettings().getColor("triggerColor", MACD.DEFAULT_TRIGGER_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Trigger Period");
        triggerPeriod = new Spinner(content, SWT.BORDER);
        triggerPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));
        triggerPeriod.setMinimum(1);
        triggerPeriod.setMaximum(99);
        triggerPeriod.setSelection(getSettings().getInteger("triggerPeriod", MACD.DEFAULT_TRIGGER_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Trigger Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        triggerLabel = new Text(content, SWT.BORDER);
        triggerLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        triggerLabel.setText(getSettings().getString("triggerLabel", MACD.DEFAULT_TRIGGER_LABEL));

        triggerLineType = createLineTypeCombo(content, "Trigger Line Type", getSettings().getInteger("triggerLineType", MACD.DEFAULT_TRIGGER_LINETYPE).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Osc Color");
        oscColor = new ColorSelector(content);
        oscColor.setColorValue(getSettings().getColor("oscColor", MACD.DEFAULT_OSC_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Osc Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        oscLabel = new Text(content, SWT.BORDER);
        oscLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        oscLabel.setText(getSettings().getString("oscLabel", MACD.DEFAULT_OSC_LABEL));

        oscLineType = createLineTypeCombo(content, "Osc Line Type", getSettings().getInteger("oscLineType", MACD.DEFAULT_OSC_LINETYPE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", macdLabel.getText());
        getSettings().set("color", macdColor.getColorValue());
        getSettings().set("fastPeriod", fastPeriod.getSelection());
        getSettings().set("slowPeriod", slowPeriod.getSelection());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("maType", maType.getSelectionIndex());
        getSettings().set("input", input.getSelectionIndex());
        getSettings().set("triggerColor", triggerColor.getColorValue());
        getSettings().set("triggerPeriod", triggerPeriod.getSelection());
        getSettings().set("triggerLabel", triggerLabel.getText());
        getSettings().set("triggerLineType", triggerLineType.getSelectionIndex());
        getSettings().set("oscColor", oscColor.getColorValue());
        getSettings().set("oscLabel", oscLabel.getText());
        getSettings().set("oscLineType", oscLineType.getSelectionIndex());
    }
}
