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

package net.sourceforge.eclipsetrader.trading.wizards.systems;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.trading.TradingSystem;
import net.sourceforge.eclipsetrader.trading.TradingPlugin;
import net.sourceforge.eclipsetrader.trading.TradingSystemPluginPreferencePage;
import net.sourceforge.eclipsetrader.trading.wizards.CommonDialogPage;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TradingSystemSettingsDialog extends PreferenceDialog
{
    private TradingSystem system;
    private BaseParametersPage baseParameters;

    public TradingSystemSettingsDialog(TradingSystem system, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.system = system;
        
        baseParameters = new BaseParametersPage() {
            public void createControl(Composite parent)
            {
                super.createControl(parent);
                setAccount(TradingSystemSettingsDialog.this.system.getAccount());
                setSecurity(TradingSystemSettingsDialog.this.system.getSecurity());
                setMaxExposure(TradingSystemSettingsDialog.this.system.getMaxExposure());
                setMaxAmount(TradingSystemSettingsDialog.this.system.getMaxAmount());
                setMinAmount(TradingSystemSettingsDialog.this.system.getMinAmount());
            }

            public void performFinish()
            {
                TradingSystemSettingsDialog.this.system.setAccount(baseParameters.getAccount());
                TradingSystemSettingsDialog.this.system.setSecurity(baseParameters.getSecurity());
                TradingSystemSettingsDialog.this.system.setMaxExposure(baseParameters.getMaxExposure());
                TradingSystemSettingsDialog.this.system.setMaxAmount(baseParameters.getMaxAmount());
                TradingSystemSettingsDialog.this.system.setMinAmount(baseParameters.getMinAmount());
            }
        };

        getPreferenceManager().addToRoot(new PreferenceNode("base", new CommonDialogPage(baseParameters) {
            protected Control createContents(Composite parent)
            {
                Control control = super.createContents(parent);
                return control;
            }
        }));

        IConfigurationElement[] members = TradingPlugin.getTradingSystemPluginPreferencePages(system.getPluginId());
        try {
            for (int i = 0; i < members.length; i++)
            {
                final TradingSystemPluginPreferencePage preferencePage = (TradingSystemPluginPreferencePage)members[i].createExecutableExtension("class");
                preferencePage.init(system.getSecurity(), system.getParameters());
                PreferencePage page = new PreferencePage() {
                    protected Control createContents(Composite parent)
                    {
                        noDefaultAndApplyButton();
                        
                        Control control = preferencePage.createContents(parent);
                        return control;
                    }

                    public boolean performOk()
                    {
                        TradingSystemSettingsDialog.this.system.getParameters().putAll(preferencePage.getParameters());
                        return super.performOk();
                    }
                };
                page.setTitle(members[i].getAttribute("name"));
                getPreferenceManager().addToRoot(new PreferenceNode("plugin" + String.valueOf(i), page));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Trading System Settings");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#okPressed()
     */
    protected void okPressed()
    {
        super.okPressed();
        CorePlugin.getRepository().save(system);
    }
}
