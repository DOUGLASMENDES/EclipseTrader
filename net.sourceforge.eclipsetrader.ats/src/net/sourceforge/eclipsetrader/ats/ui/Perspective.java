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

package net.sourceforge.eclipsetrader.ats.ui;

import net.sourceforge.eclipsetrader.ats.ui.report.BacktestReportView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	public static final String PERSPECTIVE_ID = "net.sourceforge.eclipsetrader.ats";

	public static final String EDITOR_AREA = "center";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		IFolderLayout folder = layout.createFolder(EDITOR_AREA, IPageLayout.RIGHT, 0, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout leftFolder = layout.createFolder("left", IPageLayout.LEFT, 0.18f, EDITOR_AREA);
		IFolderLayout bottomFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.80f, EDITOR_AREA);
		IFolderLayout rightFolder = layout.createFolder("right", IPageLayout.RIGHT, 0.78f, EDITOR_AREA);

		folder.addView("net.sourceforge.eclipsetrader.ats.viewer");
		leftFolder.addView("net.sourceforge.eclipsetrader.views.securities");
		rightFolder.addView("net.sourceforge.eclipsetrader.ats.components");
		bottomFolder.addView("net.sourceforge.eclipsetrader.trading.orders");

		IFolderLayout middleFolder = layout.createFolder("middle", IPageLayout.BOTTOM, 0.60f, EDITOR_AREA);
		middleFolder.addView(BacktestReportView.VIEW_ID);

		layout.addShowViewShortcut("net.sourceforge.eclipsetrader.ats.components");
		layout.addShowViewShortcut("net.sourceforge.eclipsetrader.views.securities");
		layout.addShowViewShortcut("net.sourceforge.eclipsetrader.ats.viewer");
		layout.addShowViewShortcut("net.sourceforge.eclipsetrader.trading.orders");
	}
}
