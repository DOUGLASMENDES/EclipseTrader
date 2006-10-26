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

package net.sourceforge.eclipsetrader.core.ui.dialogs;

import net.sourceforge.eclipsetrader.core.ui.preferences.IntradayDataOptions;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class IntradayChartsDialog extends TitleAreaDialog
{
    protected IntradayDataOptions intradayDataOptions;

    public IntradayChartsDialog(Shell parentShell)
    {
        super(parentShell);
        intradayDataOptions = new IntradayDataOptions();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText("Intraday Charts");
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        setTitle("Change Intraday Charts");
        setMessage("Select the options that will be applied to the selected securities");
        Control control = intradayDataOptions.createControls((Composite)super.createDialogArea(parent));
        return control;
    }
}
