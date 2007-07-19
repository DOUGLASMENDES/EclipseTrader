/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Robert Kallal - Port of single line to parallel line
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

public class ParallelLinePreferences extends ObjectPluginPreferencePage
{
    private ColorSelector colorSelector;
    private Text date1;
    private Text value1;
    private Text date2;
    private Text value2;
    private Text valueOffset;
    private Button extend1;
    private Button extend2;
    private NumberFormat nf = ChartsPlugin.getPriceFormat();
    private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

    public ParallelLinePreferences()
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
        label.setText(Messages.ParallelLinePreferences_Color);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        colorSelector = new ColorSelector(content);
        colorSelector.setColorValue(getSettings().getColor("color", Lines.DEFAULT_COLOR).getRGB()); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ParallelLinePreferences_StartDateValue);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        date1 = new Text(group, SWT.BORDER);
        date1.setLayoutData(new GridData(80, SWT.DEFAULT));
        date1.setText(df.format(getSettings().getDate("date1", null))); //$NON-NLS-1$
        value1 = new Text(group, SWT.BORDER);
        value1.setLayoutData(new GridData(80, SWT.DEFAULT));
        value1.setText(nf.format(getSettings().getDouble("value1", null))); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ParallelLinePreferences_EndDateValue);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        date2 = new Text(group, SWT.BORDER);
        date2.setLayoutData(new GridData(80, SWT.DEFAULT));
        date2.setText(df.format(getSettings().getDate("date2", null))); //$NON-NLS-1$
        value2 = new Text(group, SWT.BORDER);
        value2.setLayoutData(new GridData(80, SWT.DEFAULT));
        value2.setText(nf.format(getSettings().getDouble("value2", null))); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        label.setText(Messages.ParallelLinePreferences_OffsetValue);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        valueOffset = new Text(group, SWT.BORDER);
        valueOffset.setLayoutData(new GridData(80, SWT.DEFAULT));
        valueOffset.setText(nf.format(getSettings().getDouble("valueOffset", null))); //$NON-NLS-1$
        
        label = new Label(content, SWT.NONE);
        extend1 = new Button(content, SWT.CHECK);
        extend1.setText(Messages.ParallelLinePreferences_ExtendStart);
        extend1.setSelection(getSettings().getBoolean("extend1", false)); //$NON-NLS-1$

        label = new Label(content, SWT.NONE);
        extend2 = new Button(content, SWT.CHECK);
        extend2.setText(Messages.ParallelLinePreferences_ExtendEnd);
        extend2.setSelection(getSettings().getBoolean("extend2", false)); //$NON-NLS-1$

    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage#performFinish()
     */
    public void performFinish()
    {
        try
        {
            getSettings().set("color", colorSelector.getColorValue()); //$NON-NLS-1$
            getSettings().set("date1", df.parse(date1.getText())); //$NON-NLS-1$
            getSettings().set("date2", df.parse(date2.getText())); //$NON-NLS-1$
            getSettings().set("value1", nf.parse(value1.getText()).doubleValue()); //$NON-NLS-1$
            getSettings().set("value2", nf.parse(value2.getText()).doubleValue()); //$NON-NLS-1$
            getSettings().set("valueOffset",nf.parse(valueOffset.getText()).doubleValue()); //$NON-NLS-1$
            getSettings().set("extend1", extend1.getSelection()); //$NON-NLS-1$
            getSettings().set("extend2", extend2.getSelection()); //$NON-NLS-1$
        }
        catch (ParseException e) {
            CorePlugin.logException(e);
        }
    }
}
