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

package net.sourceforge.eclipsetrader.charts.objects;

import java.text.NumberFormat;
import java.text.ParseException;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.indicators.Lines;
import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HorizontalLinePreferences extends ObjectPluginPreferencePage
{
    private ColorSelector colorSelector;
    private Text value;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();

    public HorizontalLinePreferences()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage#createControl(org.eclipse.swt.widgets.Composite)
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
        label.setText(Messages.HorizontalLinePreferences_Color);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", Lines.DEFAULT_COLOR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.HorizontalLinePreferences_Value);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        value = new Text(content, SWT.BORDER);
        value.setLayoutData(new GridData(80, SWT.DEFAULT));
        value.setText(nf.format(getSettings().getDouble("value", null))); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        try
        {
            getSettings().set("color", colorSelector.getColorValue()); //$NON-NLS-1$
            getSettings().set("value", nf.parse(value.getText()).doubleValue()); //$NON-NLS-1$
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
    }
}
