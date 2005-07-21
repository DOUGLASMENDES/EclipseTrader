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
package net.sourceforge.eclipsetrader;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represents objects that issue notifications when
 * they are updated.
 */
public class ObservableObject
{
  private List observers = new ArrayList();
  
  public void addObserver(IObjectObserver observer)
  {
    if (!observers.contains(observer)) 
      observers.add(observer);
  }
  
  public void removeObserver(IObjectObserver observer)
  {
    observers.remove(observer);
  }

  public void fireItemUpdated()
  {
    Object[] obj = observers.toArray();
    for (int i = 0; i < obj.length; i++)
      ((IObjectObserver)obj[i]).objectUpdated();
  }
}
