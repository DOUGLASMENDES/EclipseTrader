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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
public class BuyArrow extends ToolPlugin
{
  protected Point p1 = null;
  protected Date date1;
  protected double value1 = 0;
  protected Image image;
  protected ImageData imageData;
  protected SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
  
  public BuyArrow()
  {
    try
    {
      URL url = new URL(ViewsPlugin.getDefault().getBundle().getEntry("/"), "icons/elcl16/buyarrow.gif");
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
    
    if (Math.abs(x - (p1.x - image.getImageData().width / 2)) <= image.getImageData().width && Math.abs(y - p1.y) <= image.getImageData().height)
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
      p1 = new Point(me.x, me.y);
    
    mouseDragged(me);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseDragged(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDragged(MouseEvent me)
  {
    if (isMousePressed() == true)
    {
      getCanvas().getChart().redraw(p1.x - imageData.width / 2, p1.y, imageData.width, imageData.height, true);
      p1.x = getX(getDate(me.x));
      p1.y = me.y;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseReleased(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseReleased(MouseEvent me)
  {
    super.mouseReleased(me);
    
    date1 = getDate(p1.x);
    value1 = getScaler().convertToVal(p1.y);

    Map map = new HashMap();
    map.put("x1", df.format(date1));
    map.put("y1", String.valueOf(value1));
    setParameters(map);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#setParameter(java.lang.String,java.lang.String)
   */
  public void setParameter(String name, String value)
  {
    if (name.equals("x1"))
    {
      try {
        date1 = df.parse(value);
      } catch(Exception e) {}
    }
    else if (name.equals("y1"))
      value1 = Double.parseDouble(value);

    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#invalidate()
   */
  public void invalidate()
  {
    if (date1 != null)
    {
      if (p1 == null)
        p1 = new Point(0, 0);
      p1.x = getX(date1);
      p1.y = getScaler().convertToY(value1);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#paintTool(org.eclipse.swt.graphics.GC)
   */
  public void paintTool(GC gc)
  {
    if (p1 != null && image != null)
      gc.drawImage(image, p1.x - imageData.width / 2, p1.y);
  }
}
