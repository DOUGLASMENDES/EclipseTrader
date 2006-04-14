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

public class StochasticPreferencePage extends IndicatorPluginPreferencePage
{
    private ColorSelector dcolor;
    private ColorSelector kcolor;
    private ColorSelector buyColor;
    private ColorSelector sellColor;
    private Combo dlineType;
    private Text dlabel;
    private Spinner dperiod;
    private Combo klineType;
    private Text klabel;
    private Spinner kperiod;
    private Spinner period;
    private Spinner buyLine;
    private Spinner sellLine;
    private Combo maType;

    public StochasticPreferencePage()
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
        label.setText("%D Color");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        dcolor = new ColorSelector(content);
        dcolor.setColorValue(getSettings().getColor("dcolor", Stochastic.DEFAULT_DCOLOR).getRGB());

        dlineType = createLineTypeCombo(content, "%D Line Type", getSettings().getInteger("dlineType", Stochastic.DEFAULT_DLINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("%D Label");
        dlabel = new Text(content, SWT.BORDER);
        dlabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        dlabel.setText(getSettings().getString("dlabel", Stochastic.DEFAULT_DLABEL));

        label = new Label(content, SWT.NONE);
        label.setText("%D Period");
        dperiod = new Spinner(content, SWT.BORDER);
        dperiod.setLayoutData(new GridData(25, SWT.DEFAULT));
        dperiod.setMinimum(1);
        dperiod.setMaximum(99);
        dperiod.setSelection(getSettings().getInteger("dperiod", Stochastic.DEFAULT_DPERIOD).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("%K Color");
        kcolor = new ColorSelector(content);
        kcolor.setColorValue(getSettings().getColor("kcolor", Stochastic.DEFAULT_KCOLOR).getRGB());

        klineType = createLineTypeCombo(content, "%K Line Type", getSettings().getInteger("klineType", Stochastic.DEFAULT_KLINETYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("%K Label");
        klabel = new Text(content, SWT.BORDER);
        klabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        klabel.setText(getSettings().getString("klabel", Stochastic.DEFAULT_KLABEL));

        label = new Label(content, SWT.NONE);
        label.setText("%K Period");
        kperiod = new Spinner(content, SWT.BORDER);
        kperiod.setLayoutData(new GridData(25, SWT.DEFAULT));
        kperiod.setMinimum(1);
        kperiod.setMaximum(99);
        kperiod.setSelection(getSettings().getInteger("kperiod", Stochastic.DEFAULT_KPERIOD).intValue());

        maType = createMovingAverageCombo(content, "MA Type", getSettings().getInteger("maType", Stochastic.DEFAULT_MATYPE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        period = new Spinner(content, SWT.BORDER);
        period.setLayoutData(new GridData(25, SWT.DEFAULT));
        period.setMinimum(1);
        period.setMaximum(99);
        period.setSelection(getSettings().getInteger("period", Stochastic.DEFAULT_PERIOD).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Buy Line Color");
        buyColor = new ColorSelector(content);
        buyColor.setColorValue(getSettings().getColor("buyColor", Stochastic.DEFAULT_BUYCOLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Buy Line");
        buyLine = new Spinner(content, SWT.BORDER);
        buyLine.setLayoutData(new GridData(25, SWT.DEFAULT));
        buyLine.setMinimum(1);
        buyLine.setMaximum(99);
        buyLine.setSelection(getSettings().getInteger("buyLine", Stochastic.DEFAULT_BUYLINE).intValue());

        label = new Label(content, SWT.NONE);
        label.setText("Sell Line Color");
        sellColor = new ColorSelector(content);
        sellColor.setColorValue(getSettings().getColor("sellColor", Stochastic.DEFAULT_SELLCOLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Sell Line");
        sellLine = new Spinner(content, SWT.BORDER);
        sellLine.setLayoutData(new GridData(25, SWT.DEFAULT));
        sellLine.setMinimum(1);
        sellLine.setMaximum(99);
        sellLine.setSelection(getSettings().getInteger("sellLine", Stochastic.DEFAULT_SELLLINE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("dcolor", dcolor.getColorValue());
        getSettings().set("kcolor", kcolor.getColorValue());
        getSettings().set("buyColor", buyColor.getColorValue());
        getSettings().set("sellColor", sellColor.getColorValue());
        getSettings().set("dlineType", dlineType.getSelectionIndex());
        getSettings().set("dlabel", dlabel.getText());
        getSettings().set("dperiod", dperiod.getSelection());
        getSettings().set("klineType", klineType.getSelectionIndex());
        getSettings().set("klabel", klabel.getText());
        getSettings().set("kperiod", kperiod.getSelection());
        getSettings().set("period", period.getSelection());
        getSettings().set("buyLine", buyLine.getSelection());
        getSettings().set("sellLine", sellLine.getSelection());
        getSettings().set("maType", maType.getSelectionIndex());
    }
}
