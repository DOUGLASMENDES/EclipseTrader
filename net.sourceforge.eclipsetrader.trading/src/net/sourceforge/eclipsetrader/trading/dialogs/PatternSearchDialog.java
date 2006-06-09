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

package net.sourceforge.eclipsetrader.trading.dialogs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;

public abstract class PatternSearchDialog extends Dialog
{
    protected IViewPart view;
    protected Combo period;
    protected Combo pattern;
    protected Button bullishOnly;
    protected Text begin;
    protected Text end;
    protected Button allOccurrences;
    protected SimpleDateFormat dateFormat = CorePlugin.getDateFormat();
    protected SimpleDateFormat dateParse = CorePlugin.getDateParse();
    protected static Map last = new HashMap();

    public PatternSearchDialog(Shell parentShell)
    {
        super(parentShell);
    }

    public PatternSearchDialog(IViewPart view)
    {
        super(view.getViewSite().getShell());
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Pattern Search");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite content)
    {
        Label label = new Label(content, SWT.NONE);
        label.setText("Pattern");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        pattern = new Combo(content, SWT.READ_ONLY);
        pattern.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        pattern.add("All");
        for (Iterator iter = CorePlugin.getAllPatternPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            pattern.add(element.getAttribute("name"));
            pattern.setData(element.getAttribute("name"), element.getAttribute("id"));
        }
        if (last.get("pattern") != null)
            pattern.setText((String)last.get("pattern"));
        else
            pattern.select(0);
        
        label = new Label(content, SWT.NONE);
        label.setText("Begin Date");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        begin = new Text(content, SWT.BORDER);
        begin.setLayoutData(new GridData(80, SWT.DEFAULT));
        begin.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                if (begin.getText().length() != 0)
                    try {
                        Date date = dateParse.parse(begin.getText());
                        begin.setText(dateFormat.format(date));
                    } catch(Exception e1) {
                        begin.setText("");
                        CorePlugin.logException(e1);
                    }
            }
        });
        if (last.get("begin") != null)
            begin.setText((String)last.get("begin"));
        
        label = new Label(content, SWT.NONE);
        label.setText("End Date");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        end = new Text(content, SWT.BORDER);
        end.setLayoutData(new GridData(80, SWT.DEFAULT));
        end.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                if (end.getText().length() != 0)
                    try {
                        Date date = dateParse.parse(end.getText());
                        end.setText(dateFormat.format(date));
                    } catch(Exception e1) {
                        end.setText("");
                        CorePlugin.logException(e1);
                    }
            }
        });
        if (last.get("end") != null)
            end.setText((String)last.get("end"));

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add("Daily");
        period.add("Weekly");
        period.add("Monthly");
        if (last.get("period") != null)
            period.setText((String)last.get("period"));
        else
            period.select(1);

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        bullishOnly = new Button(content, SWT.CHECK);
        bullishOnly.setText("Bullish Only");
        if (last.get("bullishOnly") != null)
            bullishOnly.setSelection(((Boolean) last.get("bullishOnly")).booleanValue());

        label = new Label(content, SWT.NONE);
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        allOccurrences = new Button(content, SWT.CHECK);
        allOccurrences.setText("Search all occurrences");
        if (last.get("allOccurrences") != null)
            allOccurrences.setSelection(((Boolean) last.get("allOccurrences")).booleanValue());
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    public void cancelPressed()
    {
        super.cancelPressed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    public void okPressed()
    {
        last.put("pattern", pattern.getText());
        last.put("begin", begin.getText());
        last.put("end", end.getText());
        last.put("period", period.getText());
        last.put("bullishOnly", new Boolean(bullishOnly.getSelection()));
        last.put("allOccurrences", new Boolean(allOccurrences.getSelection()));

        super.okPressed();
    }
}
