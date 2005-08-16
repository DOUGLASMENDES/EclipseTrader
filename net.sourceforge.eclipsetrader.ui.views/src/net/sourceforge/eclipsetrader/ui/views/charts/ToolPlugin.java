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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.Date;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;


/**
 */
public abstract class ToolPlugin extends ChartObject
{
  private IAction action;
  
  public void setAction(IAction action)
  {
    this.action = action;
    if (action != null)
    {
      action.setEnabled(false);
      action.setChecked(true);
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.ChartObject#mouseReleased(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseReleased(MouseEvent me)
  {
    super.mouseReleased(me);
    if (action != null)
    {
      action.setChecked(false);
      action.setEnabled(true);
      action = null;
    }
  }
  
  public int getX(Date date)
  {
    return getCanvas().getMargin() + getCanvas().getColumnWidth() / 2 + getData().getX(date) * getCanvas().getColumnWidth();
  }
  
  public Date getDate(int x)
  {
    int index = (x - (getCanvas().getMargin() + (getCanvas().getColumnWidth() / 2))) / getCanvas().getColumnWidth();
    if (index >= 0 && index < getData().size())
      return getData().get(index).getDate();
    return null;
  }
  
  public void invalidate()
  {
  }

  /**
   * Paints this tool's representation.
   * 
   * @param gc - the graphic context used to paint
   */
  public void paintTool(GC gc)
  {
  }
}
