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
package net.sourceforge.eclipsetrader;


/**
 * Interface for history chart data providers.
 * <p></p>
 * @since 1.0
 */
public interface IChartDataProvider
{

  /**
   * Read the chart data from the provider's storage.
   * <p></p>
   * @param data The IBasicData object of the chart.
   * @return Returns the IChartData array.
   */
  public abstract IChartData[] getData(IBasicData data);

  /**
   * Update the chart data by reading the new elements from the provider's storage.
   * <p></p>
   * @param data The IBasicData object of the chart.
   * @return Returns the updated IChartData array.
   */
  public abstract void update(IBasicData data);
}
