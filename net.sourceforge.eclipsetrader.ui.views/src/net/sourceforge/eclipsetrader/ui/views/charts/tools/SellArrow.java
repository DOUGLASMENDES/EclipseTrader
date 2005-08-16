/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts.tools;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

/**
 */
public class SellArrow extends BuyArrow
{
  
  public SellArrow()
  {
    try
    {
      URL url = new URL(ViewsPlugin.getDefault().getBundle().getEntry("/"), "icons/elcl16/sellarrow.gif");
      ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
      image = descriptor.createImage();
      imageData = descriptor.getImageData();
    } catch (MalformedURLException e) {}
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseDragged(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDragged(MouseEvent me)
  {
    if (isMousePressed() == true)
    {
      getCanvas().getChart().redraw(p1.x - imageData.width / 2, p1.y - imageData.height, imageData.width, imageData.height, true);
      p1.x = getX(getDate(me.x));
      p1.y = me.y;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#paintTool(org.eclipse.swt.graphics.GC)
   */
  public void paintTool(GC gc)
  {
    if (p1 != null && image != null)
      gc.drawImage(image, p1.x - imageData.width / 2, p1.y - imageData.height);
  }
}
