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

package org.eclipsetrader.ui.internal.repositories;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;

public class RepositoryLabelProvider extends LabelProvider implements IFontProvider {
	private Font repositoryFont;
	private Font categoryFont;
	private Image repositoryIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.REPOSITORY);
	private Image securityFolderIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.REPOSITORY_OBJECT_FOLDER);
	private Image securityIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.REPOSITORY_OBJECT);

	public RepositoryLabelProvider() {
		FontData[] fontData = Display.getDefault().getSystemFont().getFontData();
		if (fontData != null && fontData.length != 0) {
			repositoryFont = new Font(Display.getDefault(), fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD);
			categoryFont = new Font(Display.getDefault(), fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
    	if (repositoryFont != null)
    		repositoryFont.dispose();
    	if (categoryFont != null)
    		categoryFont.dispose();
	    super.dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
    	if (element instanceof IAdaptable)
    		element = ((IAdaptable) element).getAdapter(Object.class);

		if (element instanceof IRepository)
			return repositoryIcon;
		if (element instanceof ISecurity || element instanceof IWatchList)
			return securityIcon;
		if (element instanceof SecurityContainerObject || element instanceof WatchListContainerObject)
			return securityFolderIcon;

		return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
    	if (element instanceof IAdaptable)
    		element = ((IAdaptable) element).getAdapter(Object.class);

		if (element instanceof IRepository)
			return ((IRepository) element).toString();
		if (element instanceof ISecurity)
			return ((ISecurity) element).getName();
		if (element instanceof IWatchList)
			return ((IWatchList) element).getName();
		return element != null ? element.toString() : ""; //$NON-NLS-1$
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
    	if (element instanceof IAdaptable)
    		element = ((IAdaptable) element).getAdapter(Object.class);

		if (element instanceof IRepository)
			return repositoryFont;
		if (element instanceof SecurityContainerObject || element instanceof WatchListContainerObject)
			return categoryFont;

		return null;
    }
}
