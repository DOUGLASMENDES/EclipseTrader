/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

/**
 * The default trader perspective
 */
public class TraderPerspective implements IPerspectiveFactory
{

  public TraderPerspective()
  {
  }

  public void createInitialLayout(IPageLayout layout)
  {
    layout.setEditorAreaVisible(false);

    IPlaceholderFolderLayout folder = layout.createPlaceholderFolder("strips", IPageLayout.TOP, 0.11f, IPageLayout.ID_EDITOR_AREA);

    folder = layout.createPlaceholderFolder("trading", IPageLayout.TOP, 0.17f, IPageLayout.ID_EDITOR_AREA);

    folder = layout.createPlaceholderFolder("book", IPageLayout.LEFT, 0.30f, IPageLayout.ID_EDITOR_AREA);
    folder.addPlaceholder("net.sourceforge.eclipsetrader.ui.views.Book:*");

    folder = layout.createPlaceholderFolder("rtcharts", IPageLayout.BOTTOM, 0.50f, "book");
    folder.addPlaceholder("net.sourceforge.eclipsetrader.ui.views.RealtimeChart:*");

    folder = layout.createPlaceholderFolder("news", IPageLayout.BOTTOM, 0.75f, IPageLayout.ID_EDITOR_AREA);
  }
}
