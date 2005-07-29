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

/**
 * Interface for objects that listen to updates to the ObservableCollection
 * class.
 */
public interface ICollectionObserver
{

  /**
   * Invoked when an item is added to the observed collection.
   * 
   * @param obj - the added object
   */
  public void itemAdded(Object obj);

  /**
   * Invoked when an item is removed from the observed collection.
   * 
   * @param obj - the removed object
   */
  public void itemRemoved(Object obj);
}
