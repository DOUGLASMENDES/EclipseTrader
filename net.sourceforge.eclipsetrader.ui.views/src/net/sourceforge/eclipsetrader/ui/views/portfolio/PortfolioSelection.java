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

import org.eclipse.jface.viewers.ISelection;

/**
 * Instances of this class are used to enable or disable the menu and toolbar
 * items based on the selected stock watch item.
 */
public class PortfolioSelection implements ISelection
{
  public Object item;

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  public boolean isEmpty()
  {
    return (item == null);
  }

}
