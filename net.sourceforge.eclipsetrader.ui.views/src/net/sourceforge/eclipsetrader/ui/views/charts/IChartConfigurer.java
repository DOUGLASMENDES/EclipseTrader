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

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for chart configuration dialogs.
 * <p>This interface is partially compatible with IWorkbenchPreferencePage to allow
 * future enhancements to the chart's configuration dialogs and may be deprecated 
 * in the future</p>
 */
public interface IChartConfigurer
{

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
   * Creates and returns the SWT control for the customized body of the chart 
   * configuration dialog under the given parent composite.
   * <p></p>
   * 
   * @param parent the parent composite
   * @return the new control
   */
  public Control createContents(Composite parent);

  /**
   * Set a parameter value.
   * <p></p>
   */
  public void setParameter(String name, String value);
  
  /**
   * Get a parameter value.
   * <p></p>
   */
  public String getParameter(String name);
  
  /**
   * Get a color parameter's RGB value.
   * <p></p>
   */
  public RGB getColorParameter(String name);
}
