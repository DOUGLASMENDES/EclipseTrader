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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.HashMap;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.graphics.GC;

/**
 * Interface for chart plotters.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public interface IChartPlotter
{
  
  /**
   * Return the plugin id of this plotter.
   * <p></p>
   */
  public String getId();

  /**
   * Set the chart's name.
   * <p></p>
   */
  public void setName(String name);
  
  /**
   * Get the chart's name.
   * <p></p>
   */
  public String getName();

  /**
   * Return the plotter's description.
   * <p>Usually the description is a combination of the plotter's name and one, or
   * more, of it's parameters, like "Medium Average (30)", and should uniquely identify
   * the drawing.</p>
   */
  public String getDescription();

  /**
   * Set the chart canvas that contains this drawing.
   * <p></p>
   */
  public void setCanvas(ChartCanvas canvas);
  
  /**
   * Get the chart canvas that contains this drawing.
   * <p></p>
   */
  public ChartCanvas getCanvas();

  /**
   * Set the data used by this plotter to calculate the drawing.
   * <p></p>
   */
  public void setData(IChartData[] data);
  
  /**
   * Paint the chart section.
   * <p></p>
   */
  public void paintChart(GC gc, int width, int height);
  
  /**
   * Paint the scale section.
   * <p></p>
   */
  public void paintScale(GC gc, int width, int height);

  /**
   * Set a parameter value.
   * <p></p>
   */
  public void setParameter(String name, String value);
  
  /**
   * Return the parameters map for the plotter.
   * <p></p>
   */
  public HashMap getParameters();
}
