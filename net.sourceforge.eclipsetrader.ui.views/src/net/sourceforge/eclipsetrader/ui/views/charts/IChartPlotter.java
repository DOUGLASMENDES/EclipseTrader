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


/**
 * @author Marco
 */
public interface IChartPlotter
{
  
  /**
   * Return the plugin id of this plotter.
   * <p></p>
   */
  public String getId();

  /**
   * Return the plotter's description.
   * <p>Usually the description is a combination of the plotter's name and one, or
   * more, of it's parameters, like "Medium Average (30)", and should uniquely identify
   * the drawing.</p>
   */
  public String getDescription();
  
  /**
   * Open the plotter's parameters dialog.
   * <p></p>
   */
  public void setParameters();

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
