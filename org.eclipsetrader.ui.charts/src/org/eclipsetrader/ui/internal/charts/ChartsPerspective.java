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
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipsetrader.ui.UIConstants;

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
		IFolderLayout editorsFolder = layout.createFolder(UIConstants.EDITOR_AREA, IPageLayout.LEFT, (float) 100.0, layout.getEditorArea());
		editorsFolder.addPlaceholder("org.eclipsetrader.ui.chart"); //$NON-NLS-1$
		editorsFolder.addPlaceholder("org.eclipsetrader.ui.chart:*"); //$NON-NLS-1$
		editorsFolder.addPlaceholder("org.eclipsetrader.ui.views.watchlist");
		editorsFolder.addPlaceholder("org.eclipsetrader.ui.views.watchlist:*");
		editorsFolder.addPlaceholder("org.eclipsetrader.news.browser");
		editorsFolder.addPlaceholder("org.eclipsetrader.news.browser:*");

		// Left.
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.20, UIConstants.EDITOR_AREA); //$NON-NLS-1$ 
		left.addView("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
		left.addPlaceholder("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$
		layout.addView("org.eclipsetrader.ui.charts.palette", IPageLayout.BOTTOM, (float) 0.50, "left"); //$NON-NLS-1$ //$NON-NLS-2$

		// Bottom
		IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, UIConstants.EDITOR_AREA); //$NON-NLS-1$ 
		bottom.addPlaceholder("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$

		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipsetrader.ui.wizards.new.security");//$NON-NLS-1$

		// Add "actionsets".
		layout.addActionSet("org.eclipsetrader.ui.charts.trigger"); //$NON-NLS-1$
		layout.addActionSet("org.eclipsetrader.ui.charts.tools"); //$NON-NLS-1$
		layout.addActionSet("org.eclipsetrader.ui.charts.zoom"); //$NON-NLS-1$

		// Add "show views".
		layout.addShowViewShortcut("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
		layout.addShowViewShortcut("org.eclipsetrader.ui.views.markets"); //$NON-NLS-1$
		layout.addShowViewShortcut("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

		// Add "perspectives".
		layout.addPerspectiveShortcut("org.eclipsetrader.ui.traderPerspective"); //$NON-NLS-1$
	}
}
