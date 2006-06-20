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

package net.sourceforge.eclipsetrader.trading.wizards.accounts;

import java.util.Iterator;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.Account;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Shell;

public class AccountSettingsDialog extends PreferenceDialog
{
    private Account account;
    private GeneralPage generalPage;

    public AccountSettingsDialog(Account account, Shell parentShell)
    {
        super(parentShell, new PreferenceManager());
        this.account = account;

        generalPage = new GeneralPage(account);
        getPreferenceManager().addToRoot(new PreferenceNode("general", generalPage));

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(CorePlugin.ACCOUNT_PROVIDERS_EXTENSION_POINT);
        if (extensionPoint != null)
        {
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for (int i = 0; i < members.length; i++)
            {
                if (members[i].getAttribute("id").equals(account.getPluginId())) //$NON-NLS-1$
                {
                    members = members[i].getChildren("preferencePage");
                    for (int ii = 0; ii < members.length; ii++)
                    {
                        try
                        {
                            PreferencePage preferencePage = (PreferencePage) members[ii].createExecutableExtension("class");
                            if (members[ii].getAttribute("name") != null)
                                preferencePage.setTitle(members[ii].getAttribute("name"));
                            preferencePage.setPreferenceStore(account.getPreferenceStore());
                            getPreferenceManager().addToRoot(new PreferenceNode("page" + String.valueOf(ii), preferencePage));
                        }
                        catch (Exception e)
                        {
                            CorePlugin.logException(e);
                        }
                    }
                    break;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Account Properties");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#okPressed()
     */
    protected void okPressed()
    {
        SafeRunnable.run(new SafeRunnable() {
            private boolean errorOccurred;

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.ISafeRunnable#run()
             */
            public void run()
            {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
                errorOccurred = false;
                boolean hasFailedOK = false;
                try
                {
                    // Notify all the pages and give them a chance to abort
                    Iterator nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
                    while (nodes.hasNext())
                    {
                        IPreferenceNode node = (IPreferenceNode) nodes.next();
                        IPreferencePage page = node.getPage();
                        if (page != null && page.getControl() != null)
                        {
                            if (!page.performOk())
                            {
                                hasFailedOK = true;
                                return;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    handleException(e);
                }
                finally
                {
                    //Don't bother closing if the OK failed
                    if (hasFailedOK)
                        return;

                    if (!errorOccurred)
                        //Give subclasses the choice to save the state of the
                        //preference pages.
                        handleSave();

                    close();
                }
            }

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
             */
            public void handleException(Throwable e)
            {
                errorOccurred = true;

                Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, 0, e.toString(), e));

                setSelectedNodePreference(null);
                String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$
                MessageDialog.openError(getShell(), JFaceResources.getString("Error"), message); //$NON-NLS-1$

            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferenceDialog#handleSave()
     */
    protected void handleSave()
    {
        CorePlugin.getRepository().save(account);
        super.handleSave();
    }
}
