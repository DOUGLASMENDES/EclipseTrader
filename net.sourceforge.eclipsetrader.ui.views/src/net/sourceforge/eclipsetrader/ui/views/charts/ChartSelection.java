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

import org.eclipse.jface.viewers.ISelection;

/**
 */
public class ChartSelection implements ISelection
{
  private ChartCanvas canvas;
  
  public ChartSelection()
  {
    this.canvas = null;
  }
  
  public ChartSelection(ChartCanvas canvas)
  {
    this.canvas = canvas;
  }

  public ChartCanvas getChartCanvas()
  {
    return canvas;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  public boolean isEmpty()
  {
    return (this.canvas == null);
  }
}
