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

import java.util.Enumeration;
import java.util.Vector;

import net.sourceforge.eclipsetrader.BookData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;
import net.sourceforge.eclipsetrader.IBookUpdateListener;

/**
 */
public class PriceBook
{
  public String ticker = "";
  public IBookData[] bid = new BookData[5];
  public IBookData[] ask = new BookData[5];
  public Vector listeners = new Vector();
  
  public PriceBook()
  {
    for (int i = 0; i < bid.length; i++)
      bid[i] = new BookData();
    for (int i = 0; i < ask.length; i++)
      ask[i] = new BookData();
  }
  
  public void fireBookUpdated(IBasicData data)
  {
    Enumeration e = listeners.elements();
    while(e.hasMoreElements() == true)
      ((IBookUpdateListener)e.nextElement()).bookUpdated(data, bid, ask);
  }
}
