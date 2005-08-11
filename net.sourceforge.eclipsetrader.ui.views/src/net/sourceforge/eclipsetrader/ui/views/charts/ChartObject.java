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
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;


/**
 */
public abstract class ChartObject implements Observer
{
  private Canvas canvas;
  private Scaler scaler;
  private boolean pressed = false;
  private boolean selected = false;
  protected HashMap parameters = new HashMap();

  public Canvas getCanvas()
  {
    return this.canvas;
  }
  
  public void setCanvas(Canvas canvas)
  {
    this.canvas = canvas;
  }
  
  public Scaler getScaler()
  {
    return this.scaler;
  }
  
  public void setScaler(Scaler scaler)
  {
    if (this.scaler != null)
      this.scaler.deleteObserver(this);
    this.scaler = scaler;
    if (this.scaler != null)
      this.scaler.addObserver(this);
  }

  public boolean containsPoint(int x, int y)
  {
    return false;
  }

  public boolean isOnHandle(int x, int y)
  {
    return false;
  }
  
  public void setParameter(String name, String value)
  {
    parameters.put(name, value);
  }
  
  public String getParameter(String name)
  {
    return (String)parameters.get(name);
  }
  
  public Map getParameters()
  {
    return parameters;
  }
  
  /**
   * Open the tool's parameters dialog.
   * 
   * @param parent - parent shell instance
   * @return true if the user accepts the parameters, false otherwise
   */
  public boolean openParametersDialog(Shell parent)
  {
    return false;
  }

  /**
   * Return the state of the mouse button.
   * 
   * @return true if the mouse button is pressed, false otherwise
   */
  public boolean isMousePressed()
  {
    return pressed;
  }

  public void mousePressed(MouseEvent me)
  {
    pressed = true;
  }

  public void mouseMoved(MouseEvent me)
  {
  }

  public void mouseDragged(MouseEvent me)
  {
  }

  public void mouseReleased(MouseEvent me)
  {
    pressed = false;
  }
  
  /**
   * Returns a rectangle describing the receiver's size and location.
   * 
   * @return the receiver's bounding rectangle
   */
  public Rectangle getBounds()
  {
    if (canvas != null)
      return canvas.getBounds();
    else
      return new Rectangle(0, 0, 0, 0);
  }

  public void scalerUpdate()
  {
  }

  public boolean isSelected()
  {
    return selected;
  }

  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    if (o == scaler)
      scalerUpdate();
  }
}
