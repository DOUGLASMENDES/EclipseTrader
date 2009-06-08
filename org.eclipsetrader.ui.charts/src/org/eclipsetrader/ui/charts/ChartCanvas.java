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
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartCanvas {
	private Composite composite;
	private SummaryBar summary;
	private Canvas canvas;
	private Canvas verticalScaleCanvas;

	private Image image;
	private Image verticalScaleImage;

	private Label label;

	private IChartObject[] chartObject;
	private TimeSpan resolutionTimeSpan;
	private DoubleValuesAxis verticalAxis;
	private NumberFormat nf = NumberFormat.getInstance();

	Date[] visibleDates;
	Date firstDate;
	Date lastDate;
	Point location;
	DateValuesAxis datesAxis;

	private Observer observer = new Observer() {
		public void update(Observable o, Object arg) {
			canvas.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!canvas.isDisposed())
						redraw();
				}
			});
		}
	};

	public ChartCanvas(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GC gc = new GC(composite);
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		summary = new SummaryBar(composite, SWT.NONE);
		summary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

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
				removeObservers();
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
		((GridData) verticalScaleCanvas.getLayoutData()).widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 12);
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

	public void setDatesAxis(DateValuesAxis datesAxis) {
		this.datesAxis = datesAxis;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public void setDateRange(Date firstDate, Date lastDate) {
		this.firstDate = firstDate;
		this.lastDate = lastDate;
	}

	public void setVisibleDates(Date[] visibleDates) {
		this.visibleDates = visibleDates;
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
			accept(new IChartObjectVisitor() {
				public boolean visit(IChartObject object) {
					if (object.getDataSeries() != null) {
						IDataSeries series = object.getDataSeries().getSeries(new AdaptableWrapper(firstDate), new AdaptableWrapper(lastDate));
						if (series != null)
							verticalAxis.addValues(new Object[] {
							    series.getLowest(), series.getHighest()
							});
					}
					return true;
				}
			});
		}
	}

	private void onPaint(PaintEvent event) {
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

			Graphics graphics = new Graphics(image, location, datesAxis, verticalAxis);
			try {
				graphics.fillRectangle(clientArea);
				if (visibleDates != null)
					paintBackground(graphics, clientArea);
				paintObjects(graphics, clientArea);
			} catch (Throwable e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartCanvas_RenderingChartError);
				ChartsUIActivator.log(status);
			} finally {
				canvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.FALSE);
				graphics.dispose();
			}
		}

		Util.paintImage(event, image);
	}

	void paintBackground(Graphics graphics, Rectangle clientArea) {
		graphics.pushState();
		try {
			graphics.setLineDash(new int[] {
			    3, 3
			});
			graphics.setForegroundColor(Util.blend(graphics.getForegroundColor(), graphics.getBackgroundColor(), 15));

			Calendar oldDate = null;
			Calendar currentDate = Calendar.getInstance();

			for (int i = 0; i < visibleDates.length; i++) {
				Date date = visibleDates[i];
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
		} finally {
			graphics.popState();
		}
	}

	void paintObjects(Graphics graphics, Rectangle clientArea) {
		Double lowestValue = (Double) verticalAxis.getFirstValue();
		Double highestValue = (Double) verticalAxis.getLastValue();

		DataBounds dataBounds = new DataBounds(visibleDates, lowestValue, highestValue, clientArea, (int) datesAxis.gridSize);
		for (int i = 0; i < chartObject.length; i++) {
			graphics.pushState();
			try {
				chartObject[i].setDataBounds(dataBounds);
				chartObject[i].paint(graphics);
			} finally {
				graphics.popState();
			}
		}
	}

	private void onPaintVerticalScale(PaintEvent event) {
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

			Graphics graphics = new Graphics(verticalScaleImage, location, datesAxis, verticalAxis);
			try {
				graphics.fillRectangle(clientArea);

				Object[] scaleArray = verticalAxis.getValues();
				for (int loop = 0; loop < scaleArray.length; loop++) {
					int y = verticalAxis.mapToAxis(scaleArray[loop]);

					String s;
					if (((Double) scaleArray[loop]).doubleValue() > 1000000) {
						Double value = (Double) scaleArray[loop];
						s = nf.format(value / 1000000.0) + "M"; //$NON-NLS-1$
					}
					else
						s = nf.format(scaleArray[loop]);

					int h = graphics.stringExtent(s).y / 2;
					graphics.drawLine(0, y, 4, y);
					graphics.drawString(s, 7, y - h);
				}

				for (int i = 0; i < chartObject.length; i++) {
					graphics.pushState();
					try {
						chartObject[i].paintScale(graphics);
					} finally {
						graphics.popState();
					}
				}
			} catch (Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartCanvas_VerticalScaleRenderingError, e);
				ChartsUIActivator.log(status);
			} finally {
				verticalScaleCanvas.setData(BaseChartViewer.K_NEEDS_REDRAW, Boolean.FALSE);
				graphics.dispose();
			}
		}

		Util.paintImage(event, verticalScaleImage);
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

	public IChartObject[] getChartObject() {
		return chartObject;
	}

	public void setChartObject(IChartObject[] chartObject) {
		removeObservers();

		this.chartObject = chartObject;

		addObservers();
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
		if (verticalAxis == null)
			return;
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
		summary.removeAll();

		accept(new IChartObjectVisitor() {
			public boolean visit(IChartObject object) {
				ISummaryBarDecorator factory = null;
				if (object instanceof IAdaptable) {
					factory = (ISummaryBarDecorator) ((IAdaptable) object).getAdapter(ISummaryBarDecorator.class);
					if (factory != null)
						factory.createDecorator(summary.getCompositeControl());
				}
				return true;
			}
		});

		summary.layout();
		summary.getParent().layout();
	}

	public TimeSpan getResolutionTimeSpan() {
		return resolutionTimeSpan;
	}

	public void setResolutionTimeSpan(TimeSpan resolutionTimeSpan) {
		this.resolutionTimeSpan = resolutionTimeSpan;
	}

	public void accept(IChartObjectVisitor visitor) {
		if (chartObject == null)
			return;

		for (int i = 0; i < chartObject.length; i++)
			chartObject[i].accept(visitor);
	}

	void addObservers() {
		accept(new IChartObjectVisitor() {
			public boolean visit(IChartObject object) {
				if (!(object instanceof IAdaptable))
					return true;

				IAdaptable adaptable = (IAdaptable) object;
				Observable observable = (Observable) adaptable.getAdapter(Observable.class);
				if (observable != null)
					observable.addObserver(observer);

				return true;
			}
		});
	}

	void removeObservers() {
		accept(new IChartObjectVisitor() {
			public boolean visit(IChartObject object) {
				if (!(object instanceof IAdaptable))
					return true;

				IAdaptable adaptable = (IAdaptable) object;
				Observable observable = (Observable) adaptable.getAdapter(Observable.class);
				if (observable != null)
					observable.deleteObserver(observer);

				return true;
			}
		});
	}
}
