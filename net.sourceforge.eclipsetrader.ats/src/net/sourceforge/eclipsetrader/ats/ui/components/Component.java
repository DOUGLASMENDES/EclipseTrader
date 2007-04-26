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

package net.sourceforge.eclipsetrader.ats.ui.components;

import org.eclipse.core.runtime.IConfigurationElement;

public class Component {
	IConfigurationElement element;

	public Component(IConfigurationElement element) {
		this.element = element;
	}

	public String getDescription() {
		return ((IConfigurationElement) element).getAttribute("name");
	}

	public void dispose() {
	}
}
