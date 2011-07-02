/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.application;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipsetrader.ui.internal.UIActivator;

@SuppressWarnings("restriction")
public class TraderWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public static final String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "EXIT_PROMPT_ON_CLOSE_LAST_WINDOW"; //$NON-NLS-1$

    public TraderWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new TraderActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowMenuBar(true);
        configurer.setShowCoolBar(true);
        configurer.setShowPerspectiveBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);

        configurer.getWindow().addPageListener(new IPageListener() {

            @Override
            public void pageActivated(IWorkbenchPage page) {
                updateTitle();
            }

            @Override
            public void pageClosed(IWorkbenchPage page) {
                updateTitle();
            }

            @Override
            public void pageOpened(IWorkbenchPage page) {
            }
        });
        configurer.getWindow().addPerspectiveListener(new PerspectiveAdapter() {

            @Override
            public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                updateTitle();
            }

            @Override
            public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
                updateTitle();
            }

            @Override
            public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
                updateTitle();
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowShellClose()
     */
    @Override
    public boolean preWindowShellClose() {
        if (getWorkbench().getWorkbenchWindowCount() > 1) {
            return true;
        }

        IPreferenceStore preferenceStore = UIActivator.getDefault().getPreferenceStore();
        boolean promptOnExit = preferenceStore.getBoolean(EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

        if (promptOnExit) {
            MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(getWindowConfigurer().getWindow().getShell(), "Confirm Exit", "Exit EclipseTrader ?", "Always exit without prompt", false, null, null);
            if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
                return false;
            }

            if (dlg.getToggleState()) {
                preferenceStore.setValue(EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, false);
                UIActivator.getDefault().savePluginPreferences();
            }
        }

        return true;
    }

    /**
     * Returns the workbench.
     *
     * @return the workbench
     */
    private IWorkbench getWorkbench() {
        return getWindowConfigurer().getWorkbenchConfigurer().getWorkbench();
    }

    private String computeTitle() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        IWorkbenchPage currentPage = configurer.getWindow().getActivePage();

        String title = null;
        IProduct product = Platform.getProduct();
        if (product != null) {
            title = product.getName();
        }
        if (title == null) {
            title = "";
        }

        if (currentPage != null) {
            IPerspectiveDescriptor persp = currentPage.getPerspective();
            if (persp != null) {
                if (!persp.getLabel().equals("") && !persp.getLabel().equals(title)) {
                    title += " - " + persp.getLabel();
                }
            }
        }

        return title;
    }

    private void updateTitle() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        String oldTitle = configurer.getTitle();
        String newTitle = computeTitle();
        if (!newTitle.equals(oldTitle)) {
            configurer.setTitle(newTitle);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#isDurableFolder(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isDurableFolder(String perspectiveId, String folderId) {
        if ("org.eclipsetrader.ui.editorss".equals(folderId)) {
            return true;
        }

        return super.isDurableFolder(perspectiveId, folderId);
    }
}
