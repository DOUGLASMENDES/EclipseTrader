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
package net.sourceforge.eclipsetrader.ui.views.news;

import java.util.*;

/**
 * News item data
 */
public interface INewsItem 
{
  public boolean readed = false;
  public Date date = null;
  public String title = "";
  public String source = "";
  public String url = "";
}
