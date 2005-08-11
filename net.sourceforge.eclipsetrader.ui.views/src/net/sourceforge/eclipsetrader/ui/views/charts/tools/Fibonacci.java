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

import net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class Fibonacci extends ToolPlugin
{
  private Point p1 = null;
  private Point p2 = null;
  private Point selected = null;
  private Color color = new Color(null, 0, 0, 0);
  private double[] retrace = { 1, 1, 2, 3, 5, 8 };
  private double value1 = 0, value2 = 0;

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#containsPoint(int, int)
   */
  public boolean containsPoint(int x, int y)
  {
    if (p1 == null || p2 == null || isMousePressed() == true)
      return true;
    
    if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p1.y) <= 2)
      return true;
    else if (Math.abs(x - p2.x) <= 2 && Math.abs(y - p1.y) <= 2)
      return true;
    else if (Math.abs(x - p2.x) <= 2 && Math.abs(y - p2.y) <= 2)
      return true;
    else if (Math.abs(x - p1.x) <= 2 && Math.abs(y - p2.y) <= 2)
      return true;
    
    return false;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mousePressed(org.eclipse.swt.events.MouseEvent)
   */
  public void mousePressed(MouseEvent me)
  {
    super.mousePressed(me);
    
    if (p1 == null && p2 == null)
    {
      p1 = new Point(me.x, me.y);
      p2 = new Point(me.x, me.y);
      selected = p2;
    }
    else
    {
      if (Math.abs(me.x - p1.x) <= 2 && Math.abs(me.y - p1.y) <= 2)
        selected = p1;
      else if (Math.abs(me.x - p2.x) <= 2 && Math.abs(me.y - p1.y) <= 2)
      {
        int x = p1.x;
        p1.x = p2.x;
        p2.x = x;
        selected = p1;
      }
      else if (Math.abs(me.x - p2.x) <= 2 && Math.abs(me.y - p2.y) <= 2)
        selected = p2;
      else if (Math.abs(me.x - p1.x) <= 2 && Math.abs(me.y - p2.y) <= 2)
      {
        int y = p1.y;
        p1.y = p2.y;
        p2.y = y;
        selected = p2;
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseDragged(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDragged(MouseEvent me)
  {
    if (selected != null)
    {
      getCanvas().redraw(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x - p2.x) + 1, Math.abs(p1.y - p2.y) + 1, true);
      selected.x = me.x;
      selected.y = me.y;
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#mouseReleased(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseReleased(MouseEvent me)
  {
    super.mouseReleased(me);
    selected = null;
    
    setParameter("x1", String.valueOf(p1.x));
    value1 = getScaler().convertToVal(p1.y);
    setParameter("y1", String.valueOf(value1));
    
    setParameter("x2", String.valueOf(p2.x));
    value2 = getScaler().convertToVal(p2.y);
    setParameter("y2", String.valueOf(value2));
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#scalerUpdate()
   */
  public void scalerUpdate()
  {
    p1.y = getScaler().convertToY(Double.parseDouble(getParameter("y1")));
    p2.y = getScaler().convertToY(Double.parseDouble(getParameter("y2")));
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#paintTool(org.eclipse.swt.graphics.GC)
   */
  public void paintTool(GC gc)
  {
    if (p1 != null && p2 != null)
    {
      double total = 0;
      for (int i = 0; i < retrace.length; i++)
        total += retrace[i];

      double step = Math.abs(p1.y - p2.y) / total;

      gc.setForeground(color);
      gc.drawLine(p1.x, p1.y, p2.x, p1.y);
      
      if (p1.y < p2.y)
      {
        double y = p1.y;
        for (int i = 0; i < retrace.length - 1 && y < p2.y; i++)
        {
          y += step * retrace[i];
          gc.drawLine(p1.x, (int)Math.round(y), p2.x, (int)Math.round(y));
        }
      }
      else
      {
        double y = p2.y;
        for (int i = 0; i < retrace.length - 1 && y < p1.y; i++)
        {
          y += step * retrace[i];
          gc.drawLine(p1.x, (int)Math.round(y), p2.x, (int)Math.round(y));
        }
      }

      gc.drawLine(p1.x, p2.y, p2.x, p2.y);
    }
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
    else if (name.equals("x2"))
    {
      if (p2 == null)
        p2 = new Point(0, 0);
      p2.x = Integer.parseInt(value);
    }
    else if (name.equals("y2"))
    {
      if (p2 == null)
        p2 = new Point(0, 0);
      p2.y = getScaler().convertToY(Double.parseDouble(value));
    }
    super.setParameter(name, value);
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ToolPlugin#openParametersDialog(org.eclipse.swt.widgets.Shell)
   */
  public boolean openParametersDialog(Shell parent)
  {
    // Select the line color
    ColorDialog colorDialog = new ColorDialog(parent, SWT.APPLICATION_MODAL);
    colorDialog.setRGB(color.getRGB());
    RGB newColor = colorDialog.open();
    if (newColor != null)
    {
      if (color != null)
        color.dispose();
      color = new Color(null, newColor);
    }
    
    return (newColor != null);
  }
}
