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
package net.sourceforge.eclipsetrader.directa.internal;

import net.sourceforge.eclipsetrader.IBasicData;

/**
 * Interface for implementing a receiver for the Streamer class events.
 */
public interface IStreamerEventReceiver
{

  /**
   * Signal a generic data update.
   */
  public void dataUpdated();
  
  /**
   * Signal a data update for the specific item.
   */
  public void dataUpdated(IBasicData data);
  
  /**
   * Signal a change in the order status list.
   */
  public void orderStatusChanged();
}
