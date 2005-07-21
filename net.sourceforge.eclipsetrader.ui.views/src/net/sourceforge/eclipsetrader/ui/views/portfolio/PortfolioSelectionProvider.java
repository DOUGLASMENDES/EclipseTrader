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
package net.sourceforge.eclipsetrader.ui.views.portfolio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class PortfolioSelectionProvider implements ISelectionProvider
{
  private List selectionListeners = new ArrayList();
  private PortfolioSelection selection;

  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    if (selectionListeners.contains(listener) == false)
      selectionListeners.add(listener);
  }

  public ISelection getSelection()
  {
    return selection;
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    selectionListeners.remove(listener);
  }

  public void setSelection(ISelection selection)
  {
    if (selection instanceof PortfolioSelection)
    {
      this.selection = (PortfolioSelection)selection;
      for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); )
        ((ISelectionChangedListener)iter.next()).selectionChanged(new SelectionChangedEvent(this, selection));
    }
  }

}
