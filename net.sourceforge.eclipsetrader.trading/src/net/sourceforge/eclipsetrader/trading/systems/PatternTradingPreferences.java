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

package net.sourceforge.eclipsetrader.trading.systems;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.trading.TradingSystemPluginPreferencePage;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class PatternTradingPreferences extends TradingSystemPluginPreferencePage
{
    private Combo pattern;
    private Combo period;
    private Spinner bars;

    public PatternTradingPreferences()
    {
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.TradingSystemPluginPreferencePage#init(net.sourceforge.eclipsetrader.core.db.Security, java.util.Map)
     */
    public void init(Security security, Map params)
    {
        super.init(security, params);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText("Pattern");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        pattern = new Combo(content, SWT.READ_ONLY);
        pattern.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        label = new Label(content, SWT.NONE);
        label.setText("Period");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        period = new Combo(content, SWT.READ_ONLY);
        period.add("Daily");
        period.add("Weekly");
        period.add("Monthly");
        if (getParameters().get("period") != null)
            period.select(Integer.parseInt((String)getParameters().get("period")));
        else
            period.select(1);

        label = new Label(content, SWT.NONE);
        label.setText("Bars");
        label.setLayoutData(new GridData(125, SWT.DEFAULT));
        bars = new Spinner(content, SWT.BORDER);
        bars.setMinimum(0);
        bars.setMaximum(9999);
        if (getParameters().get("bars") != null)
            bars.setSelection(Integer.parseInt((String)getParameters().get("bars")));

        for (Iterator iter = CorePlugin.getAllPatternPlugins().iterator(); iter.hasNext(); )
        {
            IConfigurationElement element = (IConfigurationElement)iter.next();
            pattern.add(element.getAttribute("name"));
            pattern.setData(element.getAttribute("name"), element.getAttribute("id"));
            if (element.getAttribute("id").equals(getParameters().get("pattern")))
                pattern.select(pattern.getItemCount() - 1);
        }
        if (pattern.getSelectionIndex() == -1)
            pattern.select(0);
        
        return content;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.AlertPluginPreferencePage#performOk()
     */
    public void performOk()
    {
        Map params = new HashMap();

        if (pattern != null)
            params.put("pattern", (String) pattern.getData(pattern.getText()));
        if (period != null)
            params.put("period", String.valueOf(period.getSelectionIndex()));
        if (bars != null)
            params.put("bars", String.valueOf(bars.getSelection()));
        
        setParameters(params);
    }
}
