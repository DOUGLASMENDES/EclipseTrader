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
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipsetrader.ui.internal.charts.ChartObjectHitVisitor;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class BaseChartViewer implements ISelectionProvider {
	public static final int VERTICAL_SCALE_WIDTH = 86;
	public static final int HORIZONTAL_SCALE_HEIGHT = 26;

	static final String K_NEEDS_REDRAW = "needs_redraw";

	private Composite composite;
	private SashForm sashForm;
	private DateScaleCanvas dateScaleCanvas;
	private Label verticalScaleLabel;

	private IChartObject[] input;
	private ChartCanvas[] chartCanvas = new ChartCanvas[0];

	DateValuesAxis datesAxis = new DateValuesAxis();

	private IChartObject selectedObject;
	private ListenerList selectionListeners = new ListenerList(ListenerList.IDENTITY);
	private boolean showTooltips;
	private CrosshairDecorator decorator = new CrosshairDecorator();

	public BaseChartViewer(Composite parent, int style) {
		composite = new Composite(parent, style | SWT.H_SCROLL);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 3;
		composite.setLayout(gridLayout);

		sashForm = new SashForm(composite, SWT.VERTICAL | SWT.NO_FOCUS);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		dateScaleCanvas = new DateScaleCanvas(this, composite);
		dateScaleCanvas.getControl().setVisible(false);
		((GridData) dateScaleCanvas.getControl().getLayoutData()).exclude = true;
		dateScaleCanvas.getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	redraw();
            }
		});

		verticalScaleLabel = new Label(composite, SWT.NONE);
		verticalScaleLabel.setLayoutData(new GridData(ChartItem.VERTICAL_SCALE_WIDTH, SWT.DEFAULT));
		verticalScaleLabel.setVisible(false);
		((GridData) verticalScaleLabel.getLayoutData()).exclude = true;

		Label label = new Label(sashForm, SWT.NONE);
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		composite.getHorizontalBar().setVisible(false);
		composite.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	redraw();
            }
		});

		composite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	redraw();
            }
		});

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

	Date firstDate;
	Date lastDate;
	Date[] visibleDates;

	protected Point getLocation() {
		return new Point(composite.getHorizontalBar().getSelection(), 0);
	}

	void revalidate() {
    	updateScrollbars();

		Rectangle clientArea = dateScaleCanvas.getCanvas().getClientArea();
		ScrollBar hScroll = composite.getHorizontalBar();

		if (visibleDates == null) {
			firstDate = (Date) datesAxis.mapToValue(hScroll.getSelection());
			lastDate = (Date) datesAxis.mapToValue(hScroll.getSelection() + clientArea.width);

			List<Date> l = new ArrayList<Date>();
			Object[] values = datesAxis.getValues();
			for (int i = 0; i < values.length; i++) {
	        	Date date = (Date) values[i];
	        	if ((firstDate == null || !date.before(firstDate)) && (lastDate == null || !date.after(lastDate)))
	        		l.add(date);
			}
			visibleDates = l.toArray(new Date[l.size()]);
		}
	}

	protected void paintImage(PaintEvent event, Image image) {
		if (image != null && !image.isDisposed()) {
			Rectangle bounds = image.getBounds();
			int width = event.width;
			if ((event.x + width) > bounds.width)
				width = bounds.width - event.x;
			int height = event.height;
			if ((event.y + height) > bounds.height)
				height = bounds.height - event.y;
			if (width != 0 && height != 0)
				event.gc.drawImage(image, event.x, event.y, width, height, event.x, event.y, width, height);
		}
	}

	protected void updateScrollbars() {
		Rectangle clientArea = dateScaleCanvas.getCanvas().getClientArea();
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
				selection = hiddenArea - 1;

			hScroll.setValues(selection, 0, hiddenArea + clientArea.width - 1, clientArea.width, 5, clientArea.width);
		}
	}

	public Object getInput() {
    	return input;
    }

	public void setInput(IChartObject input) {
		setInput(new IChartObject[] { input });
	}

	public void setInput(IChartObject[] input) {
    	this.input = input;
    	refresh();
	}

	public void refresh() {
    	ChartCanvas[] newCanvas = new ChartCanvas[input.length];

    	int length = Math.min(chartCanvas.length, newCanvas.length);
		System.arraycopy(chartCanvas, 0, newCanvas, 0, length);
		for (int i = length; i < chartCanvas.length; i++)
			chartCanvas[i].dispose();

		if (chartCanvas.length == 0) {
			Control[] c = sashForm.getChildren();
			if (c.length != 0)
				c[0].dispose();
		}

		chartCanvas = newCanvas;

    	datesAxis.clear();

    	for (int i = 0; i < input.length; i++) {
			if (chartCanvas[i] == null || chartCanvas[i].isDisposed()) {
				chartCanvas[i] = new ChartCanvas(this, sashForm);
				chartCanvas[i].getCanvas().setMenu(composite.getMenu());
				chartCanvas[i].getCanvas().addMouseMoveListener(new MouseMoveListener() {
                	private ChartObjectHitVisitor visitor = new ChartObjectHitVisitor();

                	public void mouseMove(MouseEvent e) {
		            	if (showTooltips && !decorator.isVisible()) {
		            		visitor.setLocation(e.x, e.y);
			            	ChartCanvas chartCanvas = (ChartCanvas) e.widget.getData();
			            	if (chartCanvas.getChartObject() != null)
		                    	chartCanvas.getChartObject().accept(visitor);

	                    	String s = null;
	                    	if (visitor.getChartObject() != null)
                        		s = visitor.getChartObject().getToolTip();

	                    	if ((s != null && !s.equals(chartCanvas.getCanvas().getToolTipText())) || (s == null && chartCanvas.getCanvas().getToolTipText() != null))
	                    		chartCanvas.getCanvas().setToolTipText(s);
		            	}
		            }
				});
				chartCanvas[i].getCanvas().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseDown(MouseEvent e) {
                    	ChartObjectHitVisitor visitor = new ChartObjectHitVisitor(e.x, e.y);
		            	ChartCanvas eventCanvas = (ChartCanvas) e.widget.getData();
		            	if (eventCanvas.getChartObject() != null)
	                    	eventCanvas.getChartObject().accept(visitor);
		            	if (selectedObject == null && selectedObject != visitor.getChartObject())
		            		decorator.deactivate();
		            	handleSelectionChanged(visitor.getChartObject());
                    }

                    @Override
                    public void mouseUp(MouseEvent e) {
                   		decorator.activate();
	                    super.mouseUp(e);
                    }
				});
				decorator.decorateCanvas(chartCanvas[i]);
			}

			chartCanvas[i].setChartObject(input[i]);
			chartCanvas[i].redraw();

			input[i].accept(new IChartObjectVisitor() {
                public boolean visit(IChartObject object) {
                	if (object.getDataSeries() != null)
                		datesAxis.addValues(object.getDataSeries().getValues());
    	            return true;
                }
        	});
        	//canvas.setChartObject(input[i]);
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

    	redraw();
    }

	protected void handleSelectionChanged(IChartObject newSelection) {
    	if (selectedObject != newSelection) {
    		ChartObjectFocusEvent event = new ChartObjectFocusEvent(selectedObject, newSelection);
    		if (selectedObject != null)
    			selectedObject.handleFocusLost(event);
    		selectedObject = newSelection;
    		if (selectedObject != null)
    			selectedObject.handleFocusGained(event);
    		fireSelectionChangedEvent(new SelectionChangedEvent(BaseChartViewer.this, getSelection()));
    		for (int i = 0; i < chartCanvas.length; i++)
    			chartCanvas[i].redraw();
    	}
	}

	public void redraw() {
		visibleDates = null;

    	for (int i = 0; i < chartCanvas.length; i++) {
    		if (chartCanvas[i] != null && !chartCanvas[i].isDisposed())
    			chartCanvas[i].redraw();
    	}

    	dateScaleCanvas.redraw();
	}

	RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

    private int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
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
    			for (int i = 0; i < input.length; i++) {
        			input[i].accept(new IChartObjectVisitor() {
                        public boolean visit(IChartObject object) {
                        	if (object == element)
                        		handleSelectionChanged(object);
    	                    return true;
                        }
        			});
    			}
    		}
    	}
    	if (selectedObject != null && newSelection.isEmpty())
    		handleSelectionChanged(null);
    }

    protected void fireSelectionChangedEvent(SelectionChangedEvent event) {
    	Object[] l = selectionListeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
    		try {
    			((ISelectionChangedListener) l[i]).selectionChanged(event);
    		} catch(Throwable e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, "Unexpected exception notifying selection listeners", e);
				ChartsUIActivator.log(status);
    		}
    	}
    }

	public void setShowTooltips(boolean showTooltips) {
		this.showTooltips = showTooltips;
    }
}
