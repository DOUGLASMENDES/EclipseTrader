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

package net.sourceforge.eclipsetrader.ui;

import net.sourceforge.eclipsetrader.internal.ui.Activator;

import org.eclipse.swt.graphics.Image;

public class UIImageRegistry {
	public static final String ICON_SECURITY = "SECURITY"; //$NON-NLS-1$
	public static final String ICON_SECURITY_GROUP = "SECURITY_GROUP"; //$NON-NLS-1$

	private UIImageRegistry() {
	}

	public static Image getImage(String key) {
		return Activator.getDefault().getImageRegistry().get(key);
	}
}
