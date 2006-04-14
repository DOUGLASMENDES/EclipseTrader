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

public class FiboLinePreferences extends ObjectPluginPreferencePage
{
    private ColorSelector colorSelector;
    private Text date1;
    private Text value1;
    private Text date2;
    private Text value2;
    private Text line1;
    private Text line2;
    private Text line3;
    private Text line4;
    private Text line5;
    private Button extend;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();
    private NumberFormat pcf = ChartsPlugin.getPercentageFormat();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

    public FiboLinePreferences()
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
        label.setText("Lines");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        line1 = new Text(content, SWT.BORDER);
        line1.setLayoutData(new GridData(80, SWT.DEFAULT));
        Double value = getSettings().getDouble("line1", null);
        if (value != null)
            line1.setText(pcf.format(value));

        label = new Label(content, SWT.NONE);
        line2 = new Text(content, SWT.BORDER);
        line2.setLayoutData(new GridData(80, SWT.DEFAULT));
        value = getSettings().getDouble("line2", 38.2);
        if (value != null)
            line2.setText(pcf.format(value));

        label = new Label(content, SWT.NONE);
        line3 = new Text(content, SWT.BORDER);
        line3.setLayoutData(new GridData(80, SWT.DEFAULT));
        value = getSettings().getDouble("line3", 50.0);
        if (value != null)
            line3.setText(pcf.format(value));

        label = new Label(content, SWT.NONE);
        line4 = new Text(content, SWT.BORDER);
        line4.setLayoutData(new GridData(80, SWT.DEFAULT));
        value = getSettings().getDouble("line4", 61.8);
        if (value != null)
            line4.setText(pcf.format(value));

        label = new Label(content, SWT.NONE);
        line5 = new Text(content, SWT.BORDER);
        line5.setLayoutData(new GridData(80, SWT.DEFAULT));
        value = getSettings().getDouble("line5", null);
        if (value != null)
            line5.setText(pcf.format(value));
        
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
            if (line1.getText().length() != 0)
                getSettings().set("line1", nf.parse(line1.getText()).doubleValue());
            if (line2.getText().length() != 0)
                getSettings().set("line2", nf.parse(line2.getText()).doubleValue());
            if (line3.getText().length() != 0)
                getSettings().set("line3", nf.parse(line3.getText()).doubleValue());
            if (line4.getText().length() != 0)
                getSettings().set("line4", nf.parse(line4.getText()).doubleValue());
            if (line5.getText().length() != 0)
                getSettings().set("line5", nf.parse(line5.getText()).doubleValue());
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
    }
}
