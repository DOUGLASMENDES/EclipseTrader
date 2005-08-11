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
import net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 */
public class SellArrow extends ToolPlugin
{
  private Point p1 = null;
  private Point selected = null;
  private double value1 = 0;
  private Image image;
  private ImageData imageData;
  
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
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#containsPoint(int, int)
   */
  public boolean containsPoint(int x, int y)
  {
    if (p1 == null || isMousePressed() == true)
      return true;
    
    if (Math.abs(x - (p1.x - imageData.width / 2)) <= imageData.width && Math.abs(y - (p1.y - imageData.height)) <= imageData.height)
      return true;
    
    return false;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mousePressed(org.eclipse.swt.events.MouseEvent)
   */
  public void mousePressed(MouseEvent me)
  {
    super.mousePressed(me);
    
    if (p1 == null)
    {
      p1 = new Point(me.x, me.y);
      selected = p1;
    }
    else
    {
      if (Math.abs(me.x - (p1.x - imageData.width / 2)) <= imageData.width && Math.abs(me.y - (p1.y - imageData.height)) <= imageData.height)
        selected = p1;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseDragged(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDragged(MouseEvent me)
  {
    if (isMousePressed() == true && selected != null)
    {
      getCanvas().redraw(p1.x - imageData.width / 2, p1.y - imageData.height, imageData.width, imageData.height, true);
      p1.x = me.x;
      p1.y = me.y;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseReleased(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseReleased(MouseEvent me)
  {
    super.mouseReleased(me);
    selected = null;
    
    super.setParameter("x1", String.valueOf(p1.x));
    value1 = getScaler().convertToVal(p1.y);
    super.setParameter("y1", String.valueOf(value1));
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#paintTool(org.eclipse.swt.graphics.GC)
   */
  public void paintTool(GC gc)
  {
    if (p1 != null && image != null)
      gc.drawImage(image, p1.x - imageData.width / 2, p1.y - imageData.height);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#scalerUpdate()
   */
  public void scalerUpdate()
  {
    p1.y = getScaler().convertToY(Double.parseDouble(getParameter("y1")));
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#setParameter(java.lang.String, java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("x1"))
    {
      if (p1 == null)
        p1 = new Point(0, 0);
      p1.x = Integer.parseInt(value);
    }
    else if (name.equals("y1"))
    {
      if (p1 == null)
        p1 = new Point(0, 0);
      p1.y = getScaler().convertToY(Double.parseDouble(value));
    }
    super.setParameter(name, value);
  }
}
