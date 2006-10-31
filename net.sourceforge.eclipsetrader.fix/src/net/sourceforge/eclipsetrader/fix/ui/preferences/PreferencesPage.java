/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Cottet - Initial Release
 */

package net.sourceforge.eclipsetrader.fix.ui.preferences;

import net.sourceforge.eclipsetrader.fix.FixPlugin;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencesPage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button enableExecutor;

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

        enableExecutor = new Button(content, SWT.CHECK);
        enableExecutor.setText("Enable Executor server");
        enableExecutor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        enableExecutor.setSelection(FixPlugin.getDefault().getPreferenceStore().getBoolean(FixPlugin.PREFS_ENABLE_EXECUTOR));
        
        return content;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        FixPlugin.getDefault().getPreferenceStore().setValue(FixPlugin.PREFS_ENABLE_EXECUTOR, enableExecutor.getSelection());

        return super.performOk();
    }
}
