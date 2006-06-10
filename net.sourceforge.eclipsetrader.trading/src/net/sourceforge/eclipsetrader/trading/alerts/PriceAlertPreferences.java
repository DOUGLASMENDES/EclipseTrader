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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class PriceAlertPreferences extends AlertPluginPreferencePage
{
    private Combo interval;
    private Spinner period;
    private Combo type;
    private Label description;
    private SelectionAdapter selectionAdapter = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
            updateDescription();
        }
    };

    public PriceAlertPreferences()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        
        Label label = new Label(content, SWT.NONE);
        label.setText("Interval");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        interval = new Combo(content, SWT.READ_ONLY);
        interval.add("Daily");
        interval.add("Weekly");
        interval.add("Monthly");
        interval.setVisibleItemCount(15);
        interval.addSelectionListener(selectionAdapter);
        
        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Spinner(content, SWT.BORDER);
        period.setMinimum(0);
        period.setMaximum(99999);
        period.addSelectionListener(selectionAdapter);
        
        label = new Label(content, SWT.NONE);
        label.setText("Type");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        type = new Combo(content, SWT.READ_ONLY);
        type.add("High");
        type.add("Low");
        type.setVisibleItemCount(15);
        type.addSelectionListener(selectionAdapter);

        description = new Label(content, SWT.NONE);
        description.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true, 2, 1));

        int field = PriceAlert.DAILY;
        String value = (String)getParameters().get("interval");
        if (value != null)
            field = Integer.parseInt(value);
        interval.select(field - PriceAlert.DAILY);
        
        value = (String)getParameters().get("period");
        period.setSelection(value != null ? Integer.parseInt(value) : 7);
        
        field = PriceAlert.HIGH;
        value = (String)getParameters().get("type");
        if (value != null)
            field = Integer.parseInt(value);
        type.select(field);
        
        updateDescription();
        
        return content;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#performOk()
     */
    public void performOk()
    {
        Map parameters = new HashMap();

        parameters.put("interval", String.valueOf(interval.getSelectionIndex() + MovingAverageCrossover.DAILY));
        parameters.put("period", String.valueOf(period.getSelection()));
        parameters.put("type", String.valueOf(type.getSelectionIndex()));
        
        setParameters(parameters);
    }

    private void updateDescription()
    {
        String s = "Price reaches a new " + String.valueOf(period.getSelection());
        switch(interval.getSelectionIndex() + PriceAlert.DAILY)
        {
            case PriceAlert.DAILY:
                s += " days";
                break; 
            case PriceAlert.WEEKLY:
                s += " weeks";
                break; 
            case PriceAlert.MONTHLY:
                s += " months";
                break; 
        }
        s += type.getSelectionIndex() == PriceAlert.HIGH ? " high" : " low";
        description.setText(s);
        description.getParent().layout();
    }
}
