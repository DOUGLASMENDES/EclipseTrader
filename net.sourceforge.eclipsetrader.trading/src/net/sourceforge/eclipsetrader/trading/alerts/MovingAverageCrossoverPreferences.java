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

package net.sourceforge.eclipsetrader.trading.alerts;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class MovingAverageCrossoverPreferences extends AlertPluginPreferencePage
{
    private Button daily;
    private Button weekly;
    private Button monthly;
    private Spinner period;
    private Combo maType;
    private Button upward;
    private Button downward;

    public MovingAverageCrossoverPreferences()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        Label label = new Label(content, SWT.NONE);
        label.setText("Interval");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));

        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        
        daily = new Button(group, SWT.RADIO);
        daily.setText("Daily");
        weekly = new Button(group, SWT.RADIO);
        weekly.setText("Weekly");
        monthly = new Button(group, SWT.RADIO);
        monthly.setText("Monthly");
        
        label = new Label(content, SWT.NONE);
        label.setText("Moving Average Type");
        maType = new Combo(content, SWT.READ_ONLY);
        maType.add("SIMPLE");
        maType.add("EXPONENTIAL");
        maType.add("WEIGHTED");
        maType.add("WILLIAM'S");
        
        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Spinner(content, SWT.BORDER);
        period.setMinimum(0);
        period.setMaximum(99999);
        
        label = new Label(content, SWT.NONE);
        label.setText("Direction");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));

        group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        
        upward = new Button(group, SWT.RADIO);
        upward.setText("Upward");
        downward = new Button(group, SWT.RADIO);
        downward.setText("Downward");

        int field = MovingAverageCrossover.DAILY;
        String value = (String)getParameters().get("interval");
        if (value != null)
            field = Integer.parseInt(value);
        daily.setSelection(field == MovingAverageCrossover.DAILY);
        weekly.setSelection(field == MovingAverageCrossover.WEEKLY);
        monthly.setSelection(field == MovingAverageCrossover.MONTHLY);
        
        value = (String)getParameters().get("period");
        if (value != null)
            period.setSelection(Integer.parseInt(value));
        
        value = (String)getParameters().get("maType");
        maType.select(value != null ? Integer.parseInt(value) : 1);
        
        field = MovingAverageCrossover.UPWARD;
        value = (String)getParameters().get("direction");
        if (value != null)
            field = Integer.parseInt(value);
        upward.setSelection(field == MovingAverageCrossover.UPWARD);
        downward.setSelection(field == MovingAverageCrossover.DOWNWARD);
        
        return content;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#performOk()
     */
    public void performOk()
    {
        Map parameters = new HashMap();

        if (daily.getSelection())
            parameters.put("interval", String.valueOf(PriceAlert.DAILY));
        else if (weekly.getSelection())
            parameters.put("interval", String.valueOf(PriceAlert.WEEKLY));
        else if (monthly.getSelection())
            parameters.put("interval", String.valueOf(PriceAlert.MONTHLY));
        parameters.put("period", String.valueOf(period.getSelection()));
        parameters.put("maType", String.valueOf(maType.getSelectionIndex()));
        if (upward.getSelection())
            parameters.put("direction", String.valueOf(MovingAverageCrossover.UPWARD));
        else if (downward.getSelection())
            parameters.put("direction", String.valueOf(MovingAverageCrossover.DOWNWARD));
        
        setParameters(parameters);
    }
}
