/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.charts.DataSeriesSubsetVisitor;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.NumericDataSeries;
import org.eclipsetrader.core.charts.OHLCDataSeries;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.AdaptableWrapper;
import org.eclipsetrader.ui.charts.IBarDecorator;
import org.eclipsetrader.ui.charts.IChartRenderer;
import org.eclipsetrader.ui.charts.IColorRegistry;
import org.eclipsetrader.ui.charts.ILineDecorator;
import org.eclipsetrader.ui.charts.IObjectRenderer;
import org.eclipsetrader.ui.charts.IScaleRenderer;
import org.eclipsetrader.ui.charts.RenderTarget;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartDocumentRenderer implements IChartRenderer, IScaleRenderer, IColorRegistry {
	private NumberFormat nf = NumberFormat.getInstance();
	private SimpleDateFormat monthYearFormatter = new SimpleDateFormat("MMM, yyyy"); //$NON-NLS-1$
	private SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM"); //$NON-NLS-1$
    private Map<RGB, Color> colorRegistry = new HashMap<RGB, Color>();

    private class CacheData {
    	Object cachedFirstValue;
    	Object cachedLastValue;
    	IDataSeries cachedSeries;
    }

    private Map<Object, CacheData> cacheMap = new HashMap<Object, CacheData>();

	public ChartDocumentRenderer() {
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartRenderer#dispose()
     */
    public void dispose() {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartRenderer#renderBackground(org.eclipsetrader.ui.charts.RenderTarget)
     */
    public void renderBackground(RenderTarget graphics) {
    	graphics.gc.fillRectangle(0, 0, graphics.width, graphics.height);

    	IDataSeries series = getVisibleSeries(graphics.input, graphics.firstValue, graphics.lastValue);
    	if (series == null)
    		return;
		if (series.getFirst() == null || series.getLast() == null)
			return;

    	graphics.verticalAxis.clear();
		graphics.verticalAxis.addValues(new Object[] { series.getHighest(), series.getLowest() });
		graphics.verticalAxis.computeSize(graphics.height);

		int dashes[] = { 3, 3 };
		graphics.gc.setLineDash(dashes);
		graphics.gc.setForeground(getColor(blend(graphics.gc.getForeground().getRGB(), graphics.gc.getBackground().getRGB(), 15)));

		Date firstDate = (Date) series.getFirst().getAdapter(Date.class);
		Date lastDate = (Date) series.getLast().getAdapter(Date.class);
		if (firstDate != null && lastDate != null) {
			Calendar oldDate = null;
			Calendar currentDate = Calendar.getInstance();

			Object[] scaleArray = graphics.horizontalAxis.getValues();
			int index = Collections.binarySearch(Arrays.asList(scaleArray), firstDate, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
	                return ((Date) o1).compareTo((Date) o2);
                }
			});
			index = index < 0 ? -index : index;

			for (int i = index; i < scaleArray.length; i++) {
				Date date = (Date) scaleArray[i];
				if (date.before(firstDate))
					continue;
				if (date.after(lastDate))
					continue;

				boolean tick = false;
				currentDate.setTime(date);

				if (oldDate != null) {
					if (currentDate.get(Calendar.YEAR) != oldDate.get(Calendar.YEAR))
						tick = true;
					else if (currentDate.get(Calendar.MONTH) != oldDate.get(Calendar.MONTH))
						tick = true;
					oldDate.setTime(date);
				}
				else {
					oldDate = Calendar.getInstance();
					oldDate.setTime(date);
				}

				if (tick) {
					int x = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
					graphics.gc.drawLine(x, 0, x, graphics.height);
				}
			}
		}

		Object[] scaleArray = graphics.verticalAxis.getValues();
		for (int loop = 0; loop < scaleArray.length; loop++) {
			int y = graphics.verticalAxis.mapToAxis(scaleArray[loop]);
			graphics.gc.drawLine(0, y, graphics.width, y);
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartRenderer#renderElement(org.eclipsetrader.ui.charts.RenderTarget, java.lang.Object)
     */
    public void renderElement(RenderTarget graphics, Object element) {
		try {
			IAdaptable adaptableElement = (IAdaptable) element;
	    	IDataSeries series = getVisibleSeries(element, graphics.firstValue, graphics.lastValue);

	    	IObjectRenderer renderer = (IObjectRenderer) adaptableElement.getAdapter(IObjectRenderer.class);
			if (renderer != null) {
				graphics.registry = this;
				renderer.renderObject(graphics, series);
			}
			else {
		    	if (series != null) {
		        	graphics.verticalAxis.clear();
		    		graphics.verticalAxis.addValues(new Object[] { series.getHighest(), series.getLowest() });

		    		IAdaptable[] values = series.getValues();
					if (series instanceof OHLCDataSeries) {
			    		IBarDecorator decorator = (IBarDecorator) adaptableElement.getAdapter(IBarDecorator.class);
						Color positiveColor = getColor(decorator != null ? decorator.getPositiveBarColor() : new RGB(0, 255, 0));
						Color negativeColor = getColor(decorator != null ? decorator.getNegativeBarColor() : new RGB(255, 0, 0));
						renderBars(graphics, values, 3, positiveColor, negativeColor);
					}
			    	else if (series instanceof NumericDataSeries) {
			    		ILineDecorator decorator = (ILineDecorator) adaptableElement.getAdapter(ILineDecorator.class);
						Color color = getColor(decorator != null ? decorator.getColor() : new RGB(0, 0, 0));
						renderLine(graphics, values, color);
			    	}
		    	}
			}
		} catch (Error e) {
			if (ChartsUIActivator.getDefault() == null)
				throw e;
			ChartsUIActivator.log(new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, "Error rendering element"));
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IScaleRenderer#renderHorizontalScale(org.eclipsetrader.ui.charts.RenderTarget)
     */
    public void renderHorizontalScale(RenderTarget graphics) {
    	graphics.gc.fillRectangle(0, 0, graphics.width, graphics.height);

    	Color color = graphics.gc.getForeground();
    	Color highLightColor = getColor(new RGB(255, 0, 0));

    	Date firstDate = (Date) (graphics.firstValue instanceof IAdaptable ? ((IAdaptable) graphics.firstValue).getAdapter(Date.class) : graphics.firstValue);
		Date lastDate = (Date) (graphics.lastValue instanceof IAdaptable ? ((IAdaptable) graphics.lastValue).getAdapter(Date.class) : graphics.lastValue);
		if (firstDate != null && lastDate != null) {
			Calendar oldDate = null;
			Calendar currentDate = Calendar.getInstance();

			Object[] scaleArray = graphics.horizontalAxis.getValues();
			int index = Collections.binarySearch(Arrays.asList(scaleArray), firstDate, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
	                return ((Date) o1).compareTo((Date) o2);
                }
			});
			index = index < 0 ? -index : index;

			for (int i = index; i < scaleArray.length; i++) {
				Date date = (Date) scaleArray[i];
				if (date.before(firstDate))
					continue;
				if (date.after(lastDate))
					continue;

				boolean tick = false; // Draw a longer tick
				boolean highlight = false; // Highlight the tick
				String text = "";

				currentDate.setTime(date);

				if (oldDate != null) {
					if (currentDate.get(Calendar.YEAR) != oldDate.get(Calendar.YEAR)) {
						tick = true;
						highlight = true;
						text = monthYearFormatter.format(currentDate.getTime());
					}
					else if (currentDate.get(Calendar.MONTH) != oldDate.get(Calendar.MONTH)) {
						tick = true;
						highlight = false;
						text = monthFormatter.format(currentDate.getTime());
					}
					oldDate.setTime(date);
				}
				else {
					oldDate = Calendar.getInstance();
					oldDate.setTime(date);
				}

				int x = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
				graphics.gc.setForeground(highlight ? highLightColor : color);
				graphics.gc.drawLine(x, 0, x, tick ? 6 : 3);
				if (tick)
					graphics.gc.drawString(text, x - 1, 7, true);
			}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IScaleRenderer#renderVerticalScale(org.eclipsetrader.ui.charts.RenderTarget)
     */
    public void renderVerticalScale(RenderTarget graphics) {
    	graphics.gc.fillRectangle(0, 0, graphics.width, graphics.height);

    	IDataSeries series = getVisibleSeries(graphics.input, graphics.firstValue, graphics.lastValue);
    	if (series == null)
    		return;

    	graphics.verticalAxis.clear();
		graphics.verticalAxis.addValues(new Object[] { series.getHighest(), series.getLowest() });
		graphics.verticalAxis.computeSize(graphics.height);

		Object[] scaleArray = graphics.verticalAxis.getValues();
		for (int loop = 0; loop < scaleArray.length; loop++) {
			int y = graphics.verticalAxis.mapToAxis(scaleArray[loop]);
			String s = nf.format(scaleArray[loop]);
			int h = graphics.gc.stringExtent(s).y / 2;
			graphics.gc.drawLine(0, y, 4, y);
			graphics.gc.drawString(s, 7, y - h);
		}
    }

    protected IDataSeries getVisibleSeries(Object element, Object first, Object last) {
    	CacheData cache = cacheMap.get(element);
    	if (cache != null) {
        	if (cache.cachedFirstValue == first && cache.cachedLastValue == last)
        		return cache.cachedSeries;
    	}

		IDataSeries dataSeries = (IDataSeries) ((IAdaptable) element).getAdapter(IDataSeries.class);
		if (dataSeries == null)
			return null;

    	IAdaptable firstValue = first instanceof IAdaptable ? (IAdaptable) first : new AdaptableWrapper(first);
		IAdaptable lastValue = last instanceof IAdaptable ? (IAdaptable) last : new AdaptableWrapper(last);

		cache = new CacheData();
		cache.cachedFirstValue = first;
		cache.cachedLastValue = last;

		DataSeriesSubsetVisitor visitor = new DataSeriesSubsetVisitor(firstValue, lastValue);
		visitor.visit(dataSeries);
		cache.cachedSeries = visitor.getSubset();
		cacheMap.put(dataSeries, cache);

		return cache.cachedSeries;
    }

    public void clearCache() {
    	cacheMap.clear();
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.renderers.IColorRegistry#getColor(org.eclipse.swt.graphics.RGB)
     */
    public Color getColor(RGB rgb) {
		Color color = colorRegistry.get(rgb);
		if (color == null || color.isDisposed()) {
			color = rgb != null ? new Color(Display.getCurrent(), rgb) : null;
			if (color != null)
				colorRegistry.put(rgb, color);
		}
		return color;
	}

	protected void renderLine(RenderTarget graphics, IAdaptable[] values, Color color) {
		int[] pointArray = new int[values.length * 2];
		for (int i = 0, pa = 0; i < values.length; i++) {
			Date date = (Date) values[i].getAdapter(Date.class);
			Number value = (Number) values[i].getAdapter(Number.class);
			pointArray[pa++] = graphics.horizontalAxis.mapToAxis(date) + graphics.x;
			pointArray[pa++] = graphics.verticalAxis.mapToAxis(value);
		}

		graphics.gc.setLineStyle(SWT.LINE_SOLID);

		graphics.gc.setForeground(color);
		graphics.gc.setLineWidth(1);
		graphics.gc.drawPolyline(pointArray);
	}

	protected void renderBars(RenderTarget event, IAdaptable[] values, int width, Color positiveColor, Color negativeColor) {
		int half = width / 2;

		event.gc.setLineStyle(SWT.LINE_SOLID);
		event.gc.setLineWidth(1);

		for (int i = 0; i < values.length; i++) {
			IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
			if (ohlc == null)
				continue;

			int h = event.verticalAxis.mapToAxis(ohlc.getHigh());
			int l = event.verticalAxis.mapToAxis(ohlc.getLow());
			int c = event.verticalAxis.mapToAxis(ohlc.getClose());
			int o = event.verticalAxis.mapToAxis(ohlc.getOpen());

			int x = event.horizontalAxis.mapToAxis(ohlc.getDate()) + event.x;

			event.gc.setForeground(ohlc.getClose() >= ohlc.getOpen() ? positiveColor : negativeColor);
			event.gc.drawLine(x, h, x, l);
			event.gc.drawLine(x - half, o, x, o);
			event.gc.drawLine(x, c, x + half + 1, c);
		}
	}

	private RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

    private int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
	}
}
