/*******************************************************************************
 * Copyright (c) 2004-2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.HashMap;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Interface for chart plotters.
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
   * 
   * @param data - the data to use
   */
  public void setData(IChartData[] data);
  
  /**
   * Get the default drawing color.
   * 
   * @return Color object
   */
  public Color getColor();
  
  /**
   * Paint the chart section.
   * 
   * @param gc - the graphics context to draw on
   * @param width - the width the canvas
   * @param height - the height of the canvas
   */
  public void paintChart(GC gc, int width, int height);
  
  /**
   * Paint the scale section.
   * 
   * @param gc - the graphics context to draw on
   * @param width - the width the canvas
   * @param height - the height of the canvas
   */
  public void paintScale(GC gc, int width, int height);
  
  /**
   * Paint the chart grid.
   * 
   * @param gc - the graphics context to draw on
   * @param width - the width the canvas
   * @param height - the height of the canvas
   */
  public void paintGrid(GC gc, int width, int height);

  /**
   * Set a parameter value.
   * 
   * @param name - the name of the parameter
   * @param value - the value to set
   */
  public void setParameter(String name, String value);
  
  /**
   * Return the parameters map for the plotter.
   * 
   * @return the parameters map
   */
  public HashMap getParameters();

  /**
   * Return the value at the given vertical position.
   * 
   * @param y - vertical position
   * @param height - height of the containing canvas
   * @return the value at the given position
   */
  public double getValue(int y, int height);

  /**
   * Return a string with the value at the given vertical position
   * formatted accordingly with the plotter's range of values and
   * presentation parameters.
   * 
   * @param y - vertical position
   * @param height - height of the containing canvas
   * @return the formatted value
   */
  public String getFormattedValue(int y, int height);
}
