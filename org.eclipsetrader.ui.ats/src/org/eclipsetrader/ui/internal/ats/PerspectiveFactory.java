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

package org.eclipsetrader.ui.internal.ats;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipsetrader.ui.UIConstants;

public class PerspectiveFactory implements IPerspectiveFactory {

    public PerspectiveFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    @Override
    public void createInitialLayout(IPageLayout layout) {
        // Editors are not needed
        layout.setEditorAreaVisible(false);

        // Our editor area
        IFolderLayout editors = layout.createFolder(UIConstants.EDITOR_AREA, IPageLayout.LEFT, (float) 100.0, layout.getEditorArea());
        editors.addPlaceholder("org.eclipsetrader.ui.chart:*"); //$NON-NLS-1$

        // Left.
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.20, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        left.addView("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        left.addPlaceholder("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

        // Bottom
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        bottom.addPlaceholder("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$

        // Right.
        IPlaceholderFolderLayout right = layout.createPlaceholderFolder("right", IPageLayout.RIGHT, (float) 0.75, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        right.addPlaceholder("org.eclipsetrader.ui.views.level2:*"); //$NON-NLS-1$

        // Add "new wizards".
        layout.addNewWizardShortcut("org.eclipsetrader.ui.wizards.new.security");//$NON-NLS-1$

        // Add "actionsets".

        // Add "show views".
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.markets"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

        // Add "perspectives".
    }
}
