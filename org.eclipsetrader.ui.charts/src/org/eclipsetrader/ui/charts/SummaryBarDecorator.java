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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;

public class SummaryBarDecorator implements MouseListener, MouseMoveListener, MouseTrackListener, DisposeListener {
	private Point location;
	private boolean mouseDown;
	private List<ChartCanvas> decoratedCanvas = new ArrayList<ChartCanvas>();

	private IChartObjectVisitor summaryLabelVisitor = new IChartObjectVisitor() {
        public boolean visit(IChartObject object) {
        	ISummaryBarDecorator factory = null;
        	if (object instanceof IAdaptable) {
        		factory = (ISummaryBarDecorator) ((IAdaptable) object).getAdapter(ISummaryBarDecorator.class);
        		if (factory != null)
        			factory.updateDecorator(location != null ? location.x : SWT.DEFAULT, SWT.DEFAULT);
        	}
	        return true;
        }
	};

	public SummaryBarDecorator() {
	}

	public void decorateCanvas(ChartCanvas canvas) {
		canvas.getCanvas().addMouseListener(this);
		canvas.getCanvas().addMouseMoveListener(this);
		canvas.getCanvas().addMouseTrackListener(this);

		canvas.getCanvas().addDisposeListener(this);

		decoratedCanvas.add(canvas);
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
		if (!mouseDown) {
			location = new Point(e.x, e.y);
			updateLabel();
			mouseDown = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		if (e.button != 1)
			return;
		if (mouseDown) {
			location = null;
			updateLabel();
			mouseDown = false;
		}
	}

	private void updateLabel() {
		for (ChartCanvas canvas : decoratedCanvas) {
	    	if (canvas.getChartObject() != null)
	    		canvas.getChartObject().accept(summaryLabelVisitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseEnter(MouseEvent e) {
		if (mouseDown) {
			location = new Point(e.x, e.y);
			updateLabel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseExit(MouseEvent e) {
		if (location != null) {
			location = null;
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
		if (mouseDown) {
			location = new Point(e.x, e.y);
			updateLabel();
		}
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
}
