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
 * Implementors of this interface are capable of collecting the realtime data
 * for the realtime chart view.
 * <p></p>
 * 
 * @author Marco Maccaferri - 21/08/2004
 */
public interface IRealtimeChartProvider
{

  /**
   * Backfills the realtime chart data.
   */
  public void backfill(IBasicData data);
  
  /**
   * Get the realtime chart data.
   */
  public IChartData[] getHistoryData(IBasicData data);

  public void addRealtimeChartListener(IBasicData data, IRealtimeChartListener listener);
  public void removeRealtimeChartListener(IBasicData data, IRealtimeChartListener listener);
}
