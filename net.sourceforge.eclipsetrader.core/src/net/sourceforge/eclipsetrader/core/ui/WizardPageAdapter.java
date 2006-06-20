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

package net.sourceforge.eclipsetrader.core.ui;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public class WizardPageAdapter extends WizardPage
{
    private IPreferencePage preferencePage;
    private IPreferenceStore preferenceStore;
    private IPreferencePageContainer preferencePageContainer = new IPreferencePageContainer() {

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
         */
        public IPreferenceStore getPreferenceStore()
        {
            return preferenceStore;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
         */
        public void updateButtons()
        {
            setPageComplete(preferencePage.isValid());
            getContainer().updateButtons();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
         */
        public void updateMessage()
        {
            setMessage(preferencePage.getMessage());
            getContainer().updateMessage();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
         */
        public void updateTitle()
        {
            setTitle(preferencePage.getTitle());
            getContainer().updateTitleBar();
        }
        
    };

    public WizardPageAdapter(IPreferencePage preferencePage)
    {
        super("");
        setTitle(preferencePage.getTitle());
        setDescription(preferencePage.getDescription());
        preferencePage.setContainer(preferencePageContainer);
        setPageComplete(preferencePage.isValid());
        this.preferencePage = preferencePage;
    }

    public IPreferencePage getPreferencePage()
    {
        return preferencePage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        preferencePage.createControl(parent);
        setControl(preferencePage.getControl());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        preferencePage.setVisible(visible);
    }

    public void performFinish()
    {
        preferencePage.performOk();
    }
}
