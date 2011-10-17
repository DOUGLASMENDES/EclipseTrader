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

package org.eclipsetrader.ui.charts;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public interface IColorRegistry {

    /**
     * Returns an instance of Color based on the passed RGB value.
     * Color instances are cached so that subsequent requests for the same RGB
     * value returns the same Color instance.
     *
     * @param device the device
     * @param rgb the RGB value
     * @return the Color instance
     */
    public Color getColor(RGB rgb);
}
