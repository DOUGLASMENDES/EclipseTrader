/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
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
import java.util.Observable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.Graphics;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IGraphics;
import org.eclipsetrader.ui.charts.Util;

public class CurrentBook extends Observable implements IChartObject, IAdaptable {
	ITrade trade;
	IBook book;

	int width;
	int height;
	int boxWidth = 100;
	long biggestQuantity;

	RGB bidForeground = new RGB(255, 0, 0);
	RGB askForeground = new RGB(0, 255, 0);
	RGB bidBackground;
	RGB askBackground;

	NumberFormat numberFormat = NumberFormat.getInstance();

	public CurrentBook() {
	    numberFormat.setGroupingUsed(true);
	    numberFormat.setMinimumIntegerDigits(1);
	    numberFormat.setMinimumFractionDigits(0);
	    numberFormat.setMaximumFractionDigits(4);
	}

	public void setBook(IBook book) {
    	this.book = book;
    	setChanged();
    	notifyObservers();
    }

	public void setTrade(ITrade trade) {
    	this.trade = trade;
    	setChanged();
    	notifyObservers();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
	 */
	public void accept(IChartObjectVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
	 */
	public IDataSeries getDataSeries() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
	 */
	public String getToolTip() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
	 */
	public String getToolTip(int x, int y) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusGained(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	public void handleFocusGained(ChartObjectFocusEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#handleFocusLost(org.eclipsetrader.ui.charts.ChartObjectFocusEvent)
	 */
	public void handleFocusLost(ChartObjectFocusEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#invalidate()
	 */
	public void invalidate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
	 */
	public void paint(IGraphics graphics) {
		if (book == null)
			return;

		IBookEntry[] bidEntry = book.getBidProposals();
		IBookEntry[] askEntry = book.getAskProposals();
		if (bidEntry.length == 0 && askEntry.length == 0)
			return;

		calculateBiggestQuantity();
		if (biggestQuantity == 0)
			return;

		int verticalCenter = 0;
		if (trade != null && trade.getPrice() != null)
			verticalCenter = graphics.mapToVerticalAxis(trade.getPrice());
		else if (bidEntry.length != 0 && askEntry.length != 0) {
			double middle = (askEntry[0].getPrice() + bidEntry[0].getPrice()) / 2;
			verticalCenter = graphics.mapToVerticalAxis(middle);
		}
		else if (bidEntry.length != 0)
			verticalCenter = graphics.mapToVerticalAxis(bidEntry[0].getPrice());
		else if (askEntry.length != 0)
			verticalCenter = graphics.mapToVerticalAxis(askEntry[0].getPrice());

		bidBackground = Util.blend(bidForeground, graphics.getBackgroundColor(), 25);
		askBackground = Util.blend(askForeground, graphics.getBackgroundColor(), 25);

		Point extents = graphics.stringExtent(numberFormat.format(biggestQuantity));

		int y = verticalCenter + extents.y + 2;

		for (int i = 0; i < 5 && i < bidEntry.length; i++) {
			int w = getBarWidth(bidEntry[i]);
			int x = width - w - 10;

			graphics.setForegroundColor(bidForeground);
			graphics.setBackgroundColor(bidBackground);

			graphics.fillRectangle(x, y - extents.y + 2, w, extents.y - 4);
			graphics.drawRectangle(x, y - extents.y + 2, w, extents.y - 4);

			y += extents.y + 4;
		}

		y = verticalCenter - 2;

		for (int i = 0; i < 5 && i < askEntry.length; i++) {
			int w = getBarWidth(askEntry[i]);
			int x = width - w - 10;

			graphics.setForegroundColor(askForeground);
			graphics.setBackgroundColor(askBackground);

			graphics.fillRectangle(x, y - extents.y + 2, w, extents.y - 4);
			graphics.drawRectangle(x, y - extents.y + 2, w, extents.y - 4);

			y -= extents.y + 4;
		}
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paintScale(org.eclipsetrader.ui.charts.Graphics)
     */
    public void paintScale(Graphics graphics) {
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
	 */
	public void setDataBounds(DataBounds bounds) {
		this.width = bounds.width;
		this.height = bounds.height;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
    	if (adapter.isAssignableFrom(getClass()))
    		return this;
	    return null;
    }

	void calculateBiggestQuantity() {
		biggestQuantity = 0;

		IBookEntry[] entry = book.getBidProposals();
		for (int i = 0; i < entry.length && i < 5; i++)
			biggestQuantity = Math.max(biggestQuantity, entry[i].getQuantity());

		entry = book.getAskProposals();
		for (int i = 0; i < entry.length && i < 5; i++)
			biggestQuantity = Math.max(biggestQuantity, entry[i].getQuantity());
    }

	int getBarWidth(IBookEntry entry) {
	    return (int) (entry.getQuantity().doubleValue() / biggestQuantity * 100.0);
    }
}
