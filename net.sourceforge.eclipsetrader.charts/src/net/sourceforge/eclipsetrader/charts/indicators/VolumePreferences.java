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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class VolumePreferences extends IndicatorPluginPreferencePage
{
    private Text lineLabel;
    private ColorSelector positiveColor;
    private ColorSelector negativeColor;
    private ColorSelector neutralColor;

    public VolumePreferences()
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
        label.setText(Messages.VolumePreferences_Label);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        lineLabel = new Text(content, SWT.BORDER);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        lineLabel.setText(getSettings().getString("label", Volume.DEFAULT_LABEL)); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.VolumePreferences_Positive);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        positiveColor = new ColorSelector(content);
        positiveColor.setColorValue(getSettings().getColor("positive", Volume.DEFAULT_POSITIVE).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.VolumePreferences_Negative);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        negativeColor = new ColorSelector(content);
        negativeColor.setColorValue(getSettings().getColor("negative", Volume.DEFAULT_NEGATIVE).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.VolumePreferences_Neutral);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        neutralColor = new ColorSelector(content);
        neutralColor.setColorValue(getSettings().getColor("neutral", Volume.DEFAULT_NEUTRAL).getRGB()); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        getSettings().set("label", lineLabel.getText()); //$NON-NLS-1$
        getSettings().set("positive", positiveColor.getColorValue()); //$NON-NLS-1$
        getSettings().set("negative", negativeColor.getColorValue()); //$NON-NLS-1$
        getSettings().set("neutral", neutralColor.getColorValue()); //$NON-NLS-1$
    }
}
