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

import net.sourceforge.eclipsetrader.core.ui.preferences.TradeSourceOptions;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TradingOptionsDialog extends TitleAreaDialog
{
    protected TradeSourceOptions options;

    public TradingOptionsDialog(Shell parentShell)
    {
        super(parentShell);
        options = new TradeSourceOptions() {
            public Composite createControls(Composite parent)
            {
                Composite composite = super.createControls(parent);
                symbol.setEnabled(false);
                return composite;
            }
            protected void updateEnablement()
            {
                super.updateEnablement();
                symbol.setEnabled(false);
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        newShell.setText("Trading Options");
        super.configureShell(newShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        setTitle("Change Trading Options");
        setMessage("Select the trading options that will be applied to the selected securities");
        Control control = options.createControls((Composite)super.createDialogArea(parent));
        return control;
    }
}
