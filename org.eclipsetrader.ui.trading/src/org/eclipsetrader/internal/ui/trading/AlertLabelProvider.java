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

package org.eclipsetrader.internal.ui.trading;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.trading.IAlert;

public class AlertLabelProvider extends BaseLabelProvider implements ILabelProvider {

    public AlertLabelProvider() {
    }

    @Override
    public String getText(Object element) {
        return ((IAlert) element).getDescription();
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IAdaptable) {
            return (Image) ((IAdaptable) element).getAdapter(Image.class);
        }
        return null;
    }
}
