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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipsetrader.core.feed.TimeSpan;
import org.eclipsetrader.ui.internal.charts.ChartObjectHitVisitor;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class BaseChartViewer implements ISelectionProvider {
	static final String K_NEEDS_REDRAW = "needs_redraw"; //$NON-NLS-1$

	private Composite composite;
	private SashForm sashForm;
	private DateScaleCanvas dateScaleCanvas;
	private Label verticalScaleLabel;

	private IChartObject[][] input;
	private ChartCanvas[] chartCanvas = new ChartCanvas[0];

	DateValuesAxis datesAxis;

	private ChartCanvas selectedChartCanvas;
	private IChartObject selectedObject;
	private ListenerList selectionListeners = new ListenerList(ListenerList.IDENTITY);
	private boolean showTooltips;
	private boolean showScaleTooltips;

	private SummaryBarDecorator summaryDecorator;
	private CrosshairDecorator decorator;
	private int decoratorMode;

	private ChartToolEditor activeEditor = new ChartToolEditor();

	public BaseChartViewer(Composite parent, int style) {
		composite = new Composite(parent, style | SWT.H_SCROLL);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 3;
		composite.setLayout(gridLayout);

		GC gc = new GC(composite);
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		sashForm = new SashForm(composite, SWT.VERTICAL | SWT.NO_FOCUS);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		datesAxis = new DateValuesAxis();
		datesAxis.additionalSpace = 15;

		dateScaleCanvas = new DateScaleCanvas(composite);
		dateScaleCanvas.getControl().setVisible(false);
		((GridData) dateScaleCanvas.getControl().getLayoutData()).exclude = true;
		dateScaleCanvas.getControl().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateScrollbars();
				revalidate();
				redraw();
			}
		});

		verticalScaleLabel = new Label(composite, SWT.NONE);
		verticalScaleLabel.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 12), SWT.DEFAULT));
		verticalScaleLabel.setVisible(false);
		((GridData) verticalScaleLabel.getLayoutData()).exclude = true;

		Label label = new Label(sashForm, SWT.NONE);
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		composite.getHorizontalBar().setVisible(false);
		composite.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				revalidate();
				redraw();
			}
		});

		composite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				redraw();
			}
		});

		summaryDecorator = new SummaryBarDecorator();

		decorator = new CrosshairDecorator();
		decorator.createSummaryLabel(sashForm);
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

	public ChartCanvas[] getChildren() {
		return chartCanvas;
	}

	public void setHorizontalScaleVisible(boolean visible) {
		/*if (visible && !horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = false;
			horizontalScaleCanvas.setVisible(true);
			composite.layout();
		}
		if (!visible && horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = true;
			horizontalScaleCanvas.setVisible(false);
			composite.layout();
		}*/
	}

	public void setVerticalScaleVisible(boolean visible) {
		/*if (visible && !canvas.getVerticalScaleCanvas().getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).horizontalSpan = 1;
			canvas.setVerticalScaleVisible(visible);
			composite.layout();
		}
		if (!visible && canvas.getVerticalScaleCanvas().getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).horizontalSpan = 2;
			canvas.setVerticalScaleVisible(visible);
			composite.layout();
		}*/
	}

	public Display getDisplay() {
		return composite.getDisplay();
	}

	protected Point getLocation() {
		return new Point(composite.getHorizontalBar().getSelection(), 0);
	}

	void revalidate() {
		Rectangle clientArea = dateScaleCanvas.getCanvas().getClientArea();
		ScrollBar hScroll = composite.getHorizontalBar();

		Date firstDate = (Date) datesAxis.mapToValue(hScroll.getSelection());
		Date lastDate = (Date) datesAxis.mapToValue(hScroll.getSelection() + clientArea.width);

		List<Date> l = new ArrayList<Date>();
		Object[] values = datesAxis.getValues();
		for (int i = 0; i < values.length; i++) {
			Date date = (Date) values[i];
			if ((firstDate == null || !date.before(firstDate)) && (lastDate == null || !date.after(lastDate)))
				l.add(date);
		}
		Date[] visibleDates = l.toArray(new Date[l.size()]);

		dateScaleCanvas.setDatesAxis(datesAxis);
		dateScaleCanvas.setVisibleDates(visibleDates);
		dateScaleCanvas.setLocation(getLocation());

		for (int i = 0; i < chartCanvas.length; i++) {
			if (chartCanvas[i] != null && !chartCanvas[i].isDisposed()) {
				chartCanvas[i].setDatesAxis(datesAxis);
				chartCanvas[i].setVisibleDates(visibleDates);
				chartCanvas[i].setDateRange(firstDate, lastDate);
				chartCanvas[i].setLocation(getLocation());
			}
		}
	}

	public void print(Printer printer) {
		GC gc = new GC(printer);
		try {
			Rectangle printerBounds = printer.getClientArea();
			Rectangle trimBounds = printer.computeTrim(printerBounds.x, printerBounds.y, printerBounds.width, printerBounds.height);

			printerBounds.x -= trimBounds.x;
			printerBounds.y -= trimBounds.y;
			printerBounds.width -= printerBounds.x;
			printerBounds.height -= printerBounds.y;
			System.out.println(printerBounds);

			System.out.println(printerBounds);
			double ratio = (double) printerBounds.width / (double) printerBounds.height;
			int y = printerBounds.y;
			for (int i = 0; i < chartCanvas.length; i++) {
				Image image = chartCanvas[i].getImage();
				if (image != null) {
					Rectangle imageBounds = image.getBounds();
					int destHeight = (int) (imageBounds.height * ratio);
					gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, printerBounds.x, y, printerBounds.width, destHeight);
					y += destHeight;
					gc.drawLine(printerBounds.x, y, printerBounds.x + printerBounds.width, y);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updateScrollbars() {
		Rectangle clientArea = dateScaleCanvas.getCanvas().getClientArea();
		if (clientArea.width == 0)
			return;

		ScrollBar hScroll = composite.getHorizontalBar();
		boolean wasVisible = hScroll.getVisible();

		Point chartSize = new Point(datesAxis.computeSize(clientArea.width), clientArea.height);

		for (int i = 0; i < 2; i++) {
			if (hScroll != null) {
				if (chartSize.x > clientArea.width)
					hScroll.setVisible(true);
				else {
					hScroll.setVisible(false);
					hScroll.setValues(0, 0, 1, 1, 1, 1);
				}
			}
			clientArea = dateScaleCanvas.getCanvas().getClientArea();
			chartSize = new Point(datesAxis.computeSize(clientArea.width), clientArea.height);
		}

		if (hScroll.getVisible()) {
			int hiddenArea = chartSize.x - clientArea.width + 1;
			int currentSelection = hScroll.getSelection();
			int rightAnchor = hScroll.getMaximum() - hScroll.getThumb();

			int selection = Math.min(currentSelection, hiddenArea - 1);
			if (!wasVisible || currentSelection == rightAnchor)
				selection = hiddenArea;

			hScroll.setValues(selection, 0, chartSize.x, clientArea.width, 5, clientArea.width);
		}
	}

	public Object getInput() {
		return input;
	}

	public void setInput(IChartObject[][] input) {
		this.input = input;
		refresh();
	}

	public void refresh() {
		ChartCanvas[] newCanvas = new ChartCanvas[input.length];

		int length = Math.min(chartCanvas.length, newCanvas.length);
		System.arraycopy(chartCanvas, 0, newCanvas, 0, length);
		for (int i = length; i < chartCanvas.length; i++) {
			chartCanvas[i].getCanvas().setMenu(null);
			chartCanvas[i].dispose();
		}

		if (chartCanvas.length == 0) {
			Control[] c = sashForm.getChildren();
			if (c.length != 0)
				c[0].dispose();
		}

		chartCanvas = newCanvas;
		selectedChartCanvas = null;

		datesAxis.clear();
		for (int i = 0; i < input.length; i++) {
			for (int c = 0; c < input[i].length; c++) {
				input[i][c].accept(new IChartObjectVisitor() {
					public boolean visit(IChartObject object) {
						if (object.getDataSeries() != null)
							datesAxis.addValues(object.getDataSeries().getValues());
						return true;
					}
				});
			}
		}

		for (int i = 0; i < input.length; i++) {
			if (chartCanvas[i] == null || chartCanvas[i].isDisposed()) {
				chartCanvas[i] = new ChartCanvas(sashForm);
				chartCanvas[i].getCanvas().setMenu(composite.getMenu());
				chartCanvas[i].getCanvas().addMouseTrackListener(new MouseTrackAdapter() {
					@Override
					public void mouseExit(MouseEvent e) {
						ChartCanvas chartCanvas = (ChartCanvas) e.widget.getData();
						if (activeEditor.isActive())
							return;
						chartCanvas.hideToolTip();
						dateScaleCanvas.hideToolTip();
					}
				});
				chartCanvas[i].getCanvas().addMouseMoveListener(new MouseMoveListener() {
					private ChartObjectHitVisitor visitor = new ChartObjectHitVisitor();

					public void mouseMove(MouseEvent e) {
						ChartCanvas chartCanvas = (ChartCanvas) e.widget.getData();

						if (showTooltips && !decorator.isVisible()) {
							String s = null;
							if (visitor.getChartObject() != null)
								s = visitor.getChartObject().getToolTip();

							if ((s != null && !s.equals(chartCanvas.getCanvas().getToolTipText())) || (s == null && chartCanvas.getCanvas().getToolTipText() != null))
								chartCanvas.getCanvas().setToolTipText(s);
						}

						if (showScaleTooltips) {
							chartCanvas.showToolTip(e.x, e.y);

							int x = e.x + composite.getHorizontalBar().getSelection();
							Date value = (Date) datesAxis.mapToValue(x);
							if (value != null)
								dateScaleCanvas.showToolTip(e.x, e.y, value);
						}

						if (activeEditor != null && !activeEditor.isActive()) {
							visitor.setLocation(e.x, e.y);
							chartCanvas.accept(visitor);

							if (visitor.getChartObject() instanceof IEditableChartObject) {
								IEditableChartObject editableObject = (IEditableChartObject) visitor.getChartObject();
								if (editableObject.isOnEditHandle(e.x, e.y)) {
									Cursor cursor = Display.getCurrent().getSystemCursor(SWT.CURSOR_CROSS);
									if (chartCanvas.getCanvas().getCursor() != cursor)
										chartCanvas.getCanvas().setCursor(cursor);
								}
								else if (editableObject.isOnDragHandle(e.x, e.y)) {
									Cursor cursor = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
									if (chartCanvas.getCanvas().getCursor() != cursor)
										chartCanvas.getCanvas().setCursor(cursor);
								}
							}
							else {
								if (visitor.getChartObject() != null && chartCanvas.getCanvas().getCursor() == null)
									chartCanvas.getCanvas().setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
								else if (visitor.getChartObject() == null && chartCanvas.getCanvas().getCursor() != null)
									chartCanvas.getCanvas().setCursor(null);
							}
						}
					}
				});
				chartCanvas[i].getCanvas().addMouseListener(new MouseAdapter() {
					private ChartObjectHitVisitor visitor = new ChartObjectHitVisitor();

					@Override
					public void mouseDown(MouseEvent e) {
						ChartCanvas eventCanvas = (ChartCanvas) e.widget.getData();

						visitor.setLocation(e.x, e.y);
						eventCanvas.accept(visitor);

						if (selectedObject != visitor.getChartObject()) {
							if (decorator.getMode() != CrosshairDecorator.MODE_MOUSE_HOVER)
								decorator.deactivate();

						}
						else {
							if (selectedObject == null)
								decorator.activate();
						}

						handleSelectionChanged(eventCanvas, visitor.getChartObject());

						if (visitor.getChartObject() instanceof IEditableChartObject && e.button == 1) {
							eventCanvas.getCanvas().update();
							decorator.setMode(CrosshairDecorator.MODE_OFF);

							IEditableChartObject object = (IEditableChartObject) visitor.getChartObject();
							activeEditor.activate(BaseChartViewer.this, eventCanvas, object);
							activeEditor.handleMouseDown(activeEditor.createEvent(e));
						}
					}
				});
				summaryDecorator.decorateCanvas(chartCanvas[i]);
				decorator.decorateCanvas(chartCanvas[i]);
			}

			if (selectedChartCanvas == null)
				selectedChartCanvas = chartCanvas[i];

			chartCanvas[i].setResolutionTimeSpan(getResolutionTimeSpan());
			chartCanvas[i].setChartObject(input[i]);
			chartCanvas[i].redraw();
		}

		int[] weights = new int[chartCanvas.length];
		if (weights.length != 0) {
			weights[0] = 100;
			for (int i = 1; i < weights.length; i++)
				weights[i] = 25;
			sashForm.setWeights(weights);
		}
		sashForm.layout();

		if (chartCanvas.length != 0) {
			dateScaleCanvas.getControl().setVisible(true);
			((GridData) dateScaleCanvas.getControl().getLayoutData()).exclude = false;
			verticalScaleLabel.setVisible(true);
			((GridData) verticalScaleLabel.getLayoutData()).exclude = false;
		}
		else {
			dateScaleCanvas.getControl().setVisible(false);
			((GridData) dateScaleCanvas.getControl().getLayoutData()).exclude = true;
			verticalScaleLabel.setVisible(false);
			((GridData) verticalScaleLabel.getLayoutData()).exclude = true;
		}
		composite.layout();

		final Map<String, Object> set = new HashMap<String, Object>();
		for (int i = 0; i < chartCanvas.length; i++) {
			chartCanvas[i].accept(new IChartObjectVisitor() {
				public boolean visit(IChartObject object) {
					if (object == selectedObject)
						set.put("selectedObject", object); //$NON-NLS-1$
					return true;
				}
			});
		}
		handleSelectionChanged(selectedChartCanvas, (IChartObject) set.get("selectedObject")); //$NON-NLS-1$

		updateScrollbars();
		revalidate();

		redraw();
	}

	protected void handleSelectionChanged(ChartCanvas newChartCanvas, IChartObject newSelection) {
		if (selectedObject != newSelection) {
			ChartObjectFocusEvent event = new ChartObjectFocusEvent(selectedObject, newSelection);
			if (selectedObject != null)
				selectedObject.handleFocusLost(event);

			selectedObject = newSelection;
			selectedChartCanvas = newChartCanvas;

			if (selectedObject != null)
				selectedObject.handleFocusGained(event);

			fireSelectionChangedEvent(new SelectionChangedEvent(BaseChartViewer.this, getSelection()));
			for (int i = 0; i < chartCanvas.length; i++)
				chartCanvas[i].redraw();
		}
	}

	public void redraw() {
		for (int i = 0; i < chartCanvas.length; i++) {
			if (chartCanvas[i] != null && !chartCanvas[i].isDisposed())
				chartCanvas[i].redraw();
		}

		dateScaleCanvas.redraw();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return selectedObject != null ? new StructuredSelection(selectedObject) : StructuredSelection.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection newSelection) {
		if (!newSelection.isEmpty() && newSelection instanceof IStructuredSelection) {
			final Object element = ((IStructuredSelection) newSelection).getFirstElement();
			if (element instanceof IChartObject) {
				for (int i = 0; i < chartCanvas.length; i++) {
					final ChartCanvas currentCanvas = chartCanvas[i];;
					currentCanvas.accept(new IChartObjectVisitor() {
						public boolean visit(IChartObject object) {
							if (object == element)
								handleSelectionChanged(currentCanvas, object);
							return true;
						}
					});
				}
			}
		}
		if (selectedObject != null && newSelection.isEmpty())
			handleSelectionChanged(selectedChartCanvas, null);
	}

	protected void fireSelectionChangedEvent(SelectionChangedEvent event) {
		Object[] l = selectionListeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			try {
				((ISelectionChangedListener) l[i]).selectionChanged(event);
			} catch (Throwable e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.BaseChartViewer_ExceptionErrorMessage, e);
				ChartsUIActivator.log(status);
			}
		}
	}

	public void setShowTooltips(boolean showTooltips) {
		this.showTooltips = showTooltips;
	}

	public void setCrosshairMode(int mode) {
		this.decoratorMode = mode;
		decorator.setMode(mode);
	}

	public void setShowScaleTooltips(boolean showScaleTooltips) {
		this.showScaleTooltips = showScaleTooltips;
	}

	public void activateEditor(IEditableChartObject object) {
		if (activeEditor.isActive())
			activeEditor.cancelEditing();

		if (selectedObject != null) {
			handleSelectionChanged(selectedChartCanvas, null);
			selectedChartCanvas.getCanvas().update();
		}

		selectedChartCanvas.getCanvas().setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_CROSS));

		decorator.setMode(CrosshairDecorator.MODE_OFF);
		activeEditor.activate(this, selectedChartCanvas, object);
	}

	public void deactivateEditor() {
		if (activeEditor != null) {
			selectedChartCanvas.getCanvas().setCursor(null);
			decorator.setMode(decoratorMode);
		}
	}

	public ChartToolEditor getEditor() {
		return activeEditor;
	}

	public void setEditor(ChartToolEditor activeEditor) {
		if (this.activeEditor != null)
			this.activeEditor.cancelEditing();

		this.activeEditor = activeEditor;
	}

	public ChartCanvas getSelectedChartCanvas() {
		return selectedChartCanvas;
	}

	public int getSelectedChartCanvasIndex() {
		for (int i = 0; i < chartCanvas.length; i++) {
			if (selectedChartCanvas == chartCanvas[i])
				return i;
		}
		return -1;
	}

	/**
	 * Returns the canvas at the given point in the receiver or null if
	 * no such canvas exists. The point is in the coordinate system of
	 * the receiver.
	 *
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @return the canvas at the given point, or null if no such canvas exists.
	 */
	public ChartCanvas getChartCanvas(int x, int y) {
		Point p = getControl().toDisplay(x, y);

		for (int i = 0; i < chartCanvas.length; i++) {
			Rectangle bounds = chartCanvas[i].getCanvas().getBounds();
			if (bounds.contains(chartCanvas[i].getCanvas().toControl(p)))
				return chartCanvas[i];
		}

		return null;
	}

	public void setDecoratorSummaryTooltips(boolean show) {
		decorator.setShowSummaryTooltip(show);
	}

	public int getZoomFactor() {
		return datesAxis.getZoomFactor();
	}

	public void setZoomFactor(int zoomFactor) {
		datesAxis.setZoomFactor(zoomFactor);

		updateScrollbars();
		revalidate();

		redraw();
	}

	public TimeSpan getResolutionTimeSpan() {
		return dateScaleCanvas.getResolutionTimeSpan();
	}

	public void setResolutionTimeSpan(TimeSpan resolutionTimeSpan) {
		dateScaleCanvas.setResolutionTimeSpan(resolutionTimeSpan);
	}

	public int[] getWeights() {
		return sashForm.getWeights();
	}

	public void setWeights(int[] weights) {
		sashForm.setWeights(weights);
		sashForm.layout();
	}
}
