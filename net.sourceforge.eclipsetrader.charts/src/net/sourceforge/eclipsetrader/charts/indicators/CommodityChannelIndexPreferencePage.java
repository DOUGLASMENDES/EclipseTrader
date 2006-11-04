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

public class CommodityChannelIndexPreferencePage extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private Combo lineType;
    private ColorSelector color;
    private Spinner period;
    private Spinner smoothing;
    private Combo maType;

    public CommodityChannelIndexPreferencePage()
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
        label.setText(Messages.CommodityChannelIndexPreferencePage_Color);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        color = new ColorSelector(content);
        color.setColorValue(getSettings().getColor("color", CommodityChannelIndex.DEFAULT_COLOR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.CommodityChannelIndexPreferencePage_Label);
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", CommodityChannelIndex.DEFAULT_LABEL)); //$NON-NLS-1$

        lineType = createLineTypeCombo(content, Messages.CommodityChannelIndexPreferencePage_LineType, getSettings().getInteger("lineType", CommodityChannelIndex.DEFAULT_LINETYPE).intValue()); //$NON-NLS-2$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.CommodityChannelIndexPreferencePage_Period);
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(1);
        period.setMaximum(99);
        period.setSelection(getSettings().getInteger("period", CommodityChannelIndex.DEFAULT_PERIOD).intValue()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.CommodityChannelIndexPreferencePage_Smoothing);
        smoothing = new Spinner(content, SWT.BORDER);
        smoothing.setLayoutData(new GridData(25, SWT.DEFAULT));
        smoothing.setMinimum(1);
        smoothing.setMaximum(99);
        smoothing.setSelection(getSettings().getInteger("smoothing", CommodityChannelIndex.DEFAULT_SMOOTHING).intValue()); //$NON-NLS-1$

        maType = createMovingAverageCombo(content, Messages.CommodityChannelIndexPreferencePage_SmoothingType, getSettings().getInteger("maType", Stochastic.DEFAULT_MATYPE).intValue()); //$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("color", color.getColorValue()); //$NON-NLS-1$
        getSettings().set("label", lineLabel.getText()); //$NON-NLS-1$
        getSettings().set("lineType", lineType.getSelectionIndex()); //$NON-NLS-1$
        getSettings().set("period", period.getSelection()); //$NON-NLS-1$
        getSettings().set("smoothing", smoothing.getSelection()); //$NON-NLS-1$
        getSettings().set("maType", maType.getSelectionIndex()); //$NON-NLS-1$
    }
}
