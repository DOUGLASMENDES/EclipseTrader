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

package net.sourceforge.eclipsetrader.internal.ui;

import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.swt.graphics.Image;

public class SecuritiesTreeLabelDecorator extends LabelDecorator {

	public SecuritiesTreeLabelDecorator() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object, org.eclipse.jface.viewers.IDecorationContext)
	 */
	@Override
	public Image decorateImage(Image image, Object element, IDecorationContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelDecorator#decorateText(java.lang.String, java.lang.Object, org.eclipse.jface.viewers.IDecorationContext)
	 */
	@Override
	public String decorateText(String text, Object element, IDecorationContext context) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelDecorator#prepareDecoration(java.lang.Object, java.lang.String, org.eclipse.jface.viewers.IDecorationContext)
	 */
	@Override
	public boolean prepareDecoration(Object element, String originalText, IDecorationContext context) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
