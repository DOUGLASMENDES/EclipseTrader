/*******************************************************************************
 * Copyright (c) 2005 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *     Stephen Bate     - Dukascopy plugin
 *******************************************************************************/
package net.sourceforge.eclipsetrader.dukascopy;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Steve
 */
public class Messages
{
  private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.dukascopy.DukascopyPluginResources";//$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  private Messages()
  {
  }

  public static String getString(String key)
  {
    try
    {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e)
    {
      return '!' + key + '!';
    }
  }
}