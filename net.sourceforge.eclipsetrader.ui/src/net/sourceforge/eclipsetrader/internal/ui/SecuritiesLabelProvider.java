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

import net.sourceforge.eclipsetrader.core.db.Security;
import net.sourceforge.eclipsetrader.core.db.SecurityGroup;
import net.sourceforge.eclipsetrader.ui.UIImageRegistry;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class SecuritiesLabelProvider extends LabelProvider {
	private Image securityImage = UIImageRegistry.getImage(UIImageRegistry.ICON_SECURITY);

	private Image securityGroupImage = UIImageRegistry.getImage(UIImageRegistry.ICON_SECURITY_GROUP);

	public SecuritiesLabelProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof Security)
			return securityImage;
		if (element instanceof SecurityGroup)
			return securityGroupImage;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof Security)
			return ((Security) element).getDescription();
		if (element instanceof SecurityGroup)
			return ((SecurityGroup) element).getDescription();
		return super.getText(element);
	}
}
