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

package net.sourceforge.eclipsetrader.charts.dialogs;

import net.sourceforge.eclipsetrader.charts.IndicatorPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.ChartIndicator;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class IndicatorPage extends PreferencePage
{
    private ChartIndicator indicator;
    private IndicatorPluginPreferencePage page;

    public IndicatorPage(ChartIndicator indicator, IndicatorPluginPreferencePage page)
    {
        super(page.getTitle());
        this.indicator = indicator;
        this.page = page;
        noDefaultAndApplyButton();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        page.createControl(parent);
        return page.getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        page.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        if (page.isPageComplete() && page.getControl() != null)
        {
            page.setSettings(new Settings());
            page.performFinish();
            indicator.setParameters(page.getSettings().getMap());
        }
        return page.isPageComplete();
    }
}
