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

import net.sourceforge.eclipsetrader.charts.ObjectPluginPreferencePage;
import net.sourceforge.eclipsetrader.charts.Settings;
import net.sourceforge.eclipsetrader.core.db.ChartObject;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ObjectPage extends PreferencePage
{
    private ChartObject object;
    private ObjectPluginPreferencePage page;

    public ObjectPage(ChartObject indicator, ObjectPluginPreferencePage page)
    {
        super(page.getTitle());
        this.object = indicator;
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
            object.setParameters(page.getSettings().getMap());
        }
        return page.isPageComplete();
    }
}
