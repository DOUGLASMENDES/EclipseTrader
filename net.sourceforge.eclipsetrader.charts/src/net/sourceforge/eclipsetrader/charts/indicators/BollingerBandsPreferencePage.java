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

public class BollingerBandsPreferencePage extends IndicatorPluginPreferencePage
{
    private ColorSelector color;
    private Combo lineType;
    private Spinner period;
    private Spinner deviation;
    private Combo maType;

    public BollingerBandsPreferencePage()
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
        label.setText("Color");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        color = new ColorSelector(content);
        color.setColorValue(getSettings().getColor("color", BollingerBands.DEFAULT_COLOR).getRGB());

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", BollingerBands.DEFAULT_LINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(2);
        period.setMaximum(99);
        period.setSelection(getSettings().getInteger("period", BollingerBands.DEFAULT_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Deviation");
        deviation = new Spinner(content, SWT.BORDER);
        deviation.setLayoutData(new GridData(25, SWT.DEFAULT));
        deviation.setMinimum(0);
        deviation.setMaximum(99);
        deviation.setSelection(getSettings().getInteger("deviation", BollingerBands.DEFAULT_DEVIATION).intValue());

        maType = createMovingAverageCombo(content, "Smoothing Type", getSettings().getInteger("maType", BollingerBands.DEFAULT_MATYPE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("color", color.getColorValue());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("period", period.getSelection());
        getSettings().set("deviation", deviation.getSelection());
        getSettings().set("maType", maType.getSelectionIndex());
    }
}
