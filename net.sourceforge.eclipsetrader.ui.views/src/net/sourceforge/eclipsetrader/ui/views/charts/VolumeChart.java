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
package net.sourceforge.eclipsetrader.ui.views.charts;

import net.sourceforge.eclipsetrader.IChartData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VolumeChart extends ChartPainter
{
  private static int VERTICAL_BORDER = 3;
  
  public void setData(IChartData[] data)
  {
    this.data = data;
    
    if (data != null)
    {
      // Determina massimo e minimo
      min = max = 0;
      for (int i = 0; i < data.length; i++)
      {
        if (data[i].getVolume() > max)
          max = data[i].getVolume();
      }
    }
  }
  
  public void paintChart(GC gc, int width, int height)
  {
    // Tipo di line e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
    
    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (height - VERTICAL_BORDER * 2) / (max - min);

      int x = chartMargin + columnWidth / 2;
      for (int i = 0; i < data.length; i++, x += columnWidth)
      {
        int y1 = height - (int)((data[i].getVolume() - min) * pixelRatio);
        int y2 = height;
        gc.drawLine(x, y1 - VERTICAL_BORDER, x, y2 - VERTICAL_BORDER);
      }
    }

    // Tipo di line e colore
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setForeground(lineColor);
    
    // Titolo del grafico
    gc.drawString("Volume", 2, 0);
  }

  public void paintScale(GC gc, int width, int height)
  {
  }
}
