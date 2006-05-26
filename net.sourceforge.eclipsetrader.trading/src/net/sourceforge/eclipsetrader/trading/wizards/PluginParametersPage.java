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

package net.sourceforge.eclipsetrader.trading.wizards;

import java.util.Map;

import net.sourceforge.eclipsetrader.trading.internal.wizards.IPluginParametersPage;

import org.eclipse.swt.widgets.Composite;

public class PluginParametersPage extends CommonPreferencePage
{
    private IPluginParametersPage page;

    public PluginParametersPage(IPluginParametersPage page)
    {
        this.page = page;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        setControl(page.createContents(parent));
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.wizards.CommonPreferencePage#performFinish()
     */
    public void performFinish()
    {
        page.performOk();
    }
    
    public Map getParameters()
    {
        return page.getParameters();
    }
}
