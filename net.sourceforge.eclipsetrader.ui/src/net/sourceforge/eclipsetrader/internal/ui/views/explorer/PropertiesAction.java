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

package net.sourceforge.eclipsetrader.internal.ui.views.explorer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class PropertiesAction extends Action {

	public PropertiesAction() {
	}

	public PropertiesAction(String text) {
		super(text);
	}

	public PropertiesAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public PropertiesAction(String text, int style) {
		super(text, style);
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
	    super.run();
    }
}
