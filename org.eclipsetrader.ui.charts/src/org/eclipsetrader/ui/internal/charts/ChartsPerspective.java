/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ChartsPerspective implements IPerspectiveFactory {

	public ChartsPerspective() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		// Editors are not needed
		layout.setEditorAreaVisible(false);

		// Our editor area
        IFolderLayout editors = layout.createFolder("org.eclipsetrader.ui.editorss", IPageLayout.LEFT, (float) 100.0, layout.getEditorArea()); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.chart:*");

        // Left.
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.20, "org.eclipsetrader.ui.editorss"); //$NON-NLS-1$
        left.addView("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        left.addPlaceholder("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

        // Add "new wizards".
		layout.addNewWizardShortcut("org.eclipsetrader.ui.wizards.new.security");//$NON-NLS-1$

        // Add "actionsets".
        layout.addActionSet("org.eclipsetrader.ui.charts.trigger");

        // Add "show views".
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.markets"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

        // Add "perspectives".
        layout.addPerspectiveShortcut("org.eclipsetrader.ui.traderPerspective"); //$NON-NLS-1$
	}
}
