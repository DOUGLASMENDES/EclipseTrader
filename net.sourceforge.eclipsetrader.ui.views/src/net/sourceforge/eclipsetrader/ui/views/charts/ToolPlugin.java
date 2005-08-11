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

  /**
   * Paints this tool's representation.
   * 
   * @param gc - the graphic context used to paint
   */
  public void paintTool(GC gc)
  {
  }
}
