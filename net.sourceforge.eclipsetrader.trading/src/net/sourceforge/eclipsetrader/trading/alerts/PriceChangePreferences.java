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

import java.text.NumberFormat;
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

public class PriceChangePreferences extends AlertPluginPreferencePage
{
    private Combo priceField;
    private Spinner change;
    private Spinner reference;
    private Label currentPrice;
    private Label description;
    private NumberFormat formatter = NumberFormat.getInstance();
    private NumberFormat percentFormatter = NumberFormat.getInstance();
    private SelectionAdapter selectionAdapter = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
            updateDescription();
        }
    };

    public PriceChangePreferences()
    {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(4);
        formatter.setMaximumFractionDigits(4);

        percentFormatter.setGroupingUsed(true);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(2);
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
        label.setText("Field");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        priceField = new Combo(content, SWT.READ_ONLY);
        priceField.add("Last");
        priceField.add("Bid");
        priceField.add("Ask");
        priceField.setVisibleItemCount(15);
        priceField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                updateCurrentPrice();
                updateDescription();
            }
        });

        label = new Label(content, SWT.NONE);
        label.setText("Change (%)");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        change = new Spinner(content, SWT.BORDER);
        change.setMinimum(0);
        change.setMaximum(99999);
        change.setDigits(2);
        change.addSelectionListener(selectionAdapter);
        
        label = new Label(content, SWT.NONE);
        label.setText("Reference Price");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        reference = new Spinner(group, SWT.BORDER);
        reference.setMinimum(0);
        reference.setMaximum(99999999);
        reference.setDigits(4);
        reference.setIncrement(100);
        reference.addSelectionListener(selectionAdapter);
        currentPrice = new Label(group, SWT.NONE);

        description = new Label(content, SWT.NONE);
        description.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true, 2, 1));

        int field = TargetPrice.LAST;
        String value = (String)getParameters().get("field");
        if (value != null)
            field = Integer.parseInt(value);
        priceField.select(field);
        
        value = (String)getParameters().get("change");
        if (value != null)
            change.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, change.getDigits())));
        else
            change.setSelection((int)Math.round(1 * Math.pow(10, change.getDigits())));

        value = (String)getParameters().get("reference");
        if (value != null)
            reference.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, reference.getDigits())));
        else if (getSecurity().getQuote() != null)
        {
            if (priceField.getSelectionIndex() == PriceChange.LAST)
                reference.setSelection((int)Math.round(getSecurity().getQuote().getLast() * Math.pow(10, reference.getDigits())));
            else if (priceField.getSelectionIndex() == PriceChange.BID)
                reference.setSelection((int)Math.round(getSecurity().getQuote().getBid() * Math.pow(10, reference.getDigits())));
            else if (priceField.getSelectionIndex() == PriceChange.ASK)
                reference.setSelection((int)Math.round(getSecurity().getQuote().getAsk() * Math.pow(10, reference.getDigits())));
        }
        
        updateCurrentPrice();
        updateDescription();
        
        return content;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#performOk()
     */
    public void performOk()
    {
        Map parameters = new HashMap();

        parameters.put("field", String.valueOf(priceField.getSelectionIndex()));
        parameters.put("change", String.valueOf(change.getSelection() / Math.pow(10, change.getDigits())));
        parameters.put("reference", String.valueOf(reference.getSelection() / Math.pow(10, reference.getDigits())));
        
        setParameters(parameters);
    }
    
    private void updateCurrentPrice()
    {
        if (getSecurity().getQuote() != null)
        {
            if (priceField.getSelectionIndex() == PriceChange.LAST)
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getLast()) + ")");
            else if (priceField.getSelectionIndex() == PriceChange.BID)
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getBid()) + ")");
            else if (priceField.getSelectionIndex() == PriceChange.ASK)
                currentPrice.setText("(current: " + formatter.format(getSecurity().getQuote().getAsk()) + ")");
        }
        else
            currentPrice.setText("");
        currentPrice.getParent().layout();
    }

    private void updateDescription()
    {
        String s = "Last";
        if (priceField.getSelectionIndex() == PriceChange.BID)
            s = "Bid";
        else if (priceField.getSelectionIndex() == PriceChange.ASK)
            s = "Ask";
        s += " price changes by " + percentFormatter.format(change.getSelection() / Math.pow(10, change.getDigits())) + "%";
        s += " from " + formatter.format(reference.getSelection() / Math.pow(10, reference.getDigits()));
        description.setText(s);
        description.getParent().layout();
    }
}
