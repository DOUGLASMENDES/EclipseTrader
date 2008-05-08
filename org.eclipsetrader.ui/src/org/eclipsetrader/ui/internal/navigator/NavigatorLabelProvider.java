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

package org.eclipsetrader.ui.internal.navigator;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.views.IWatchList;
import org.eclipsetrader.ui.UIConstants;
import org.eclipsetrader.ui.internal.UIActivator;

@SuppressWarnings("restriction")
public class NavigatorLabelProvider extends LabelProvider implements IColorProvider {
	private Image folderIcon;
	private Image blankIcon;
	private Image marketIcon;
	private Image securityIcon;
	private Image watchListIcon;

	public NavigatorLabelProvider() {
		if (UIActivator.getDefault() != null) {
			folderIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.FOLDER_OBJECT);
			blankIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.BLANK_OBJECT);
			marketIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.MARKET_FOLDER);
			securityIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.REPOSITORY_OBJECT);
			watchListIcon = UIActivator.getDefault().getImageRegistry().get(UIConstants.REPOSITORY_OBJECT);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
    	NavigatorViewItem viewItem = (NavigatorViewItem) element;

    	ILabelProvider labelProvider = (ILabelProvider) AdapterManager.getDefault().getAdapter(
    			viewItem.getReference(),
    			ILabelProvider.class);
    	if (labelProvider != null)
    		return labelProvider.getImage(element);

    	if (viewItem.getReference() instanceof IMarket)
    		return marketIcon;
    	if (viewItem.getReference() instanceof ISecurity)
    		return securityIcon;
    	if (viewItem.getReference() instanceof IWatchList)
    		return watchListIcon;

    	if (viewItem.getItemCount() != 0)
    		return folderIcon;

    	return blankIcon;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
    	NavigatorViewItem viewItem = (NavigatorViewItem) element;

    	ILabelProvider labelProvider = (ILabelProvider) AdapterManager.getDefault().getAdapter(
    			viewItem.getReference(),
    			ILabelProvider.class);
    	if (labelProvider != null)
    		return labelProvider.getText(element);

    	if (viewItem.getReference() instanceof IMarket)
			return ((IMarket) element).getName();
    	if (viewItem.getReference() instanceof ISecurity)
			return ((ISecurity) element).getName();
    	if (viewItem.getReference() instanceof IWatchList)
			return ((IWatchList) element).getName();

    	return super.getText(element);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
    	IColorProvider colorProvider = (IColorProvider) AdapterManager.getDefault().getAdapter(
    			((NavigatorViewItem) element).getReference(),
    			IColorProvider.class);
    	if (colorProvider != null)
    		return colorProvider.getBackground(element);

    	return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
    	IColorProvider colorProvider = (IColorProvider) AdapterManager.getDefault().getAdapter(
    			((NavigatorViewItem) element).getReference(),
    			IColorProvider.class);
    	if (colorProvider != null)
    		return colorProvider.getForeground(element);

    	return null;
    }
}
