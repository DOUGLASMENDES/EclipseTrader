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
package net.sourceforge.eclipsetrader.archipelago;

import java.util.Vector;

import net.sourceforge.eclipsetrader.BookData;
import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IBookData;

/**
 */
public class PriceBook
{
  public IBasicData data;
  public IBookData[] bid = new BookData[0];
  public IBookData[] ask = new BookData[0];
  public Vector bidBook = new Vector();
  public Vector askBook = new Vector();
  public Vector listeners = new Vector();
  
  public PriceBook()
  {
  }
}
