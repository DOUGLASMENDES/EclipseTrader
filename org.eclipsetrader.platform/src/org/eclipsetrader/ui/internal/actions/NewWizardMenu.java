/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BaseNewWizardMenu;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.wizards.IWizardDescriptor;

@SuppressWarnings({
        "unchecked", "restriction"
})
public class NewWizardMenu extends BaseNewWizardMenu {

    private boolean enabled = true;

    /**
     * Creates a new wizard shortcut menu for the IDE.
     *
     * @param window - the window containing the menu
     */
    public NewWizardMenu(IWorkbenchWindow window) {
        this(window, null);

    }

    /**
     * Creates a new wizard shortcut menu for the IDE.
     *
     * @param window - the window containing the menu
     * @param id - the identifier for this contribution item
     */
    public NewWizardMenu(IWorkbenchWindow window, String id) {
        super(window, id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseNewWizardMenu#addItems(org.eclipse.jface.action.IContributionManager)
     */
    @Override
    protected void addItems(List list) {
        ArrayList shortCuts = new ArrayList();
        addShortcuts(shortCuts);

        for (Iterator iterator = shortCuts.iterator(); iterator.hasNext();) {
            Object curr = iterator.next();
            if (curr instanceof ActionContributionItem && isNewProjectWizardAction(((ActionContributionItem) curr).getAction())) {
                iterator.remove();
                list.add(curr);
            }
        }
        if (!list.isEmpty()) {
            list.add(new Separator());
        }
        if (!shortCuts.isEmpty()) {
            list.addAll(shortCuts);
            list.add(new Separator());
        }
        list.add(new ActionContributionItem(getShowDialogAction()));
    }

    private boolean isNewProjectWizardAction(IAction action) {
        if (action instanceof NewWizardShortcutAction) {
            IWizardDescriptor wizardDescriptor = ((NewWizardShortcutAction) action).getWizardDescriptor();
            String[] tags = wizardDescriptor.getTags();
            for (int i = 0; i < tags.length; i++) {
                if (WorkbenchWizardElement.TAG_PROJECT.equals(tags[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * Method declared on IContributionItem.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled state of the receiver.
     *
     * @param enabledValue if <code>true</code> the menu is enabled; else
     * 		it is disabled
     */
    public void setEnabled(boolean enabledValue) {
        this.enabled = enabledValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseNewWizardMenu#getContributionItems()
     */
    @Override
    protected IContributionItem[] getContributionItems() {
        if (isEnabled()) {
            return super.getContributionItems();
        }
        return new IContributionItem[0];
    }
}
