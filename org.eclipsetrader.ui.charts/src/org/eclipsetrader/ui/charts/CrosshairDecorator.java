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

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class CrosshairDecorator implements MouseListener, MouseMoveListener, DisposeListener, PaintListener {
	public int OFFSET_X = 12;
	public int OFFSET_Y = 12;

	private boolean active = true;
	private boolean mouseDown;
	private Point location;
	private ChartCanvas focusCanvas;

	private List<ChartCanvas> decoratedCanvas = new ArrayList<ChartCanvas>();

	private class DecoratorToolTip extends ToolTip {
		private Control parent;
		private CLabel label;
		private String text;

		public DecoratorToolTip(Control control) {
	        super(control, ToolTip.NO_RECREATE, true);
	        this.parent = control;
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.window.ToolTip#createToolTipContentArea(org.eclipse.swt.widgets.Event, org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Composite createToolTipContentArea(Event event, Composite parent) {
        	label = new CLabel(parent, SWT.SHADOW_NONE);
        	label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        	label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        	if (text != null)
        		label.setText(text);
	        return label;
        }

		public String getText() {
        	return text;
        }

		public void setText(String text) {
        	this.text = text;
        	if (label != null && !label.isDisposed()) {
        		label.setText(text);
            	label.getParent().pack();
        	}
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.window.ToolTip#shouldCreateToolTip(org.eclipse.swt.widgets.Event)
         */
        @Override
        protected boolean shouldCreateToolTip(Event event) {
            return label == null || label.isDisposed();
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.window.ToolTip#getLocation(org.eclipse.swt.graphics.Point, org.eclipse.swt.widgets.Event)
         */
        @Override
        public Point getLocation(Point tipSize, Event event) {
			Rectangle bounds = parent.getBounds();
			Point p = new Point(event.x + OFFSET_X, event.y + OFFSET_Y);
			if ((p.x + tipSize.x) > (bounds.width - BaseChartViewer.VERTICAL_SCALE_WIDTH)) {
				p.x = event.x - tipSize.x - OFFSET_X;
				if (p.x < 0)
					p.x = event.x + OFFSET_X;
			}
			if ((p.y + tipSize.y) > bounds.height) {
				p.y = event.y - tipSize.y - OFFSET_Y;
				if (p.y < 0)
					p.y = event.y + OFFSET_Y;
			}
            return parent.toDisplay(p);
        }

		/* (non-Javadoc)
         * @see org.eclipse.jface.window.ToolTip#show(org.eclipse.swt.graphics.Point)
         */
        @Override
        public void show(Point location) {
        	location = parent.toControl(location);
        	if (label != null && !label.isDisposed()) {
        		Shell tip = (Shell) label.getParent();
        		Event event = new Event();
        		event.x = location.x;
        		event.y = location.y;
        		event.widget = parent;
    			tip.setLocation(getLocation(tip.getSize(), event));
        	}
        	else
        		super.show(location);
        }
	}

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
		return mouseDown;
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
		if (e.widget instanceof Canvas && e.button == 1 && active) {
			if (!mouseDown) {
				location = new Point(e.x, e.y);
				focusCanvas = (ChartCanvas) e.widget.getData();
				mouseDown = true;
				drawLines(location);
				updateLabel(location);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (e.widget instanceof Canvas && e.button == 1 && active) {
			if (mouseDown) {
				mouseDown = false;
				if (location != null) {
					restoreBackground(location);
					if (tooltip != null)
						tooltip.hide();
				}
				focusCanvas = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent e) {
		if (e.widget instanceof Canvas && mouseDown && active) {
			mouseDown = false;
			restoreBackground(location);
			mouseDown = true;

			location = new Point(e.x, e.y);
			drawLines(location);
			updateLabel(location);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    public void paintControl(PaintEvent e) {
    	if (mouseDown && e.widget instanceof Canvas && active)
    		drawLines(location);
    }

	private void drawLines(Point location) {
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
		if (tooltip != null) {
	    	summary = new StringBuilder();
			for (ChartCanvas canvas : decoratedCanvas) {
		    	if (canvas.getChartObject() != null)
		    		canvas.getChartObject().accept(summaryLabelVisitor);
			}
    		tooltip.setText(summary.toString());

			Point p = new Point(location.x, location.y);
			p = focusCanvas.getCanvas().toDisplay(p);
			tooltip.show(p);
		}
	}
}
