/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan S. Stratigakos - Original qtstalker code
 *     Marco Maccaferri      - Java porting and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

/**
 * Instances of this class represent a line on a chart.
 */
public class PlotLine
{
  public static final int DOT = 0;
  public static final int DASH = 1;
  public static final int HISTOGRAM = 2;
  public static final int HISTOGRAM_BAR = 3;
  public static final int LINE = 4;
  public static final int INVISIBLE = 5;
  public static final int HORIZONTAL = 6;
  private String label = "";
  private int type = LINE;
  private int lineWidth = 1;
  private List list = new ArrayList();
  private List colorList = new ArrayList();
  private Color color = new Color(null, 0, 0, 255);
  private boolean scaleFlag = false;
  private boolean selected = false;
  private double high = -99999999;
  private double low = 99999999;

  /**
   * Disposes the operating system resources associated with this object.
   */
  public void dispose()
  {
    if (color != null)
      color.dispose();

    for (int i = 0; i < colorList.size(); i++)
      ((Color)colorList.get(i)).dispose();
    colorList.clear();

    list.clear();
  }

  /**
   * Return the descriptive label of this line.
   * 
   * @return the line label
   */
  public String getLabel()
  {
    return this.label;
  }
  
  /**
   * Set the descriptive label for this line.
   * 
   * @param label - the line label
   */
  public void setLabel(String label)
  {
    this.label = label;
  }
  
  /**
   * Get the color used to draw this line.
   * 
   * @return the color to use
   */
  public Color getColor()
  {
    return this.color;
  }
  
  /**
   * Set the color used to draw this line.
   * 
   * @param color - the color
   */
  public void setColor(Color color)
  {
    if (this.color != null)
      this.color.dispose();

    if (color != null)
      this.color = new Color(null, color.getRGB());
    else
      this.color = null;
  }

  /**
   * Get the color used to draw the line value at the specificed position.
   * 
   * @param index - the value position
   * @return - the color to use
   */
  public Color getColor(int index)
  {
    if (index < 0 || index >= colorList.size())
      return null;
    return (Color)colorList.get(index);
  }
  
  public int getType()
  {
    return this.type;
  }
  
  public void setType(int lineStyle)
  {
    this.type = lineStyle;
  }
  
  public int getLineWidth()
  {
    return this.lineWidth;
  }
  
  public void setLineWidth(int lineWidth)
  {
    this.lineWidth = lineWidth;
  }

  /**
   * Add a value to the line.
   * 
   * @param value - the value to add
   */
  public void add(Double value)
  {
    list.add(value);
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value and it's color to the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void add(Double value, Color color)
  {
    list.add(value);
    colorList.add(new Color(null, color.getRGB()));
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value to the line.
   * 
   * @param value - the value to add
   */
  public void add(double value)
  {
    list.add(new Double(value));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }

  /**
   * Add a value and it's color to the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void add(double value, Color color)
  {
    list.add(new Double(value));
    colorList.add(new Color(null, color.getRGB()));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }

  /**
   * Add a value to the line.
   * 
   * @param value - the value to add
   */
  public void append(Double value)
  {
    list.add(value);
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value and it's color to the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void append(Double value, Color color)
  {
    list.add(value);
    colorList.add(new Color(null, color.getRGB()));
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value to the line.
   * 
   * @param value - the value to add
   */
  public void append(double value)
  {
    list.add(new Double(value));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }
  
  /**
   * Add a value and it's color to the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void append(double value, Color color)
  {
    list.add(new Double(value));
    colorList.add(new Color(null, color.getRGB()));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }
  
  public void appendColorBar(Color color)
  {
    colorList.add(new Color(null, color.getRGB()));
  }

  /**
   * Add a value to the beginning of the line.
   * 
   * @param value - the value to add
   */
  public void prepend(Double value)
  {
    list.add(0, value);
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value and it's color to the beginning of the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void prepend(Double value, Color color)
  {
    list.add(0, value);
    colorList.add(0, new Color(null, color.getRGB()));
    if (value.doubleValue() > high)
      high = value.doubleValue();
    if (value.doubleValue() < low)
      low = value.doubleValue();
  }

  /**
   * Add a value to the beginning of the line.
   * 
   * @param value - the value to add
   */
  public void prepend(double value)
  {
    list.add(0, new Double(value));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }
  
  /**
   * Add a value and it's color to the beginning of the line.
   * 
   * @param value - the value to add
   * @param color - the color
   */
  public void prepend(double value, Color color)
  {
    list.add(0, new Double(value));
    colorList.add(0, new Color(null, color.getRGB()));
    if (value > high)
      high = value;
    if (value < low)
      low = value;
  }
  
  public void prependColorBar(Color color)
  {
    colorList.add(0, new Color(null, color.getRGB()));
  }
  
  public Double getDouble(int index)
  {
    return (Double)list.get(index);
  }

  public double getDoubleValue(int index)
  {
    return ((Double)list.get(index)).doubleValue();
  }

  /**
   * Get the number of values in the list.
   * 
   * @return the number of values
   */
  public int getSize()
  {
    return list.size();
  }
  
  /**
   * Get the line value at the specified position.
   * 
   * @param index - the position of the value
   * @return the value
   */
  public double getData(int index)
  {
    return ((Double)list.get(index)).doubleValue();
  }
  
  public boolean getScaleFlag()
  {
    return this.scaleFlag;
  }
  
  public void setScaleFlag(boolean scaleFlag)
  {
    this.scaleFlag = scaleFlag;
  }
  
  public double getHigh()
  {
    return this.high;
  }
  
  public void setHigh(double high)
  {
    this.high = high;
  }
  
  public double getLow()
  {
    return this.low;
  }
  
  public void setLow(double low)
  {
    this.low = low;
  }

  public boolean isSelected()
  {
    return selected;
  }

  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
}
