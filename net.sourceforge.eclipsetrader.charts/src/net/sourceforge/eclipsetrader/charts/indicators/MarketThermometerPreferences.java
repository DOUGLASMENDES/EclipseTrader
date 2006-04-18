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

public class MarketThermometerPreferences extends IndicatorPluginPreferencePage
{
    private ColorSelector upColor;
    private ColorSelector downColor;
    private ColorSelector threshColor;
    private Text lineLabel;
    private Combo lineType;
    private Spinner threshold;
    private Spinner smoothing;
    private Combo smoothingType;
    private Combo maType;
    private ColorSelector maColor;
    private Combo maLineType;
    private Text maLabel;
    private Spinner maPeriod;

    public MarketThermometerPreferences()
    {
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
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Color Above MA");
        upColor = new ColorSelector(content);
        upColor.setColorValue(getSettings().getColor("upColor", MarketThermometer.DEFAULT_UP_COLOR).getRGB());
        
        label = new Label(content, SWT.NONE);
        label.setText("Color Below MA");
        downColor = new ColorSelector(content);
        downColor.setColorValue(getSettings().getColor("downColor", MarketThermometer.DEFAULT_DOWN_COLOR).getRGB());
        
        label = new Label(content, SWT.NONE);
        label.setText("Color Threshold");
        threshColor = new ColorSelector(content);
        threshColor.setColorValue(getSettings().getColor("threshColor", MarketThermometer.DEFAULT_THRESH_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", MarketThermometer.DEFAULT_LABEL));

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", MarketThermometer.DEFAULT_LINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Threshold");
        threshold = new Spinner(content, SWT.BORDER);
        threshold.setLayoutData(new GridData(25, SWT.DEFAULT));
        threshold.setMinimum(2);
        threshold.setMaximum(99);
        threshold.setSelection(getSettings().getInteger("threshold", MarketThermometer.DEFAULT_THRESHOLD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Smoothing");
        smoothing = new Spinner(content, SWT.BORDER);
        smoothing.setLayoutData(new GridData(25, SWT.DEFAULT));
        smoothing.setMinimum(2);
        smoothing.setMaximum(99);
        smoothing.setSelection(getSettings().getInteger("smoothing", MarketThermometer.DEFAULT_SMOOTHING).intValue());
        
        smoothingType = createMovingAverageCombo(content, "Smoothing Type", MarketThermometer.DEFAULT_SMOOTHTYPE);
        
        label = new Label(content, SWT.NONE);
        label.setText("MA Color");
        maColor = new ColorSelector(content);
        maColor.setColorValue(getSettings().getColor("maColor", MarketThermometer.DEFAULT_MA_COLOR).getRGB());

        maLineType = createLineTypeCombo(content, "MA Line Type", getSettings().getInteger("maLineType", MarketThermometer.DEFAULT_MA_LINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("MA Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        maLabel = new Text(content, SWT.BORDER);
        maLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        maLabel.setText(getSettings().getString("maLabel", MarketThermometer.DEFAULT_MA_LABEL));

        label = new Label(content, SWT.NONE);
        label.setText("MA Period");
        maPeriod = new Spinner(content, SWT.BORDER);
        maPeriod.setLayoutData(new GridData(25, SWT.DEFAULT));
        maPeriod.setMinimum(2);
        maPeriod.setMaximum(99);
        maPeriod.setSelection(getSettings().getInteger("maPeriod", MarketThermometer.DEFAULT_MA_PERIOD).intValue());

        maType = createMovingAverageCombo(content, "MA Type", getSettings().getInteger("maType", MarketThermometer.DEFAULT_MA_TYPE).intValue());
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("downColor", downColor.getColorValue());
        getSettings().set("upColor", upColor.getColorValue());
        getSettings().set("threshColor", threshColor.getColorValue());
        getSettings().set("maColor", maColor.getColorValue());
        getSettings().set("maLineType", maLineType.getSelectionIndex());
        getSettings().set("label", lineLabel.getText());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("threshold", threshold.getSelection());
        getSettings().set("smoothing", smoothing.getSelection());
        getSettings().set("maPeriod", maPeriod.getSelection());
        getSettings().set("maType", maType.getSelectionIndex());
        getSettings().set("smoothType", smoothingType.getSelectionIndex());
    }
}
