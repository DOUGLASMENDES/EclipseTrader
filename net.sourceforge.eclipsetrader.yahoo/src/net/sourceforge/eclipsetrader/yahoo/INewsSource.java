/*
 * Created on 25-ott-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.eclipsetrader.yahoo;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface INewsSource
{
  public Vector update(IProgressMonitor monitor);
}