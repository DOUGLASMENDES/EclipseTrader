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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface used by News Provider plugins.
 * <p></p>
 */
public interface INewsProvider
{
  /**
   * Get the new data.
   * <p></p>
   */
  public INewsData[] getData();

  /**
   * Update the news data.
   * <p></p>
   * @param monitor IProgressMonitor object used to display a progress indicator to the user.
   */
  public void update(IProgressMonitor monitor);
}
