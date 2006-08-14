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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.eclipsetrader.charts.events.PlotMouseEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotMouseListener;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionEvent;
import net.sourceforge.eclipsetrader.charts.events.PlotSelectionListener;
import net.sourceforge.eclipsetrader.core.db.BarData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 */
public class Plot extends Composite implements MouseListener, MouseMoveListener
{
    private Summary summary;
    private DateSummaryItem dateSummary;
    private IndicatorPlot indicatorPlot;
    private ScalePlot scalePlot;
    private Color foreground = new Color(null, 0, 0, 0);
    private Color background = new Color(null, 255, 255, 224);
    private Color selectionColor = new Color(null, 224, 0, 0);
    private int scaleWidth = 75;
    private Scaler scaler = new Scaler();
    private DatePlot datePlot;
    private boolean buttonDown = false;
    private List plotMouseListeners = new ArrayList();
    private List selectionListeners = new ArrayList();
    private int drawCount = 0;
    private Cursor crossCursor = new Cursor(null, SWT.CURSOR_CROSS);
    private Cursor handCursor = new Cursor(null, SWT.CURSOR_HAND);
    private Cursor currentCursor;
    private boolean selection = false;

    public Plot(Composite parent, int style)
    {
        super(parent, style);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        setLayout(gridLayout);

        summary = new Summary(this, SWT.NONE);
        summary.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        dateSummary = new DateSummaryItem(summary, SWT.NONE);
        
        indicatorPlot = new IndicatorPlot(this, SWT.NONE);
        indicatorPlot.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        indicatorPlot.setForeground(foreground);
        indicatorPlot.setBackground(background);
        indicatorPlot.setScaler(scaler);
        indicatorPlot.addMouseListener(this);
        indicatorPlot.addMouseMoveListener(this);

        scalePlot = new ScalePlot(this, SWT.NONE);
        GridData gridData = new GridData();
        gridData.widthHint = scaleWidth;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        scalePlot.setLayoutData(gridData);
        scalePlot.setForeground(foreground);
        scalePlot.setBackground(background);
        scalePlot.setSeparatorColor(foreground);
        scalePlot.setScaler(scaler);

        pack();
    }

    public boolean getSelection()
    {
        return selection;
    }

    public void setSelection(boolean selection)
    {
        this.selection = selection;
        if (selection)
            scalePlot.setSeparatorColor(selectionColor);
        else
            scalePlot.setSeparatorColor(foreground);
    }

    public void addIndicator(Indicator indicator)
    {
        double high = scaler.getHigh();
        double low = scaler.getLow();

        for (Iterator iter = indicator.getLines().iterator(); iter.hasNext(); )
        {
            PlotLine line = (PlotLine)iter.next();
            if ((line.getLabel() != null || line.getType() == PlotLine.BAR || line.getType() == PlotLine.CANDLE) && line.getSize() != 0)
            {
                if (line.getType() == PlotLine.BAR || line.getType() == PlotLine.CANDLE)
                {
                    BarSummaryItem item = new BarSummaryItem(summary, SWT.NONE);
                    item.setForeground(line.getColor());
                    line.setData(item);
                }
                else
                {
                    NumberSummaryItem item = new NumberSummaryItem(summary, SWT.NONE);
                    item.setText(line.getLabel().toUpperCase());
                    item.setForeground(line.getColor());
                    line.setData(item);
                }
            }
            if (line.getHigh() > high)
                high = line.getHigh();
            if (line.getLow() < low)
                low = line.getLow();
        }
        
        scaler.set(high, low);

        indicatorPlot.addIndicator(indicator);
        indicatorPlot.setBarData(datePlot.getBarData());
        if (drawCount <= 0)
        {
            indicatorPlot.redrawAll();
            scalePlot.redrawAll();
            updateSummary();
        }
    }

    public void removeIndicator(Indicator indicator)
    {
        Control[] childs = summary.getChildren();
        for (int i = 1; i < childs.length; i++)
            childs[i].dispose();

        indicatorPlot.removeIndicator(indicator);

        double high = -99999999;
        double low = 99999999;

        List list = new ArrayList();
        for (Iterator iter = indicatorPlot.getIndicators().iterator(); iter.hasNext(); )
            list.addAll(((Indicator)iter.next()).getLines());

        for (Iterator iter = list.iterator(); iter.hasNext(); )
        {
            PlotLine line = (PlotLine)iter.next();
            if (line.getType() == PlotLine.BAR || line.getType() == PlotLine.CANDLE)
            {
                BarSummaryItem item = new BarSummaryItem(summary, SWT.NONE);
                item.setForeground(line.getColor());
                line.setData(item);
            }
            else if (line.getLabel() != null && line.getSize() != 0)
            {
                NumberSummaryItem item = new NumberSummaryItem(summary, SWT.NONE);
                item.setText(line.getLabel().toUpperCase());
                item.setForeground(line.getColor());
                line.setData(item);
            }
            else
                line.setData(null);
            if (line.getHigh() > high)
                high = line.getHigh();
            if (line.getLow() < low)
                low = line.getLow();
        }
        
        scaler.set(high, low);

        if (drawCount <= 0)
        {
            indicatorPlot.redrawAll();
            scalePlot.redrawAll();
            updateSummary();
        }
    }

    public void clearIndicators()
    {
        for (Iterator iter2 = indicatorPlot.getIndicators().iterator(); iter2.hasNext(); )
        {
            Indicator indicator = (Indicator)iter2.next();

            for (Iterator iter = indicator.getLines().iterator(); iter.hasNext(); )
            {
                PlotLine line = (PlotLine)iter.next();
                if (line.getData() != null)
                {
                    ((SummaryItem)line.getData()).dispose();
                    line.setData(null);
                }
            }
        }

        indicatorPlot.clearIndicators();
        scaler.set(-99999999, 99999999);

        if (drawCount <= 0)
        {
            indicatorPlot.redrawAll();
            scalePlot.redrawAll();
            updateSummary();
        }
    }
    
    public void clearObjects()
    {
        indicatorPlot.getObjects().clear();
        indicatorPlot.setObjectSelection(null);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setRedraw(boolean)
     */
    public void setRedraw(boolean redraw)
    {
        super.setRedraw(redraw);
        
        if (redraw)
        {
            if (--drawCount == 0)
            {
                indicatorPlot.redrawAll();
                scalePlot.redrawAll();
                updateSummary();
            }
        }
        else
            drawCount++;
    }

    public void updateSummary(int x)
    {
        int index = (x - getIndicatorPlot().getPlotLocation().x - indicatorPlot.getMarginWidth()) / indicatorPlot.getGridWidth();
        if (index >= 0 && index < datePlot.getBarData().size())
            dateSummary.setData(datePlot.getBarData().getDate(index));
        else
            dateSummary.setData(null);

        List list = indicatorPlot.getIndicators();
        for (int i = 0; i < list.size(); i++)
        {
            Indicator indicator = (Indicator)list.get(i);
            for (Iterator iter = indicator.getLines().iterator(); iter.hasNext(); )
            {
                PlotLine line = (PlotLine)iter.next();
                int ofs = index - (datePlot.getBarData().size() - line.getSize());
                if (line.getData() != null)
                {
                    if (line.getType() == PlotLine.BAR || line.getType() == PlotLine.CANDLE)
                    {
                        BarSummaryItem item = (BarSummaryItem)line.getData();
                        if (ofs >= 0 && ofs < line.getSize())
                        {
                            if (ofs > 0)
                                item.setData(line.getBar(ofs), line.getBar(ofs - 1));
                            else
                                item.setData(line.getBar(ofs));
                        }
                        else
                            item.setData(null);
                    }
                    else
                    {
                        NumberSummaryItem item = (NumberSummaryItem)line.getData();
                        if (ofs >= 0 && ofs < line.getSize())
                            item.setData(line.getDouble(ofs));
                        else
                            item.setData(null);
                    }
                }
            }
        }

        summary.layout();
    }
    
    public void updateSummary()
    {
        int index = datePlot.getBarData().size() - 1;
        if (index >= 0)
            dateSummary.setData(datePlot.getBarData().getDate(index));
        else
            dateSummary.setData(null);

        List list = indicatorPlot.getIndicators();
        for (int i = 0; i < list.size(); i++)
        {
            Indicator indicator = (Indicator)list.get(i);
            for (Iterator iter = indicator.getLines().iterator(); iter.hasNext(); )
            {
                PlotLine line = (PlotLine)iter.next();
                if (line.getData() != null)
                {
                    if (line.getType() == PlotLine.BAR || line.getType() == PlotLine.CANDLE)
                    {
                        BarSummaryItem item = (BarSummaryItem)line.getData();
                        if (line.getSize() > 1)
                            item.setData(line.getBar(line.getSize() - 1), line.getBar(line.getSize() - 2));
                        else
                            item.setData(line.getBar(line.getSize() - 1));
                    }
                    else
                    {
                        NumberSummaryItem item = (NumberSummaryItem)line.getData();
                        item.setData(line.getDouble(line.getSize() - 1));
                    }
                }
            }
        }
        
        summary.layout();
    }

    public void addObject(ObjectPlugin object)
    {
        object.setPlot(this);
        indicatorPlot.addObject(object);
        if (drawCount <= 0)
            indicatorPlot.redrawAll();
    }
    
    public void removeObject(ObjectPlugin object)
    {
        indicatorPlot.removeObject(object);
        if (drawCount <= 0)
            indicatorPlot.redrawAll();
    }

    public IndicatorPlot getIndicatorPlot()
    {
        return indicatorPlot;
    }

    public ScalePlot getScalePlot()
    {
        return scalePlot;
    }

    public void setScaleWidth(int scaleWidth)
    {
        this.scaleWidth = scaleWidth;
        ((GridData)scalePlot.getLayoutData()).widthHint = this.scaleWidth;
    }
    
    public Summary getSummary()
    {
        return summary;
    }

    public Scaler getScaler()
    {
        return scaler;
    }
    
    public DatePlot getDatePlot()
    {
        return datePlot;
    }

    public void setDatePlot(DatePlot datePlot)
    {
        this.datePlot = datePlot;
        indicatorPlot.setBarData(datePlot.getBarData());
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseDoubleClick(MouseEvent e)
    {
    }
    
    public void addPlotSelectionListener(PlotSelectionListener listener)
    {
        selectionListeners.add(listener);
    }
    
    public void removePlotSelectionListener(PlotSelectionListener listener)
    {
        selectionListeners.remove(listener);
    }
    
    public void createNewObject(ObjectPlugin plugin, PlotMouseEvent e)
    {
        plugin.setPlot(this);
        indicatorPlot.setObjectSelection(plugin);
        if (currentCursor != crossCursor)
        {
            indicatorPlot.setCursor(crossCursor);
            currentCursor = crossCursor;
        }
        plugin.mouseDown(e);
        buttonDown = true;
    }
    
    private PlotMouseEvent createPlotMouseEvent(MouseEvent e)
    {
        PlotMouseEvent plotMouseEvent = new PlotMouseEvent();
        plotMouseEvent.x = e.x - indicatorPlot.getPlotLocation().x;
        plotMouseEvent.y = e.y;
        plotMouseEvent.display = getDisplay();
        plotMouseEvent.plot = this;
        plotMouseEvent.mouse = new Point(e.x, e.y);
        plotMouseEvent.button = e.button;
        if (scaler != null)
        {
            plotMouseEvent.value = scaler.convertToValue(e.y);
            plotMouseEvent.roundedValue = Scaler.roundToTick(plotMouseEvent.value);
        }
        if (datePlot != null)
            plotMouseEvent.date = datePlot.mapToDate(plotMouseEvent.x);
        
        return plotMouseEvent;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseDown(MouseEvent e)
    {
        PlotMouseEvent plotMouseEvent = createPlotMouseEvent(e);
        
        Object[] selection = indicatorPlot.getElementsAt(new Point(plotMouseEvent.x, plotMouseEvent.y));
        if (selection.length != 0)
        {
            int index = -1;
            for (int i = 0; i < selection.length; i++)
            {
                if (selection[i] == indicatorPlot.getObjectSelection())
                    index = i;
                if (selection[i] == indicatorPlot.getSelection())
                    index = i;
            }
            if (index == -1 || e.button == 1)
                index++;
            if (index >= selection.length)
                index = 0;

            if (selection[index] instanceof ObjectPlugin)
            {
                if (selection[index] != indicatorPlot.getObjectSelection())
                {
                    indicatorPlot.setObjectSelection((ObjectPlugin)selection[index]);
                    indicatorPlot.redrawAll();
                }

                Event event = new Event();
                event.x = e.x - indicatorPlot.getPlotLocation().x;
                event.y = e.y;
                event.type = SWT.Selection;
                event.display = getDisplay();
                event.widget = this;
                PlotSelectionEvent selectionEvent = new PlotSelectionEvent(event);
                selectionEvent.plot = this;
                selectionEvent.object = (ObjectPlugin)selection[index];
                for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); )
                    ((PlotSelectionListener)iter.next()).plotSelected(selectionEvent);

                if (e.button == 1)
                {
                    buttonDown = true;
                    ((ObjectPlugin)selection[index]).mouseDown(plotMouseEvent);
                }
                else if (currentCursor != null)
                {
                    indicatorPlot.setCursor(null);
                    currentCursor = null;
                }
            }
            else if (selection[index] instanceof Indicator && selection[index] != getIndicatorPlot().getSelection())
            {
                indicatorPlot.setSelection((Indicator)selection[index]);
                indicatorPlot.redrawAll();

                Event event = new Event();
                event.x = e.x - indicatorPlot.getPlotLocation().x;
                event.y = e.y;
                event.type = SWT.Selection;
                event.display = getDisplay();
                event.widget = this;
                PlotSelectionEvent selectionEvent = new PlotSelectionEvent(event);
                selectionEvent.plot = this;
                selectionEvent.indicator = (Indicator)selection[index];
                for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); )
                    ((PlotSelectionListener)iter.next()).plotSelected(selectionEvent);
            }
            return;
        }

        if (e.button == 1)
        {
            if (getIndicatorPlot().getSelection() != null || indicatorPlot.getObjectSelection() != null)
            {
                indicatorPlot.setObjectSelection(null);
                indicatorPlot.setSelection(null);
                indicatorPlot.redrawAll();

                Event event = new Event();
                event.x = e.x - indicatorPlot.getPlotLocation().x;
                event.y = e.y;
                event.type = SWT.Selection;
                event.display = getDisplay();
                event.widget = this;
                PlotSelectionEvent selectionEvent = new PlotSelectionEvent(event);
                selectionEvent.plot = this;
                for (Iterator iter = selectionListeners.iterator(); iter.hasNext(); )
                    ((PlotSelectionListener)iter.next()).plotSelected(selectionEvent);
                return;
            }

            buttonDown = true;
        }

        for (Iterator iter = plotMouseListeners.iterator(); iter.hasNext(); )
            ((PlotMouseListener)iter.next()).mouseDown(plotMouseEvent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseUp(MouseEvent e)
    {
        PlotMouseEvent plotMouseEvent = createPlotMouseEvent(e);

        if (buttonDown)
        {
            buttonDown = false;

            if (indicatorPlot.getObjectSelection() != null)
                indicatorPlot.getObjectSelection().mouseUp(plotMouseEvent);

            for (Iterator iter = plotMouseListeners.iterator(); iter.hasNext(); )
                ((PlotMouseListener)iter.next()).mouseUp(plotMouseEvent);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseMove(MouseEvent e)
    {
        PlotMouseEvent plotMouseEvent = createPlotMouseEvent(e);

        if (buttonDown)
        {
            if (indicatorPlot.getObjectSelection() != null)
            {
                indicatorPlot.getObjectSelection().mouseMove(plotMouseEvent);
                return;
            }

            for (Iterator iter = plotMouseListeners.iterator(); iter.hasNext(); )
                ((PlotMouseListener)iter.next()).mouseMove(plotMouseEvent);
        }
        else
        {
            for (Iterator iter = indicatorPlot.getObjects().iterator(); iter.hasNext(); )
            {
                ObjectPlugin plugin = (ObjectPlugin)iter.next();
                if (plugin.isOverHandle(plotMouseEvent.x, plotMouseEvent.y))
                {
                    if (currentCursor != crossCursor)
                    {
                        indicatorPlot.setCursor(crossCursor);
                        currentCursor = crossCursor;
                    }
                    return;
                }
                else if (plugin.isOverLine(plotMouseEvent.x, plotMouseEvent.y))
                {
                    if (currentCursor != handCursor)
                    {
                        indicatorPlot.setCursor(handCursor);
                        currentCursor = handCursor;
                    }
                    return;
                }
            }
            if (currentCursor != null)
            {
                indicatorPlot.setCursor(null);
                currentCursor = null;
            }
        }
    }

    public void addPlotMouseListener(PlotMouseListener listener)
    {
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        plotMouseListeners.add(listener);
    }

    public void removePlotMouseListener(PlotMouseListener listener)
    {
        if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        plotMouseListeners.remove(listener);
    }

    public void updateScale()
    {
        BarData barData = datePlot.getBarData();
        if (barData.size() != 0)
        {
            int first = ((- indicatorPlot.getPlotLocation().x) - indicatorPlot.getMarginWidth() - indicatorPlot.getGridWidth() / 2) / indicatorPlot.getGridWidth();
            if (first > 0)
                first--;
            int bars = (indicatorPlot.getBounds().width / indicatorPlot.getGridWidth()) + 1;
            double high = -99999999;
            double low = 99999999;
            
            for (Iterator iter = indicatorPlot.getIndicators().iterator(); iter.hasNext(); )
            {
                Indicator indicator = (Indicator)iter.next();
                
                for (Iterator iter2 = indicator.iterator(); iter2.hasNext(); )
                {
                    PlotLine plotLine = (PlotLine)iter2.next();
                    if (plotLine.getScaleFlag())
                        continue;

                    switch(plotLine.getType())
                    {
                        case PlotLine.DOT:
                        case PlotLine.DASH:
                        case PlotLine.LINE:
                        case PlotLine.HISTOGRAM:
                        case PlotLine.HISTOGRAM_BAR:
                        {
                            int ofs = barData.size() - plotLine.getSize();
                            int x = first - ofs;
                            for (int i = 0; i < bars; i++, x++)
                            {
                                if (x >= 0 && x < plotLine.getSize())
                                {
                                    if (plotLine.getData(x) > high)
                                        high = plotLine.getData(x);
                                    if (plotLine.getData(x) < low)
                                        low = plotLine.getData(x);
                                }
                            }
                            break;
                        }
                        case PlotLine.BAR:
                        case PlotLine.CANDLE:
                        {
                            int ofs = barData.size() - plotLine.getSize();
                            int x = first - ofs;
                            for (int i = 0; i < bars; i++, x++)
                            {
                                if (x >= 0 && x < plotLine.getSize())
                                {
                                    if (plotLine.getBar(x).getHigh() > high)
                                        high = plotLine.getBar(x).getHigh();
                                    if (plotLine.getBar(x).getLow() < low)
                                        low = plotLine.getBar(x).getLow();
                                }
                            }
                            break;
                        }
                    }
                }
            }

            if (high != -99999999 && low != 99999999)
            {
                scaler.set(high, low);
                if (drawCount <= 0)
                {
                    indicatorPlot.redrawAll();
                    scalePlot.redrawAll();
                }
            }
        }
    }
}
