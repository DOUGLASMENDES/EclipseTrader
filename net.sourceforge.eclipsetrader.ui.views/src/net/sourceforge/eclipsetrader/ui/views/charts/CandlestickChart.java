/*
 * Created on 29-ago-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.eclipsetrader.ui.views.charts;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;

/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CandlestickChart
{

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    GC gc = e.gc;
    
/*    if (data != null && max > min && visible == true)
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = (canvas.getClientArea().height) / (max - min);

      // Tipo di line e colore
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(textColor);

      int x = 3;
      for (int i = 0; i < data.length; i++, x += 6)
      {
        int y1 = canvas.getClientArea().height - (int)((data[i].getMaxPrice() - min) * pixelRatio);
        int y2 = canvas.getClientArea().height - (int)((data[i].getMinPrice() - min) * pixelRatio);
        gc.drawLine(x, y1, x, y2);
        
        y1 = canvas.getClientArea().height - (int)((data[i].getOpenPrice() - min) * pixelRatio);
        y2 = canvas.getClientArea().height - (int)((data[i].getClosePrice() - min) * pixelRatio);
        if (y1 > y2)
        {
          gc.setBackground(background);
          gc.fillRectangle(x - 2, y2, 5, y1 - y2);
          if (y1 == y2)
            gc.drawRectangle(x - 2, y2, 4, y1 - y2);
          else
            gc.drawRectangle(x - 2, y2, 4, y1 - y2 - 1);
        }
        else
        {
          gc.setBackground(textColor);
          gc.fillRectangle(x - 2, y1, 5, y2 - y1 + 1);
        }
      }
    }*/
  }
}
