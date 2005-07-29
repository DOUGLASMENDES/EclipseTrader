/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Instances of this class represents an array list that issues notifications
 * when items are added, removed or updated.
 */
public class ObservableCollection extends ArrayList
{
  private static final long serialVersionUID = 82368136474584598L;
  private List observers = new ArrayList();

  public ObservableCollection()
  {
    super();
  }

  public ObservableCollection(int arg0)
  {
    super(arg0);
  }

  public ObservableCollection(Collection arg0)
  {
    super(arg0);
  }
  
  public void addObserver(ICollectionObserver observer)
  {
    if (!observers.contains(observer)) 
      observers.add(observer);
  }
  
  public void removeObserver(ICollectionObserver observer)
  {
    observers.remove(observer);
  }
  
  /* (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  public boolean add(Object obj)
  {
    boolean value = super.add(obj);
    if (value)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemAdded(obj);
    }
    return value;
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#add(int, java.lang.Object)
   */
  public void add(int index, Object obj)
  {
    super.add(index, obj);
    for (Iterator iter = observers.iterator(); iter.hasNext(); )
      ((ICollectionObserver)iter.next()).itemAdded(obj);
  }

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#remove(java.lang.Object)
   */
  public boolean remove(Object arg0)
  {
    boolean value = super.remove(arg0);
    if (value && arg0 != null)
    {
      for (Iterator iter = observers.iterator(); iter.hasNext(); )
        ((ICollectionObserver)iter.next()).itemRemoved(arg0);
    }
    return value;
  }
}
