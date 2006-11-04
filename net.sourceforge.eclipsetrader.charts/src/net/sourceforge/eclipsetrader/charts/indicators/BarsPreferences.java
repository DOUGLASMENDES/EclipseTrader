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
        
        addSecuritySelector(content, "securityId", Messages.BarsPreferences_Security, 0); //$NON-NLS-1$

        Label label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_Type);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        barType = new Combo(content, SWT.READ_ONLY);
        barType.add(Messages.BarsPreferences_Bars);
        barType.add(Messages.BarsPreferences_Candles);
        barType.select(getSettings().getInteger("barType", Bars.DEFAULT_BARTYPE).intValue() - PlotLine.BAR); //$NON-NLS-1$
        
        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_PositiveBars);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        positiveBar = new ColorSelector(content);
        positiveBar.setColorValue(getSettings().getColor("positiveBar", Bars.DEFAULT_POSITIVE_BAR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_NegativeBars);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        negativeBar = new ColorSelector(content);
        negativeBar.setColorValue(getSettings().getColor("negativeBar", Bars.DEFAULT_NEGATIVE_BAR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_NeutralBars);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        neutralBar = new ColorSelector(content);
        neutralBar.setColorValue(getSettings().getColor("neutralBar", Bars.DEFAULT_NEUTRAL_BAR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_CandleLines);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineCandle = new ColorSelector(content);
        lineCandle.setColorValue(getSettings().getColor("lineCandle", Bars.DEFAULT_LINE_CANDLE).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_PositiveCandle);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        positiveCandle = new ColorSelector(content);
        positiveCandle.setColorValue(getSettings().getColor("positiveCandle", Bars.DEFAULT_POSITIVE_CANDLE).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.BarsPreferences_NegativeCandle);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        negativeCandle = new ColorSelector(content);
        negativeCandle.setColorValue(getSettings().getColor("negativeCandle", Bars.DEFAULT_NEGATIVE_CANDLE).getRGB()); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("barType", barType.getSelectionIndex() + PlotLine.BAR); //$NON-NLS-1$
        getSettings().set("positiveBar", positiveBar.getColorValue()); //$NON-NLS-1$
        getSettings().set("negativeBar", negativeBar.getColorValue()); //$NON-NLS-1$
        getSettings().set("neutralBar", neutralBar.getColorValue()); //$NON-NLS-1$
        getSettings().set("lineCandle", lineCandle.getColorValue()); //$NON-NLS-1$
        getSettings().set("positiveCandle", positiveCandle.getColorValue()); //$NON-NLS-1$
        getSettings().set("negativeCandle", negativeCandle.getColorValue()); //$NON-NLS-1$
        super.performFinish();
    }
}
