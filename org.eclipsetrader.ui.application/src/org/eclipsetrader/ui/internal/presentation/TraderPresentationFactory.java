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

package org.eclipsetrader.ui.internal.presentation;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * Customized presentation factory that shows the icon and close button for
 * hidden views.
 */
@SuppressWarnings("restriction")
public class TraderPresentationFactory extends WorkbenchPresentationFactory {

	private static int viewTabPosition = WorkbenchPlugin.getDefault()
			.getPreferenceStore()
			.getInt(IPreferenceConstants.VIEW_TAB_POSITION);

	public TraderPresentationFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.AbstractPresentationFactory#createViewPresentation(org.eclipse.swt.widgets.Composite, org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	@Override
    public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {

		DefaultTabFolder folder = new DefaultTabFolder(parent, viewTabPosition
				| SWT.BORDER, site
				.supportsState(IStackPresentationSite.STATE_MINIMIZED), site
				.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final int minimumCharacters = store
				.getInt(IWorkbenchPreferenceConstants.VIEW_MINIMUM_CHARACTERS);
		if (minimumCharacters >= 0) {
			folder.setMinimumCharacters(minimumCharacters);
		}

		PresentablePartFolder partFolder = new PresentablePartFolder(folder);

		folder.setUnselectedCloseVisible(true);
		folder.setUnselectedImageVisible(true);

		TabbedStackPresentation result = new TabbedStackPresentation(site,
				partFolder, new StandardViewSystemMenu(site));

		DefaultThemeListener themeListener = new DefaultThemeListener(folder,
				result.getTheme());
		result.getTheme().addListener(themeListener);

		new DefaultSimpleTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				folder);

		return result;
	}
}
