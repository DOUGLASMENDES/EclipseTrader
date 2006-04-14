/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{
    public static final String PERSPECTIVE_ID = "net.sourceforge.eclipsetrader.charts";

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout)
    {
        layout.createPlaceholderFolder("left", IPageLayout.LEFT, 0.20f, IPageLayout.ID_EDITOR_AREA);
        layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, 0.80f, IPageLayout.ID_EDITOR_AREA);
    }
}
