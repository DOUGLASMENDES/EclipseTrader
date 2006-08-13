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
import net.sourceforge.eclipsetrader.charts.PlotLine;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BarsPreferences extends IndicatorPluginPreferencePage
{
    private Combo barType;
    private ColorSelector positiveBar;
    private ColorSelector negativeBar;
    private ColorSelector neutralBar;
    private ColorSelector lineCandle;
    private ColorSelector positiveCandle;
    private ColorSelector negativeCandle;

    public BarsPreferences()
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
        label.setText("Bar Type");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        barType = new Combo(content, SWT.READ_ONLY);
        barType.add("Bars");
        barType.add("Candles");
        barType.select(getSettings().getInteger("barType", Bars.DEFAULT_BARTYPE).intValue() - PlotLine.BAR);
        
        label = new Label(content, SWT.NONE);
        label.setText("Positive Bars");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        positiveBar = new ColorSelector(content);
        positiveBar.setColorValue(getSettings().getColor("positiveBar", Bars.DEFAULT_POSITIVE_BAR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Negative Bars");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        negativeBar = new ColorSelector(content);
        negativeBar.setColorValue(getSettings().getColor("negativeBar", Bars.DEFAULT_NEGATIVE_BAR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Neutral Bars");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        neutralBar = new ColorSelector(content);
        neutralBar.setColorValue(getSettings().getColor("neutralBar", Bars.DEFAULT_NEUTRAL_BAR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Candle Lines");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineCandle = new ColorSelector(content);
        lineCandle.setColorValue(getSettings().getColor("lineCandle", Bars.DEFAULT_LINE_CANDLE).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Positive Candles");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        positiveCandle = new ColorSelector(content);
        positiveCandle.setColorValue(getSettings().getColor("positiveCandle", Bars.DEFAULT_POSITIVE_CANDLE).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Negative Candles");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        negativeCandle = new ColorSelector(content);
        negativeCandle.setColorValue(getSettings().getColor("negativeCandle", Bars.DEFAULT_NEGATIVE_CANDLE).getRGB());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("barType", barType.getSelectionIndex() + PlotLine.BAR);
        getSettings().set("positiveBar", positiveBar.getColorValue());
        getSettings().set("negativeBar", negativeBar.getColorValue());
        getSettings().set("neutralBar", neutralBar.getColorValue());
        getSettings().set("lineCandle", lineCandle.getColorValue());
        getSettings().set("positiveCandle", positiveCandle.getColorValue());
        getSettings().set("negativeCandle", negativeCandle.getColorValue());
    }
}
