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
package net.sourceforge.eclipsetrader.ui.internal.views;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Images
{
  static final URL BASE_URL = ViewsPlugin.getDefault().getBundle().getEntry("/");
  public static final ImageDescriptor ICON_REFRESH;
  public static final ImageDescriptor ICON_AUTOREFRESH;

  static {
    String iconPath = "icons/";

    ICON_REFRESH = createImageDescriptor(iconPath + "refresh.gif");
    ICON_AUTOREFRESH = createImageDescriptor(iconPath + "auto_refresh.gif");
  }

  /**
   * Utility method to create an <code>ImageDescriptor</code> from a path to a file.
   * @param path
   * 
   * @return
   */
  private static ImageDescriptor createImageDescriptor(String path)
  {
    try
    {
      URL url = new URL(BASE_URL, path);

      return ImageDescriptor.createFromURL(url);
    } catch (MalformedURLException e)
    {
    }

    return ImageDescriptor.getMissingImageDescriptor();
  }

}
