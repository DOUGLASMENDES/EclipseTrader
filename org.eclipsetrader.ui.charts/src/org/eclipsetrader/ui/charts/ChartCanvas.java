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

import java.text.NumberFormat;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartCanvas {
	public static final int VERTICAL_SCALE_WIDTH = 86;

	private Composite composite;
	private Composite summary;
	private Canvas canvas;
	private Canvas verticalScaleCanvas;

	private Image image;
	private Image verticalScaleImage;

	private Label label;

	private BaseChartViewer viewer;
	private IChartObject chartObject;
	private TimeSpan resolutionTimeSpan;
	private DoubleValuesAxis verticalAxis;
	private NumberFormat nf = NumberFormat.getInstance();

	ChartCanvas(BaseChartViewer viewer, Composite parent) {
		this.viewer = viewer;

		composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		summary = new Composite(composite, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginTop = rowLayout.marginBottom = 1;
		summary.setLayout(rowLayout);
		summary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		summary.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		summary.setBackgroundMode(SWT.INHERIT_FORCE);
		summary.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	Rectangle bounds = summary.getBounds();
            	e.gc.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);
            }
		});

		canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		canvas.setData(this);
		canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (image != null) {
            		image.dispose();
            		image = null;
            	}
            	redraw();
            }
		});
		canvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
            	if (image != null) {
            		image.dispose();
            		image = null;
            	}
            }
		});
		canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	if (chartObject != null)
            		onPaint(e);
            }
		});

		verticalScaleCanvas = new Canvas(composite, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		verticalScaleCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		verticalScaleCanvas.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
		((GridData) verticalScaleCanvas.getLayoutData()).widthHint = VERTICAL_SCALE_WIDTH;
		verticalScaleCanvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (verticalScaleImage != null) {
            		verticalScaleImage.dispose();
            		verticalScaleImage = null;
            	}
            	redraw();
            }
		});
		verticalScaleCanvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
            	if (verticalScaleImage != null) {
            		verticalScaleImage.dispose();
            		verticalScaleImage = null;
            	}
            }
		});
		verticalScaleCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	if (chartObject != null)
            		onPaintVerticalScale(e);
            }
		});

		label = new Label(verticalScaleCanvas, SWT.NONE);
		label.setBackground(verticalScaleCanvas.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		label.setBounds(-200, 0, 0, 0);
	}

	public void dispose() {
		composite.dispose();
	}

	public boolean isDisposed() {
		return composite.isDisposed();
	}

	public Control getControl() {
		return composite;
	}

	public Canvas getCanvas() {
    	return canvas;
    }

	public Canvas getVerticalScaleCanvas() {
    	return verticalScaleCanvas;
    }

	public DoubleValuesAxis getVerticalAxis() {
    	return verticalAxis;
    }

	private void buildVerticalAxis() {
		if (verticalAxis == null)
			verticalAxis = new DoubleValuesAxis();

		if (Boolean.TRUE.equals(verticalScaleCanvas.getData(BaseChartViewer.K_NEEDS_REDRAW))) {
			verticalAxis.clear();
			chartObject.accept(new IChartObjectVisitor() {
	            public boolean visit(IChartObject object) {
	            	if (object.getDataSeries() != null) {
	            		IDataSeries series = object.getDataSeries().getSeries(new AdaptableWrapper(viewer.firstDate), new AdaptableWrapper(viewer.lastDate));
	            		if (series != null)
	            			verticalAxis.addValues(new Object[] { series.getLowest(), series.getHighest() });
	            	}
		            return true;
	            }
	    	});
		}
	}

	private void onPaint(PaintEvent event) {
		viewer.revalidate();

		Rectangle clientArea = canvas.getClientArea();
		boolean needsRedraw = Boolean.TRUE.equals(canvas.getData(BaseChartViewer.K_NEEDS_REDRAW));

		if (image != null && !image.isDisposed()) {
			if (image.getBounds().width != clientArea.width || image.getBounds().height != clientArea.height)
				image.dispose();
		}
		if (image == null || image.isDisposed()) {
			image = new Image(canvas.getDisplay(), clientArea.width, clientArea.height);
			needsRedraw = true;
		}

		if (needsRedraw) {
			buildVerticalAxis();

			verticalAxis.computeSize(clientArea.height);

	    	Graphics graphics = new Graphics(image, viewer.getLocation(), viewer.datesAxis, verticalAxis);
			try {
		    	graphics.fillRectangle(clientArea);

		    	graphics.pushState();

		    	graphics.setLineDash(new int[] { 3, 3 });
				graphics.setForegroundColor(viewer.blend(graphics.getForegroundColor(), graphics.getBackgroundColor(), 15));

				Calendar oldDate = null;
				Calendar currentDate = Calendar.getInstance();

				for (int i = 0; i < viewer.visibleDates.length; i++) {
					Date date = viewer.visibleDates[i];
					boolean tick = false;
					currentDate.setTime(date);

					if (oldDate != null) {
						if (resolutionTimeSpan == null || resolutionTimeSpan.getUnits() == TimeSpan.Units.Days) {
							if (currentDate.get(Calendar.YEAR) != oldDate.get(Calendar.YEAR))
								tick = true;
							else if (currentDate.get(Calendar.MONTH) != oldDate.get(Calendar.MONTH))
								tick = true;
						}
						else {
							if (currentDate.get(Calendar.MONTH) != oldDate.get(Calendar.MONTH))
								tick = true;
							else if (currentDate.get(Calendar.DAY_OF_MONTH) != oldDate.get(Calendar.DAY_OF_MONTH))
								tick = true;
						}
						oldDate.setTime(date);
					}
					else {
						oldDate = Calendar.getInstance();
						oldDate.setTime(date);
					}

					if (tick) {
						int x = graphics.mapToHorizontalAxis(date);
						graphics.drawLine(x, 0, x, clientArea.height);
					}
				}

				Object[] numberArray = verticalAxis.getValues();
				for (int i = 0; i < numberArray.length; i++) {
					int y = graphics.mapToVerticalAxis(numberArray[i]);
					graphics.drawLine(0, y, clientArea.width, y);
				}

				graphics.popState();

				Double lowestValue = (Double) verticalAxis.getFirstValue();
				Double highestValue = (Double) verticalAxis.getLastValue();

		    	DataBounds dataBounds = new DataBounds(viewer.visibleDates, lowestValue, highestValue, clientArea, (int) viewer.datesAxis.gridSize);
				chartObject.setDataBounds(dataBounds);
				chartObject.paint(graphics);
			} catch(Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, "Error rendering chart");
				ChartsUIActivator.log(status);
			} finally {
				canvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.FALSE);
				graphics.dispose();
			}
		}

		viewer.paintImage(event, image);
	}

	private void onPaintVerticalScale(PaintEvent event) {
		viewer.revalidate();

		Rectangle clientArea = verticalScaleCanvas.getClientArea();
		boolean needsRedraw = Boolean.TRUE.equals(verticalScaleCanvas.getData(BaseChartViewer.K_NEEDS_REDRAW));

		if (verticalScaleImage != null && !verticalScaleImage.isDisposed()) {
			if (verticalScaleImage.getBounds().width != clientArea.width || verticalScaleImage.getBounds().height != clientArea.height)
				verticalScaleImage.dispose();
		}
		if (verticalScaleImage == null || verticalScaleImage.isDisposed()) {
			verticalScaleImage = new Image(verticalScaleCanvas.getDisplay(), clientArea.width, clientArea.height);
			needsRedraw = true;
		}

		if (needsRedraw) {
			buildVerticalAxis();

			verticalAxis.computeSize(clientArea.height);

	    	Graphics graphics = new Graphics(verticalScaleImage, viewer.getLocation(), viewer.datesAxis, verticalAxis);
			try {
		    	graphics.fillRectangle(clientArea);

				Object[] scaleArray = verticalAxis.getValues();
				for (int loop = 0; loop < scaleArray.length; loop++) {
					int y = verticalAxis.mapToAxis(scaleArray[loop]);
					String s = nf.format(scaleArray[loop]);
					int h = graphics.stringExtent(s).y / 2;
					graphics.drawLine(0, y, 4, y);
					graphics.drawString(s, 7, y - h);
				}
			} catch(Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, "Error rendering vertical scale", e);
				ChartsUIActivator.log(status);
			} finally {
				verticalScaleCanvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.FALSE);
				graphics.dispose();
			}
		}

		viewer.paintImage(event, verticalScaleImage);
	}

	public void setVerticalScaleVisible(boolean visible) {
		if (visible && !verticalScaleCanvas.getVisible()) {
			((GridData) verticalScaleCanvas.getLayoutData()).exclude = false;
			((GridData) canvas.getLayoutData()).horizontalSpan = 1;
			verticalScaleCanvas.setVisible(true);
		}
		if (!visible && verticalScaleCanvas.getVisible()) {
			((GridData) verticalScaleCanvas.getLayoutData()).exclude = true;
			((GridData) canvas.getLayoutData()).horizontalSpan = 2;
			verticalScaleCanvas.setVisible(false);
		}
	}

	public IChartObject getChartObject() {
    	return chartObject;
    }

	public void setChartObject(IChartObject chartObject) {
    	this.chartObject = chartObject;
		updateSummary();
    }

	public void redraw() {
		canvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.TRUE);
		verticalScaleCanvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.TRUE);

		canvas.redraw();
		verticalScaleCanvas.redraw();
	}

	public void hideToolTip() {
		label.setLocation(-200, 0);
	}

	public void showToolTip(int x, int y) {
		Number value = (Number) verticalAxis.mapToValue(y);
    	if (value != null) {
    		label.setText(nf.format(value));
    		label.pack();
    		label.setLocation(0, y - label.getSize().y / 2);
    	}
    }

	public Image getImage() {
    	return image;
    }

	public Image getVerticalScaleImage() {
    	return verticalScaleImage;
    }

	protected void updateSummary() {
		Control[] children = summary.getChildren();
		for (int i = 0; i < children.length; i++)
			children[i].dispose();

		if (chartObject != null) {
			chartObject.accept(new IChartObjectVisitor() {
		        public boolean visit(IChartObject object) {
		    		String s = object.getToolTip();
		        	if (s != null) {
		        		Label label = new Label(summary, SWT.NONE);
		        		label.setText(s);
		        	}
			        return true;
		        }
			});
		}

		summary.layout();
		summary.getParent().layout();
	}

	public TimeSpan getResolutionTimeSpan() {
    	return resolutionTimeSpan;
    }

	public void setResolutionTimeSpan(TimeSpan resolutionTimeSpan) {
    	this.resolutionTimeSpan = resolutionTimeSpan;
    }
}
