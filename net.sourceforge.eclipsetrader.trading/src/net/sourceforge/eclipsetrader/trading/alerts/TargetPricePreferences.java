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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class TargetPricePreferences extends AlertPluginPreferencePage
{
    private Combo priceField;
    private Spinner price;
    private Label currentPrice;
    private NumberFormat formatter = NumberFormat.getInstance();
    private Button cross;
    private Label description;
    private SelectionAdapter selectionAdapter = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e)
        {
            updateDescription();
        }
    };

    public TargetPricePreferences()
    {
        formatter.setGroupingUsed(true);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(4);
        formatter.setMaximumFractionDigits(4);
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
        label.setText("Price");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        Composite group = new Composite(content, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        group.setLayout(gridLayout);
        price = new Spinner(group, SWT.BORDER);
        price.setMinimum(0);
        price.setMaximum(999999999);
        price.setDigits(4);
        price.setIncrement(100);
        price.addSelectionListener(selectionAdapter);
        currentPrice = new Label(group, SWT.NONE);

        label = new Label(content, SWT.NONE);
        cross = new Button(content, SWT.CHECK);
        cross.setText("Cross");
        cross.addSelectionListener(selectionAdapter);

        description = new Label(content, SWT.NONE);
        description.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true, 2, 1));

        int field = TargetPrice.LAST;
        String value = (String)getParameters().get("field");
        if (value != null)
            field = Integer.parseInt(value);
        priceField.select(field);
        
        value = (String)getParameters().get("price");
        if (value != null)
            price.setSelection((int)Math.round(Double.parseDouble(value) * Math.pow(10, price.getDigits())));
        else if (getSecurity().getQuote() != null)
        {
            if (priceField.getSelectionIndex() == PriceChange.LAST)
                price.setSelection((int)Math.round(getSecurity().getQuote().getLast() * Math.pow(10, price.getDigits())));
            else if (priceField.getSelectionIndex() == PriceChange.BID)
                price.setSelection((int)Math.round(getSecurity().getQuote().getBid() * Math.pow(10, price.getDigits())));
            else if (priceField.getSelectionIndex() == PriceChange.ASK)
                price.setSelection((int)Math.round(getSecurity().getQuote().getAsk() * Math.pow(10, price.getDigits())));
        }

        value = (String)getParameters().get("cross");
        if (value != null)
            cross.setSelection(new Boolean(value).booleanValue());
        
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
        parameters.put("price", String.valueOf(price.getSelection() / Math.pow(10, price.getDigits())));
        parameters.put("cross", String.valueOf(cross.getSelection()));
        
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
        if (priceField.getSelectionIndex() == TargetPrice.BID)
            s = "Bid";
        else if (priceField.getSelectionIndex() == TargetPrice.ASK)
            s = "Ask";
        s += " price ";
        s += cross.getSelection() ? "crosses" : "reaches";
        description.setText(s + " " + formatter.format(price.getSelection() / Math.pow(10, price.getDigits()))); 
        description.getParent().layout();
    }
}
