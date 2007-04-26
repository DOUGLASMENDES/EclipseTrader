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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

public class ComponentCategory {
	String description = "";

	Image image;

	List components = new ArrayList();

	public ComponentCategory(String description, Image image) {
		this.description = description;
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public Image getImage() {
		return image;
	}

	public List getComponents() {
		return components;
	}

	public void dispose() {
	}
}
