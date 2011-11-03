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
import org.eclipsetrader.ui.internal.ats.explorer.ExplorerViewPart;
import org.eclipsetrader.ui.internal.ats.monitor.TradingSystemsViewPart;
import org.eclipsetrader.ui.internal.charts.views.HistoryDataEditorPart;
import org.eclipsetrader.ui.internal.editors.ScriptEditor;

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
        editors.addView(TradingSystemsViewPart.VIEW_ID);
        editors.addPlaceholder(ScriptEditor.VIEW_ID);
        editors.addPlaceholder(ScriptEditor.VIEW_ID + ":*"); //$NON-NLS-1$
        editors.addPlaceholder(ReportViewPart.VIEW_ID);
        editors.addPlaceholder(ReportViewPart.VIEW_ID + ":*"); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.editors.script"); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.editors.script:*"); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.chart"); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.chart:*"); //$NON-NLS-1$
        editors.addPlaceholder("org.eclipsetrader.ui.views.portfolio");
        editors.addPlaceholder("org.eclipsetrader.ui.views.watchlist");
        editors.addPlaceholder("org.eclipsetrader.ui.views.watchlist:*");
        editors.addPlaceholder(HistoryDataEditorPart.VIEW_ID);
        editors.addPlaceholder(HistoryDataEditorPart.VIEW_ID + ":*");

        // Left.
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.20, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        left.addView("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        left.addPlaceholder("org.eclipsetrader.ui.views.repositories"); //$NON-NLS-1$

        // Bottom
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        bottom.addPlaceholder("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$

        // Right.
        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.75, UIConstants.EDITOR_AREA); //$NON-NLS-1$
        right.addView(ExplorerViewPart.VIEW_ID);
        right.addPlaceholder("org.eclipsetrader.ui.views.level2:*"); //$NON-NLS-1$

        // Add "new wizards".
        layout.addNewWizardShortcut("org.eclipsetrader.ui.ats.scriptstrategy.wizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipsetrader.ui.wizard.script");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipsetrader.ui.wizards.new.stock");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipsetrader.ui.wizards.new.currency");//$NON-NLS-1$

        // Add "actionsets".
        layout.addActionSet("org.eclipsetrader.ui.launcher");

        // Add "show views".
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.navigator"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipsetrader.ui.views.markets"); //$NON-NLS-1$
        layout.addShowViewShortcut(TradingSystemsViewPart.VIEW_ID);
        layout.addShowViewShortcut(ExplorerViewPart.VIEW_ID);

        // Add "perspectives".
    }
}
