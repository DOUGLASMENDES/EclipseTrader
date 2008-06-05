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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class DateScaleCanvas {
	private Canvas horizontalScaleCanvas;
	private Image horizontalScaleImage;

	private BaseChartViewer viewer;

	private SimpleDateFormat monthYearFormatter = new SimpleDateFormat("MMM, yyyy"); //$NON-NLS-1$
	private SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM"); //$NON-NLS-1$

	DateScaleCanvas(BaseChartViewer viewer, Composite parent) {
		this.viewer = viewer;

		horizontalScaleCanvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		horizontalScaleCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		horizontalScaleCanvas.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		((GridData) horizontalScaleCanvas.getLayoutData()).heightHint = BaseChartViewer.HORIZONTAL_SCALE_HEIGHT;
		horizontalScaleCanvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (horizontalScaleImage != null) {
            		horizontalScaleImage.dispose();
            		horizontalScaleImage = null;
            	}
            	redraw();
            }
		});
		horizontalScaleCanvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
            	if (horizontalScaleImage != null) {
            		horizontalScaleImage.dispose();
            		horizontalScaleImage = null;
            	}
            }
		});
		horizontalScaleCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	onPaint(e);
            }
		});
	}

	public Control getControl() {
		return horizontalScaleCanvas;
	}

	public Canvas getCanvas() {
		return horizontalScaleCanvas;
	}

	public void redraw() {
		horizontalScaleCanvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.TRUE);
    	horizontalScaleCanvas.redraw();
	}

	private void onPaint(PaintEvent event) {
		viewer.revalidate();

		Rectangle clientArea = horizontalScaleCanvas.getClientArea();
		boolean needsRedraw = Boolean.TRUE.equals(horizontalScaleCanvas.getData(BaseChartViewer.K_NEEDS_REDRAW));

		if (horizontalScaleImage != null && !horizontalScaleImage.isDisposed()) {
			if (horizontalScaleImage.getBounds().width != clientArea.width || horizontalScaleImage.getBounds().height != clientArea.height)
				horizontalScaleImage.dispose();
		}
		if (horizontalScaleImage == null || horizontalScaleImage.isDisposed()) {
			horizontalScaleImage = new Image(horizontalScaleCanvas.getDisplay(), clientArea.width, clientArea.height);
			needsRedraw = true;
		}

		if (needsRedraw) {
	    	Graphics graphics = new Graphics(horizontalScaleImage, viewer.getLocation(), viewer.datesAxis, new DoubleValuesAxis());
			try {
		    	graphics.fillRectangle(clientArea);

				Calendar oldDate = null;
				Calendar currentDate = Calendar.getInstance();

				for (int i = 0; i < viewer.visibleDates.length; i++) {
					Date date = viewer.visibleDates[i];
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

					int x = graphics.mapToHorizontalAxis(date);
					graphics.setForegroundColor(highlight ? new RGB(255, 0, 0) : new RGB(0, 0, 0));
					graphics.drawLine(x, 0, x, tick ? 6 : 3);
					if (tick)
						graphics.drawString(text, x - 1, 7);
				}
			} catch(Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, "Error rendering horizontal scale");
				ChartsUIActivator.log(status);
			} finally {
				horizontalScaleCanvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.FALSE);
				graphics.dispose();
			}
		}

		viewer.paintImage(event, horizontalScaleImage);
	}
}
