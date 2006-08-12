/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Adzinets - initial implementation
 */

package net.sourceforge.eclipsetrader.charts.indicators;

import java.text.NumberFormat;
import java.text.ParseException;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class MAChannelsPreferencePage extends IndicatorPluginPreferencePage
{
    private ColorSelector color;
    private Combo lineType;
    private Spinner period;
    private Text percentage;
    private Combo maType;
    private NumberFormat nf = NumberFormat.getInstance();
    private Logger logger = Logger.getLogger(getClass());

    public MAChannelsPreferencePage()
    {
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(1);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
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
        color.setColorValue(getSettings().getColor("color", MAChannels.DEFAULT_COLOR).getRGB());

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", MAChannels.DEFAULT_LINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(2);
        period.setMaximum(999);
        period.setSelection(getSettings().getInteger("period", MAChannels.DEFAULT_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Channel coefficient, %");
        percentage = new Text(content, SWT.BORDER);
        percentage.setLayoutData(new GridData(25, SWT.DEFAULT));
        percentage.setText(nf.format(getSettings().getDouble("percentage", MAChannels.DEFAULT_PERCENTAGE)));

        maType = createMovingAverageCombo(content, "Smoothing Type", getSettings().getInteger("maType", MAChannels.DEFAULT_MATYPE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("color", color.getColorValue());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("period", period.getSelection());
        getSettings().set("maType", maType.getSelectionIndex());
        try {
            getSettings().set("percentage", nf.parse(percentage.getText()).doubleValue());
        } catch (ParseException e) {
            logger.warn(e);
        }
    }
}
