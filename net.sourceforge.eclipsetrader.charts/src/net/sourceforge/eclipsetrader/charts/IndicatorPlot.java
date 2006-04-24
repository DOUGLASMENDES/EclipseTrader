/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.charts;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.charts.events.PlotEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotListener;
import net.sourceforge.eclipsetrader.charts.internal.PixelTools;
import net.sourceforge.eclipsetrader.core.db.Bar;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 */
public class IndicatorPlot extends Canvas implements ControlListener, DisposeListener, PaintListener
{
    private Image image;
    private BarData barData = new BarData();
    private int gridWidth = 5;
    private int marginWidth = 2;
    private Point plotLocation = new Point(0, 0);
    private List plotListeners = new ArrayList();
    private Scaler scaler;
    private NumberFormat nf = NumberFormat.getInstance();
    private Color grid = new Color(null, 224, 224, 224);
    private Color selectionMarks = new Color(null, 0, 0, 255);
    private List indicators = new ArrayList();
    private List objects = new ArrayList();
    private Indicator selection;
    private ObjectPlugin objectSelection;

    public IndicatorPlot(Composite parent, int style)
    {
        super(parent, style);
        
        addControlListener(this);
        addDisposeListener(this);
        addPaintListener(this);
    }

    public BarData getBarData()
    {
        return barData;
    }

    public void setBarData(BarData barData)
    {
        if (barData == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        this.barData = barData;
    }
    
    public void addIndicator(Indicator indicator)
    {
        indicators.add(indicator);
    }
    
    public void removeIndicator(Indicator indicator)
    {
        indicators.remove(indicator);
        if (indicator == selection)
            selection = null;
    }
    
    public void clearIndicators()
    {
        indicators.clear();
        selection = null;
    }
    
    public List getIndicators()
    {
        return Collections.unmodifiableList(indicators);
    }

    public void addObject(ObjectPlugin object)
    {
        objects.add(object);
    }

    public void removeObject(ObjectPlugin object)
    {
        objects.remove(object);
        if (object == objectSelection)
            objectSelection = null;
    }
    
    public List getObjects()
    {
        return objects;
    }

    public Point getPlotLocation()
    {
        return new Point(plotLocation.x, plotLocation.y);
    }

    public void setPlotLocation(Point location)
    {
        if (location == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        setPlotBounds(location.x, location.y, 0, 0, true, false);
    }

    public void setPlotLocation(int x, int y)
    {
        setPlotBounds(x, y, 0, 0, true, false);
    }

    public Point getPlotSize()
    {
        Rectangle rect = new Rectangle(0, 0, 0, getSize().y);
        if (image != null && !image.isDisposed())
            rect = image.getBounds();
        return new Point(rect.width, rect.height);
    }
    
    public void setPlotSize(int width, int height)
    {
        setPlotBounds(0, 0, width, height, false, true);
    }

    public Rectangle getPlotBounds()
    {
        Rectangle rect = new Rectangle(0, 0, 0, getSize().y);
        if (image != null && !image.isDisposed())
            rect = image.getBounds();
        rect.x = plotLocation.x;
        rect.y = plotLocation.y;
        return rect;
    }

    public void setPlotBounds(int x, int y, int width, int height)
    {
        setPlotBounds(x, y, width, height, true, true);
    }

    public void setPlotBounds(Rectangle rectangle)
    {
        setPlotBounds(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true);
    }
    
    void setPlotBounds(int x, int y, int width, int height, boolean move, boolean resize)
    {
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        if (image != null && !image.isDisposed())
            rect = image.getBounds();
        rect.x = plotLocation.x;
        rect.y = plotLocation.y;
        
        height = getSize().y;
//        if (width == 0 || height == 0)
//            resize = false;
        
        if (resize && (rect.width != width || rect.height != height))
        {
            if (image != null && !image.isDisposed())
                image.dispose();
            if (width != 0 && height != 0)
            {
                image = new Image(getDisplay(), width, height);
                if (scaler != null)
                    scaler.set(getSize().y);
                
                GC gc = new GC(image);
                draw(gc);
                gc.dispose();
            }
            
            Event event = new Event();
            event.type = SWT.Resize;
            event.display = getDisplay();
            event.widget = this;
            for (Iterator iter = plotListeners.iterator(); iter.hasNext(); )
                ((PlotListener)iter.next()).plotResized(new PlotEvent(event));
            
            redraw();
        }
        if (move && (rect.x != x || rect.y != y))
        {
            this.plotLocation = new Point(x, y);
            redraw();
        }
    }

    public void addPlotListener(PlotListener listener)
    {
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        plotListeners.add(listener);
    }

    public void removePlotListener(PlotListener listener)
    {
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        plotListeners.remove(listener);
    }

    public int getGridWidth()
    {
        return gridWidth;
    }

    public void setGridWidth(int columnWidth)
    {
        this.gridWidth = columnWidth;
    }

    public int getMarginWidth()
    {
        return marginWidth;
    }

    public void setMarginWidth(int marginWidth)
    {
        this.marginWidth = marginWidth;
    }
    
    public void setScaler(Scaler scaler)
    {
        this.scaler = scaler;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
     */
    public void controlMoved(ControlEvent e)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
     */
    public void controlResized(ControlEvent e)
    {
        if (image != null && !image.isDisposed())
        {
            Rectangle rect = image.getBounds();
            if (getSize().y != rect.height && getSize().y != 0)
            {
                image.dispose();
                image = new Image(getDisplay(), rect.width, getSize().y);
                
                if (scaler != null)
                    scaler.set(getSize().y);

                GC gc = new GC(image);
                draw(gc);
                gc.dispose();
            }
        }
    }
    
    public void deselectAll()
    {
        this.selection = null;
        this.objectSelection = null;
    }
    
    public Indicator getSelection()
    {
        return selection;
    }
    
    public void setSelection(Indicator selection)
    {
        this.selection = selection;
        this.objectSelection = null;
    }
    
    public Indicator getIndicatorAt(Point point)
    {
        if (scaler != null && barData.size() != 0)
        {
            for (int ii = indicators.size() - 1; ii >= 0; ii--)
            {
                Indicator indicator = (Indicator)indicators.get(ii);

                Scaler indicatorScaler = new Scaler();
                indicatorScaler.set(scaler.getHeight(), indicator.getHigh(), indicator.getLow(), scaler.getLogScaleHigh(), scaler.getLogRange(), scaler.getLogFlag());
                
                for (int ll = indicator.getLines().size() - 1; ll >= 0; ll--)
                {
                    PlotLine plotLine = (PlotLine)indicator.getLines().get(ll);
                    switch(plotLine.getType())
                    {
                        case PlotLine.DOT:
                        case PlotLine.DASH:
                        case PlotLine.LINE:
                        {
                            if (plotLine.getScaleFlag())
                            {
                                indicatorScaler = new Scaler();
                                indicatorScaler.set(scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), scaler.getLogScaleHigh(), scaler.getLogRange(), scaler.getLogFlag());
                            }
                            int ofs = barData.size() - plotLine.getSize();
                            int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
                            int[] pointArray = new int[plotLine.getSize() * 2];
                            for (int i = 0, pa = 0; i < plotLine.getSize(); i++, x += getGridWidth())
                            {
                                pointArray[pa++] = x;
                                pointArray[pa++] = indicatorScaler.convertToY(plotLine.getData(i));
                            }
                            if (PixelTools.isPointOnLine(point.x, point.y, pointArray))
                                return indicator;
                            break;
                        }
                        case PlotLine.HORIZONTAL:
                            break;
                        case PlotLine.HISTOGRAM:
                            break;
                        case PlotLine.HISTOGRAM_BAR:
                            break;
                        case PlotLine.BAR:
                        case PlotLine.CANDLE:
                        {
                            if (plotLine.getScaleFlag())
                            {
                                indicatorScaler = new Scaler();
                                indicatorScaler.set(scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), scaler.getLogScaleHigh(), scaler.getLogRange(), scaler.getLogFlag());
                            }
                            int ofs = barData.size() - plotLine.getSize();
                            int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
                            for (int i = 0; i < plotLine.getSize(); i++, x += getGridWidth())
                            {
                                int y1 = indicatorScaler.convertToY(plotLine.getBar(i).getHigh());
                                int y2 = indicatorScaler.convertToY(plotLine.getBar(i).getLow());
                                if (point.y >= y1 && point.y <= y2 && point.x >= (x - 2) && point.x <= (x + 2))
                                    return indicator;
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public ObjectPlugin getObjectSelection()
    {
        return objectSelection;
    }
    
    public void setObjectSelection(ObjectPlugin selection)
    {
        this.selection = null;
        this.objectSelection = selection;
    }
    
    public ObjectPlugin getObjectAt(int x, int y)
    {
        for (int i = objects.size() - 1; i >= 0; i--)
        {
            ObjectPlugin object = (ObjectPlugin)objects.get(i);
            if (object.isOverLine(x, y))
                return object;
        }
        
        return null;
    }
    
    public void redrawAll()
    {
/*        if (barData.size() != 0 && getSize().y != 0)
        {
            int offset = 0;
            int width = getMarginWidth() + barData.size() * getGridWidth() + getMarginWidth();
            if (image == null || image.isDisposed() || image.getBounds().width != width || image.getBounds().height != getSize().y)
            {
                if (image != null && !image.isDisposed())
                {
                    offset = width - image.getBounds().width;
                    image.dispose();
                }
                image = new Image(getDisplay(), width, getSize().y);
                plotLocation.x -= offset;
                if (getHorizontalBar().isVisible())
                {
                    getHorizontalBar().setMaximum(width);
                    getHorizontalBar().setSelection(- plotLocation.x);
                }
            }
        }*/
        if (image != null && !image.isDisposed())
        {
            GC gc = new GC(image);
            draw(gc);
            gc.dispose();
        }
        redraw();
    }
    
    public void draw(GC gc)
    {
        Rectangle bounds = image.getBounds(); 
        Color background = getBackground();
        Color foreground = getForeground();
        
        if (background != null)
            gc.setBackground(background);
        if (foreground != null)
            gc.setForeground(foreground);

        gc.fillRectangle(bounds);

        if (scaler != null && barData.size() != 0)
        {
            drawGrid(gc, bounds);
            
            for (Iterator iter = indicators.iterator(); iter.hasNext(); )
            {
                Indicator indicator = (Indicator)iter.next();
                
                Scaler indicatorScaler = new Scaler();
                indicatorScaler.set(scaler.getHeight(), indicator.getHigh(), indicator.getLow(), scaler.getLogScaleHigh(), scaler.getLogRange(), scaler.getLogFlag());

                for (Iterator iter2 = indicator.iterator(); iter2.hasNext(); )
                {
                    PlotLine plotLine = (PlotLine)iter2.next();
                    switch(plotLine.getType())
                    {
                        case PlotLine.DOT:
                        case PlotLine.DASH:
                        case PlotLine.LINE:
                            drawLine(gc, plotLine, indicatorScaler, selection == indicator);
                            break;
                        case PlotLine.HORIZONTAL:
                            drawHorizontalLine(gc, plotLine, indicatorScaler);
                            break;
                        case PlotLine.HISTOGRAM:
                            drawHistogram(gc, plotLine, indicatorScaler);
                            break;
                        case PlotLine.HISTOGRAM_BAR:
                            drawHistogramBars(gc, plotLine, indicatorScaler);
                            break;
                        case PlotLine.BAR:
                            drawBars(gc, plotLine, indicatorScaler, selection == indicator);
                            break;
                        case PlotLine.CANDLE:
                            drawCandles(gc, plotLine, indicatorScaler, selection == indicator);
                            break;
                    }
                }
            }
        }

        if (background != null)
            background.dispose();
        if (foreground != null)
            foreground.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e)
    {
        if (image != null && !image.isDisposed())
        {
            if (image.getBounds().width < getBounds().width)
                drawGrid(e.gc, getBounds());
/*            int srcX = e.x - plotLocation.x;
            int width = e.width;
            if (width > (image.getBounds().width - srcX))
                width = image.getBounds().width - srcX;
            if (srcX >= 0 && width > 0)
                e.gc.drawImage(image, srcX, e.y, width, e.height, e.x, e.y, width, e.height);*/

            e.gc.drawImage(image, plotLocation.x, plotLocation.y);

            if (scaler != null && barData.size() != 0)
            {
                for (Iterator iter = objects.iterator(); iter.hasNext(); )
                {
                    ObjectPlugin object = (ObjectPlugin)iter.next();
                    object.drawObject(e.gc, objectSelection == object);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
     */
    public void widgetDisposed(DisposeEvent e)
    {
        for (Iterator iter = indicators.iterator(); iter.hasNext(); )
            ((Indicator)iter.next()).dispose();
        for (Iterator iter = objects.iterator(); iter.hasNext(); )
            ((ObjectPlugin)iter.next()).dispose();
        if (image != null && !image.isDisposed())
            image.dispose();
    }
    
    private void drawGrid(GC gc, Rectangle bounds)
    {
        if (scaler == null)
            return;
        List scaleArray = scaler.getScaleArray();

        if (grid != null)
            gc.setForeground(grid);
        int dashes[] = { 3, 3 };
        gc.setLineDash(dashes);

        int loop;
        for (loop = 0; loop < scaleArray.size(); loop++)
        {
            int y = scaler.convertToY(((Double) scaleArray.get(loop)).doubleValue());
            String s = nf.format((Double) scaleArray.get(loop));
            int h = gc.stringExtent(s).y / 2;

            if ((y + h) <= bounds.height)
                gc.drawLine(0, y, bounds.width, y);
        }

        gc.setLineStyle(SWT.LINE_SOLID);
    }
    
    private void drawLine(GC gc, PlotLine plotLine, Scaler scaler, boolean selected)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        int ofs = barData.size() - plotLine.getSize();
        int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
        int[] pointArray = new int[plotLine.getSize() * 2];
        for (int i = 0, pa = 0; i < plotLine.getSize(); i++, x += getGridWidth())
        {
            pointArray[pa++] = x;
            pointArray[pa++] = scaler.convertToY(plotLine.getData(i));
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        if (plotLine.getType() == PlotLine.DOT)
        {
            int[] dashes = { 1, 2 };
            gc.setLineDash(dashes);
        }
        else if (plotLine.getType() == PlotLine.DASH)
        {
            int[] dashes = { 3, 3 };
            gc.setLineDash(dashes);
        }
        gc.setForeground(plotLine.getColor());
        gc.drawPolyline(pointArray);

        // Draw the selection marks
        if (selected && pointArray.length > 0)
        {
            gc.setBackground(plotLine.getColor());
            int length = pointArray.length / 2;
            if (length <= 20)
            {
                gc.fillRectangle(pointArray[0] - 2, pointArray[1] - 2, 5, 5);
                gc.fillRectangle(pointArray[(length / 2) * 2] - 2, pointArray[(length / 2) * 2 + 1] - 2, 5, 5);
            }
            else
            {
                for (int i = 0; i < length - 5; i += 10)
                    gc.fillRectangle(pointArray[i * 2] - 2, pointArray[i * 2 + 1] - 2, 5, 5);
            }
            gc.fillRectangle(pointArray[pointArray.length - 2] - 2, pointArray[pointArray.length - 2] - 1, 5, 5);
        }
    }
    
    private void drawHorizontalLine(GC gc, PlotLine plotLine, Scaler scaler)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(plotLine.getColor());

        int x1 = getMarginWidth() + getGridWidth() / 2;
        int x2 = x1 + getGridWidth() * barData.size();
        for (int i = 0; i < plotLine.getSize(); i++)
        {
            int y = scaler.convertToY(plotLine.getDouble(i).doubleValue());
            if (plotLine.getColor(i) != null)
                gc.setForeground(plotLine.getColor(i));
            gc.drawLine(x1, y, x2, y);
        }
    }
    
    private void drawCandles(GC gc, PlotLine plotLine, Scaler scaler, boolean selected)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(plotLine.getColor());

        int ofs = barData.size() - plotLine.getSize();
        int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
        for (int i = 0; i < plotLine.getSize(); i++, x += getGridWidth())
        {
            Bar bar = plotLine.getBar(i);
            int h = scaler.convertToY(bar.getHigh());
            int l = scaler.convertToY(bar.getLow());
            int c = scaler.convertToY(bar.getClose());
            int o = scaler.convertToY(bar.getOpen());

            if (plotLine.getColor(i) != null)
                gc.setBackground(plotLine.getColor(i));

            gc.drawLine(x, h, x, c);
            gc.drawLine(x, o, x, l);
            if (o == c)
                gc.drawLine(x - 2, c, x + 2, c);
            else
            {
                gc.fillRectangle(x - 2, c, 5, o - c);
                gc.drawRectangle(x - 2, c, 5, o - c);
            }
        }

        // Draw the selection marks
        if (selected)
        {
            x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
            int[] pointArray = new int[plotLine.getSize() * 2];
            for (int i = 0, pa = 0; i < plotLine.getSize(); i++, x += getGridWidth())
            {
                pointArray[pa++] = x;
                pointArray[pa++] = scaler.convertToY(plotLine.getBar(i).getLow() + (plotLine.getBar(i).getHigh() - plotLine.getBar(i).getLow()) / 2);
            }

            if (pointArray.length > 0)
            {
                gc.setBackground(selectionMarks);
                int length = pointArray.length / 2;
                if (length <= 20)
                {
                    gc.fillRectangle(pointArray[0] - 1, pointArray[1] - 1, 5, 5);
                    gc.fillRectangle(pointArray[(length / 2) * 2] - 1, pointArray[(length / 2) * 2 + 1] - 1, 5, 5);
                }
                else
                {
                    for (int i = 0; i < length - 5; i += 10)
                        gc.fillRectangle(pointArray[i * 2] - 1, pointArray[i * 2 + 1] - 1, 5, 5);
                }
                gc.fillRectangle(pointArray[pointArray.length - 2] - 1, pointArray[pointArray.length - 1] - 1, 5, 5);
            }
        }
    }
    
    private void drawBars(GC gc, PlotLine plotLine, Scaler scaler, boolean selected)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(plotLine.getColor());
        
        int ofs = barData.size() - plotLine.getSize();
        int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
        for (int i = 0; i < plotLine.getSize(); i++, x += getGridWidth())
        {
            if (plotLine.getColor(i) != null)
                gc.setForeground(plotLine.getColor(i));

            int y = scaler.convertToY(plotLine.getBar(i).getOpen());
            gc.drawLine(x - 2, y, x, y);
            y = scaler.convertToY(plotLine.getBar(i).getClose());
            gc.drawLine(x, y, x + 2, y);

            int y1 = scaler.convertToY(plotLine.getBar(i).getHigh());
            int y2 = scaler.convertToY(plotLine.getBar(i).getLow());
            gc.drawLine(x, y1, x, y2);
        }

        // Draw the selection marks
        if (selected)
        {
            x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
            int[] pointArray = new int[plotLine.getSize() * 2];
            for (int i = 0, pa = 0; i < plotLine.getSize(); i++, x += getGridWidth())
            {
                pointArray[pa++] = x;
                pointArray[pa++] = scaler.convertToY(plotLine.getBar(i).getLow() + (plotLine.getBar(i).getHigh() - plotLine.getBar(i).getLow()) / 2);
            }

            if (pointArray.length > 0)
            {
                gc.setBackground(selectionMarks);
                int length = pointArray.length / 2;
                if (length <= 20)
                {
                    gc.fillRectangle(pointArray[0] - 1, pointArray[1] - 1, 5, 5);
                    gc.fillRectangle(pointArray[(length / 2) * 2] - 1, pointArray[(length / 2) * 2 + 1] - 1, 5, 5);
                }
                else
                {
                    for (int i = 0; i < length - 5; i += 10)
                        gc.fillRectangle(pointArray[i * 2] - 1, pointArray[i * 2 + 1] - 1, 5, 5);
                }
                gc.fillRectangle(pointArray[pointArray.length - 2] - 1, pointArray[pointArray.length - 1] - 1, 5, 5);
            }
        }
    }

    private void drawHistogram(GC gc, PlotLine plotLine, Scaler scaler)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setBackground(plotLine.getColor());

        int zero = scaler.convertToY(0);
        int ofs = barData.size() - plotLine.getSize();
        int x = -1;
        int x2 = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
        int y = -1;
        int y2 = -1;
        int[] pointArray = new int[8];
        for (int i = 0; i < plotLine.getSize(); i++, x2 += getGridWidth())
        {
            y2 = scaler.convertToY(plotLine.getData(i));

            pointArray[0] = x;
            pointArray[1] = zero;
            pointArray[2] = x;
            pointArray[3] = y;
            pointArray[4] = x2;
            pointArray[5] = y2;
            pointArray[6] = x2;
            pointArray[7] = zero;
            if (y != -1)
                gc.fillPolygon(pointArray);

            x = x2;
            y = y2;
        }
    }

    private void drawHistogramBars(GC gc, PlotLine plotLine, Scaler scaler)
    {
        if (plotLine.getScaleFlag())
        {
            scaler = new Scaler();
            scaler.set(this.scaler.getHeight(), plotLine.getHigh(), plotLine.getLow(), this.scaler.getLogScaleHigh(), this.scaler.getLogRange(), this.scaler.getLogFlag());
        }

        gc.setLineStyle(SWT.LINE_SOLID);

        int zero = scaler.convertToY(0);
        int ofs = barData.size() - plotLine.getSize();
        int x = getMarginWidth() + getGridWidth() / 2 + ofs * getGridWidth();
        for (int i = 0; i < plotLine.getSize(); i++, x += getGridWidth())
        {
            int y = scaler.convertToY(plotLine.getData(i));

            Color color = plotLine.getColor(i);
            if (color == null)
                color = plotLine.getColor();
            gc.setBackground(color);

            gc.fillRectangle(x - 1, y, 3, zero - y);
        }
    }
}
