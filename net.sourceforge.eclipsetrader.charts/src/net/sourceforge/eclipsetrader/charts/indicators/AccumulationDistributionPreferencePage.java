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
import org.eclipse.swt.widgets.Text;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;

public class AccumulationDistributionPreferencePage extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private Combo lineType;
    private ColorSelector colorSelector;
    private Combo method;

    public AccumulationDistributionPreferencePage()
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
        label.setText("Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", AccumulationDistribution.DEFAULT_LABEL));

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", AccumulationDistribution.DEFAULT_LINETYPE).intValue());
        
        label = new Label(content, SWT.NONE);
        label.setText("Color");
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", AccumulationDistribution.DEFAULT_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Method");
        method = new Combo(content, SWT.READ_ONLY);
        method.add("AD");
        method.add("WAD");
        method.select(getSettings().getInteger("method", AccumulationDistribution.DEFAULT_METHOD).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", lineLabel.getText());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("color", colorSelector.getColorValue());
        getSettings().set("method", method.getSelectionIndex());
    }
}
