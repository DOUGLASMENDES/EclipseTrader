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

import java.util.Date;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class ChartToolEditor {
	private BaseChartViewer viewer;
	private ChartCanvas chartCanvas;
	private Graphics graphics;
	private IEditableChartObject object;

	public static class ChartObjectEditorEvent {
		public int x;
		public int y;
		public Date date;
		public Number value;
		public int button;
		public int count;
		public Display display;
		public ChartCanvas chartCanvas;
		public Canvas canvas;
		public IGraphics graphics;
	}

	private MouseListener mouseListener = new MouseListener() {
        public void mouseDoubleClick(MouseEvent e) {
        }

        public void mouseDown(MouseEvent e) {
        	handleMouseDown(createEvent(e));
        }

        public void mouseUp(MouseEvent e) {
        	handleMouseUp(createEvent(e));
        }
	};

	private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        public void mouseMove(MouseEvent e) {
        	handleMouseMove(createEvent(e));
        }
	};

	public ChartToolEditor() {
	}

	public void activate(BaseChartViewer viewer, ChartCanvas chartCanvas, IEditableChartObject object) {
		if (this.chartCanvas != null)
			deactivate();

		this.viewer = viewer;
		this.chartCanvas = chartCanvas;
		this.object = object;

		this.graphics = new Graphics(chartCanvas.getCanvas(), viewer.getLocation(), viewer.datesAxis, chartCanvas.getVerticalAxis());

		chartCanvas.getCanvas().addMouseListener(mouseListener);
		chartCanvas.getCanvas().addMouseMoveListener(mouseMoveListener);
	}

	public boolean isActive() {
		return this.object != null;
	}

	public void deactivate() {
		if (this.chartCanvas != null) {
			chartCanvas.getCanvas().removeMouseListener(mouseListener);
			chartCanvas.getCanvas().removeMouseMoveListener(mouseMoveListener);
			this.chartCanvas = null;
		}
		if (this.graphics != null) {
			this.graphics.dispose();
			this.graphics = null;
		}
		this.object = null;
	}

	protected void handleMouseDown(ChartObjectEditorEvent e) {
		if (object != null)
			object.handleMouseDown(e);
    }

    protected void handleMouseUp(ChartObjectEditorEvent e) {
		if (object != null) {
			object.handleMouseUp(e);
			viewer.deactivateEditor();
		}
    }

    protected void handleMouseMove(ChartObjectEditorEvent e) {
		if (object != null)
			object.handleMouseMove(e);
    }

    public ChartCanvas getChartCanvas() {
    	return chartCanvas;
    }

	public IEditableChartObject getObject() {
    	return object;
    }

	protected ChartObjectEditorEvent createEvent(MouseEvent e) {
    	ChartObjectEditorEvent event = new ChartObjectEditorEvent();
    	event.x = e.x;
    	event.y = e.y;
    	event.date = (Date) viewer.datesAxis.mapToValue(e.x + viewer.getLocation().x);
    	event.value = (Number) chartCanvas.getVerticalAxis().mapToValue(e.y);
    	event.button = e.button;
    	event.count = e.count;
    	event.display = e.display;
    	event.chartCanvas = chartCanvas;
    	event.canvas = chartCanvas.getCanvas();
    	event.graphics = graphics;
    	return event;
    }
}
