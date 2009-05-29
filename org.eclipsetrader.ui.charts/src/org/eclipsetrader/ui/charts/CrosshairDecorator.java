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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class CrosshairDecorator implements MouseListener, MouseMoveListener, MouseTrackListener, DisposeListener, PaintListener {
	public static final int MODE_OFF = 0;
	public static final int MODE_MOUSE_DOWN = 1;
	public static final int MODE_MOUSE_HOVER = 2;

	private boolean active = true;

	private int mode = MODE_OFF;
	private boolean showSummaryTooltip;
	private boolean mouseDown;
	private boolean skipPaint;
	private Point location;
	private ChartCanvas focusCanvas;

	private List<ChartCanvas> decoratedCanvas = new ArrayList<ChartCanvas>();

	private DecoratorToolTip tooltip;

	private StringBuilder summary = new StringBuilder();

	private IChartObjectVisitor summaryLabelVisitor = new IChartObjectVisitor() {
        public boolean visit(IChartObject object) {
    		String s = object.getToolTip(location.x, SWT.DEFAULT);
        	if (s != null) {
	        	if (summary.length() != 0)
	        		summary.append("\r\n");
	        	summary.append(s);
        	}
	        return true;
        }
	};

	public CrosshairDecorator() {
	}

	public void decorateCanvas(ChartCanvas canvas) {
		canvas.getCanvas().addMouseListener(this);
		canvas.getCanvas().addMouseMoveListener(this);
		canvas.getCanvas().addMouseTrackListener(this);
		canvas.getCanvas().addDisposeListener(this);
		canvas.getCanvas().addPaintListener(this);

		decoratedCanvas.add(canvas);
	}

	public void createSummaryLabel(Composite parent) {
		tooltip = new DecoratorToolTip(parent);
		tooltip.setRespectDisplayBounds(true);
		tooltip.setRespectMonitorBounds(true);
	}

	public void dispose() {
		for (ChartCanvas canvas : decoratedCanvas) {
			canvas.getCanvas().removeMouseListener(this);
			canvas.getCanvas().removeMouseMoveListener(this);
			canvas.getCanvas().removeMouseTrackListener(this);
			canvas.getCanvas().removeDisposeListener(this);
			canvas.getCanvas().removePaintListener(this);
		}
		decoratedCanvas.clear();
	}

	public void activate() {
		if (!active)
			active = true;
	}

	public void deactivate() {
		if (active)
			active = false;
	}

	public boolean isActive() {
    	return active;
    }

	public boolean isVisible() {
		return location != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		for (ChartCanvas canvas : decoratedCanvas) {
			if (canvas.getCanvas() == e.widget) {
				decoratedCanvas.remove(canvas);
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		if (e.button != 1)
			return;
		if (mode == MODE_MOUSE_DOWN && !mouseDown && active) {
			location = new Point(e.x, e.y);
			focusCanvas = (ChartCanvas) e.widget.getData();
			drawLines(location);
			updateLabel(location);
			mouseDown = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (e.button != 1)
			return;
		if (mode == MODE_MOUSE_DOWN && mouseDown) {
			skipPaint = true;
			restoreBackground(location);
			skipPaint = false;

			location = null;
			mouseDown = false;

			if (tooltip != null)
				tooltip.hide();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
		if (mode == MODE_MOUSE_HOVER || mouseDown) {
			focusCanvas = (ChartCanvas) e.widget.getData();
			location = new Point(e.x, e.y);

			drawLines(location);
			updateLabel(location);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
		if (location != null) {
			skipPaint = true;
			restoreBackground(location);
			skipPaint = false;

			location = null;

			if (tooltip != null)
				tooltip.hide();
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseHover(MouseEvent e) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent e) {
		if (mode == MODE_MOUSE_HOVER || mouseDown) {
			skipPaint = true;
			restoreBackground(location);
			skipPaint = false;

			focusCanvas = (ChartCanvas) e.widget.getData();
			location = new Point(e.x, e.y);

			drawLines(location);
			updateLabel(location);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e) {
    	if (!skipPaint && active)
    		drawLines(location);
    }

	private void drawLines(Point location) {
		if (focusCanvas == null || location == null)
			return;

		Rectangle bounds = focusCanvas.getCanvas().getBounds();

		GC gc = new GC(focusCanvas.getCanvas());
		gc.drawLine(location.x, 0, location.x, bounds.height);
		gc.drawLine(0, location.y, bounds.width, location.y);
		gc.dispose();

		for (ChartCanvas canvas : decoratedCanvas) {
			if (canvas.getCanvas() != focusCanvas.getCanvas()) {
				bounds = canvas.getCanvas().getBounds();
				gc = new GC(canvas.getCanvas());
				gc.drawLine(location.x, 0, location.x, bounds.height);
				gc.dispose();
			}
		}
	}

	private void restoreBackground(Point location) {
		if (focusCanvas == null || location == null)
			return;

		Rectangle bounds = focusCanvas.getCanvas().getBounds();
		focusCanvas.getCanvas().redraw(location.x, 0, 1, bounds.height, false);
		focusCanvas.getCanvas().update();
		focusCanvas.getCanvas().redraw(0, location.y, bounds.width, 1, false);
		focusCanvas.getCanvas().update();

		for (ChartCanvas canvas : decoratedCanvas) {
			if (canvas.getCanvas() != focusCanvas.getCanvas()) {
				bounds = canvas.getCanvas().getBounds();
				canvas.getCanvas().redraw(location.x, 0, 1, bounds.height, false);
				canvas.getCanvas().update();
			}
		}
	}

	private void updateLabel(Point location) {
		if (tooltip != null && location != null) {
	    	summary = new StringBuilder();
			for (ChartCanvas canvas : decoratedCanvas) {
		    	if (canvas.getChartObject() != null)
		    		canvas.getChartObject().accept(summaryLabelVisitor);
			}
    		tooltip.setText(summary.toString());

    		if (showSummaryTooltip) {
        		if (!"".equals(tooltip.getText())) {
        			Point p = new Point(location.x, location.y);
        			p = focusCanvas.getCanvas().toDisplay(p);
        			tooltip.show(p);
        		}
        		else
        			tooltip.hide();
    		}
		}
	}

	public int getMode() {
    	return mode;
    }

	public void setMode(int mode) {
    	this.mode = mode;
    }

	public boolean isShowSummaryTooltip() {
    	return showSummaryTooltip;
    }

	public void setShowSummaryTooltip(boolean showSummaryTooltip) {
    	this.showSummaryTooltip = showSummaryTooltip;
    	if (!showSummaryTooltip && tooltip != null)
			tooltip.hide();
    }
}
