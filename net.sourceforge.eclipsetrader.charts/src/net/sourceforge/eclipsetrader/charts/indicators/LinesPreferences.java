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
import org.eclipse.swt.widgets.Text;

public class LinesPreferences extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private Combo lineType;
    private ColorSelector colorSelector;
    private Combo input;

    public LinesPreferences()
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
        label.setText("Color");
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", Lines.DEFAULT_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Label");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", Lines.DEFAULT_LABEL));
        
        addSecuritySelector(content, "securityId", "Security", 0);

        label = new Label(content, SWT.NONE);
        label.setText("Input");
        input = new Combo(content, SWT.READ_ONLY);
        input.add("OPEN");
        input.add("HIGH");
        input.add("LOW");
        input.add("CLOSE");
        input.select(getSettings().getInteger("input", Lines.DEFAULT_INPUT).intValue());

        lineType = createLineTypeCombo(content, "Line Type", getSettings().getInteger("lineType", Lines.DEFAULT_LINETYPE).intValue());
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", lineLabel.getText());
        getSettings().set("lineType", lineType.getSelectionIndex());
        getSettings().set("color", colorSelector.getColorValue());
        getSettings().set("input", input.getSelectionIndex());
        super.performFinish();
    }
}
