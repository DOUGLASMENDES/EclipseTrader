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

package net.sourceforge.eclipsetrader.charts.preferences;

import net.sourceforge.eclipsetrader.charts.ChartsPlugin;
import net.sourceforge.eclipsetrader.charts.views.ChartView;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ChartAppearancePrefereces extends PreferencePage implements IWorkbenchPreferencePage
{
    Button never;
    Button onlyOne;

    public ChartAppearancePrefereces()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        content.setLayout(gridLayout);

        Group group = new Group(content, SWT.NONE);
        group.setText("Hide tabs");
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        group.setLayout(new GridLayout(3, false));
        
        never = new Button(group, SWT.RADIO);
        never.setText("Never");
        onlyOne = new Button(group, SWT.RADIO);
        onlyOne.setText("When only one tab is shown");
        
        performDefaults();

        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();
        if (never.getSelection())
            pluginPreferences.setValue(ChartsPlugin.PREFS_HIDE_TABS, ChartView.HIDE_TABS_NEVER);
        else if (onlyOne.getSelection())
            pluginPreferences.setValue(ChartsPlugin.PREFS_HIDE_TABS, ChartView.HIDE_TABS_ONLYONE);

        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
        IPreferenceStore pluginPreferences = ChartsPlugin.getDefault().getPreferenceStore();
        int autoHideTabs = pluginPreferences.getInt(ChartsPlugin.PREFS_HIDE_TABS);
        never.setSelection(autoHideTabs == ChartView.HIDE_TABS_NEVER);
        onlyOne.setSelection(autoHideTabs == ChartView.HIDE_TABS_ONLYONE);

        super.performDefaults();
    }
}
