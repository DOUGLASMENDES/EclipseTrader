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
import java.text.SimpleDateFormat;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.indicators.Lines;
import net.sourceforge.eclipsetrader.core.CorePlugin;

public class LinePreferences extends ObjectPluginPreferencePage
{
    private ColorSelector colorSelector;
    private Text date1;
    private Text value1;
    private Text date2;
    private Text value2;
    private Button extend;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

    public LinePreferences()
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
        label.setText("Color");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", Lines.DEFAULT_COLOR).getRGB());

        label = new Label(content, SWT.NONE);
        label.setText("Start date / value");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        date1 = new Text(group, SWT.BORDER);
        date1.setLayoutData(new GridData(80, SWT.DEFAULT));
        date1.setText(df.format(getSettings().getDate("date1", null)));
        value1 = new Text(group, SWT.BORDER);
        value1.setLayoutData(new GridData(80, SWT.DEFAULT));
        value1.setText(nf.format(getSettings().getDouble("value1", null)));

        label = new Label(content, SWT.NONE);
        label.setText("End date / value");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        date2 = new Text(group, SWT.BORDER);
        date2.setLayoutData(new GridData(80, SWT.DEFAULT));
        date2.setText(df.format(getSettings().getDate("date2", null)));
        value2 = new Text(group, SWT.BORDER);
        value2.setLayoutData(new GridData(80, SWT.DEFAULT));
        value2.setText(nf.format(getSettings().getDouble("value2", null)));

        label = new Label(content, SWT.NONE);
        extend = new Button(content, SWT.CHECK);
        extend.setText("Extend");
        extend.setSelection(getSettings().getBoolean("extend", false));
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        try
        {
            getSettings().set("color", colorSelector.getColorValue());
            getSettings().set("date1", df.parse(date1.getText()));
            getSettings().set("date2", df.parse(date2.getText()));
            getSettings().set("value1", nf.parse(value1.getText()).doubleValue());
            getSettings().set("value2", nf.parse(value2.getText()).doubleValue());
            getSettings().set("extend", extend.getSelection());
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
    }
}
