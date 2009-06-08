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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.IDataSeriesVisitor;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class ChartViewer {
	private static final String K_NEEDS_REDRAW = "needs_redraw"; //$NON-NLS-1$

	private Composite composite;
	private SashForm sashForm;
	private ChartItem[] items = new ChartItem[0];
	private Canvas horizontalScaleCanvas;
	private Label verticalScaleLabel;

	private IAxis horizontalAxis;
	private IAxis verticalAxis;
	private IChartContentProvider contentProvider;
	private IChartRenderer renderer;

	private boolean verticalScaleVisible;
	private boolean horizontalScaleVisible;
	private Image horizontalScaleImage;
	private boolean needsRedraw;

	private Object input;

	public ChartViewer(Composite parent, int style) {
		composite = new Composite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 3;
		composite.setLayout(gridLayout);

		sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		horizontalScaleCanvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		horizontalScaleCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		horizontalScaleCanvas.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		((GridData) horizontalScaleCanvas.getLayoutData()).heightHint = ChartItem.HORIZONTAL_SCALE_HEIGHT;
		((GridData) horizontalScaleCanvas.getLayoutData()).exclude = true;
		horizontalScaleCanvas.setVisible(false);
		horizontalScaleCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	onPaintHorizontalScale(e);
            }
		});
		horizontalScaleCanvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (horizontalScaleImage != null) {
            		horizontalScaleImage.dispose();
            		horizontalScaleImage = null;
            	}
            }
		});

		verticalScaleLabel = new Label(composite, SWT.NONE);
		verticalScaleLabel.setLayoutData(new GridData(ChartItem.VERTICAL_SCALE_WIDTH, SWT.DEFAULT));
		((GridData) verticalScaleLabel.getLayoutData()).exclude = true;
		verticalScaleLabel.setVisible(false);

		Label label = new Label(sashForm, SWT.NONE);
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		composite.getHorizontalBar().setVisible(false);
		composite.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	needsRedraw = true;
            	for (int i = 0; i < items.length; i++) {
            		items[i].getCanvas().setData(K_NEEDS_REDRAW, Boolean.TRUE);
            		if (items[i].getVerticalScaleCanvas() != null)
            			items[i].getVerticalScaleCanvas().setData(K_NEEDS_REDRAW, Boolean.TRUE);
            		items[i].redraw();
            	}
            	if (horizontalScaleCanvas != null) {
            		horizontalScaleCanvas.setData(K_NEEDS_REDRAW, Boolean.TRUE);
            		horizontalScaleCanvas.redraw();
            	}
            }
		});

		composite.getVerticalBar().setVisible(false);
		composite.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	sashForm.redraw(0, 0, 0, 0, true);
            }
		});

		sashForm.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
            	if (contentProvider != null)
            		contentProvider.dispose();
            	if (renderer != null)
            		renderer.dispose();
            }
		});
	}

	public void dispose() {
		composite.dispose();
	}

	public IAxis getHorizontalAxis() {
    	return horizontalAxis;
    }

	public void setHorizontalAxis(IAxis horizontalAxis) {
    	this.horizontalAxis = horizontalAxis;
    }

	public boolean isHorizontalScaleVisible() {
    	return horizontalScaleVisible;
    }

	public void setHorizontalScaleVisible(boolean visible) {
		this.horizontalScaleVisible = visible;
	}

	public boolean isVerticalScaleVisible() {
    	return verticalScaleVisible;
    }

	public void setVerticalScaleVisible(boolean verticalScaleVisible) {
    	this.verticalScaleVisible = verticalScaleVisible;
    }

	public IAxis getVerticalAxis() {
    	return verticalAxis;
    }

	public void setVerticalAxis(IAxis verticalAxis) {
    	this.verticalAxis = verticalAxis;
    }

	public IChartContentProvider getContentProvider() {
    	return contentProvider;
    }

	public void setContentProvider(IChartContentProvider contentProvider) {
    	this.contentProvider = contentProvider;
    }

	public IChartRenderer getRenderer() {
    	return renderer;
    }

	public void setRenderer(IChartRenderer renderer) {
    	this.renderer = renderer;
    }

	public Object getInput() {
    	return input;
    }

	public void setInput(Object input) {
        Assert.isTrue(getContentProvider() != null, "ChartTemplate must have a content provider when input is set."); //$NON-NLS-1$

        Object oldInput = getInput();
        contentProvider.inputChanged(this, oldInput, input);
        this.input = input;

        refresh();
    }

	public void refresh() {
    	horizontalAxis.clear();
    	verticalAxis.clear();

    	Object[] elements = getInput() != null ? contentProvider.getElements(getInput()) : new Object[0];

    	ChartItem[] newItems = new ChartItem[elements.length];
		if (items != null) {
			int length = Math.min(items.length, newItems.length);
			System.arraycopy(items, 0, newItems, 0, length);
			for (int i = length; i < items.length; i++)
				items[i].dispose();

			if (items.length == 0) {
				Control[] c = sashForm.getChildren();
				if (c.length != 0)
					c[0].dispose();
			}
		}
		items = newItems;

		for (int i = 0; i < elements.length; i++) {
			if (items[i] == null || items[i].isDisposed()) {
				items[i] = new ChartItem(sashForm, SWT.NONE);
				items[i].setVerticalScaleVisible(verticalScaleVisible);
				items[i].getCanvas().addPaintListener(new PaintListener() {
	                public void paintControl(PaintEvent e) {
	                	onPaintItem(e);
	                }
				});
				items[i].getCanvas().addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {
	                    e.widget.setData(K_NEEDS_REDRAW, Boolean.TRUE);
                    }
				});
				items[i].getVerticalScaleCanvas().addPaintListener(new PaintListener() {
	                public void paintControl(PaintEvent e) {
	                	onPaintItemVerticalScale(e);
	                }
				});
				items[i].getVerticalScaleCanvas().addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {
	                    e.widget.setData(K_NEEDS_REDRAW, Boolean.TRUE);
                    }
				});
				if (i == 0) {
					items[i].getCanvas().addControlListener(new ControlAdapter() {
                        @Override
                        public void controlResized(ControlEvent e) {
    		            	updateScrollbars();
                        }
					});
				}
			}
			items[i].setData(elements[i]);
    		items[i].getCanvas().setData(K_NEEDS_REDRAW, Boolean.TRUE);
    		if (items[i].getVerticalScaleCanvas() != null)
    			items[i].getVerticalScaleCanvas().setData(K_NEEDS_REDRAW, Boolean.TRUE);
    		items[i].redraw();

			IDataSeries dataSeries = null;
			if (elements[i] instanceof IDataSeries)
				dataSeries = (IDataSeries) elements[i];
			else if (elements[i] instanceof IAdaptable)
				dataSeries = (IDataSeries) ((IAdaptable) elements[i]).getAdapter(IDataSeries.class);

			if (dataSeries != null) {
				dataSeries.accept(new IDataSeriesVisitor() {
                    public boolean visit(IDataSeries data) {
        				horizontalAxis.addValues(data.getValues());
	                    return true;
                    }
				});
				verticalAxis.addValues(new IAdaptable[] { dataSeries.getHighest(), dataSeries.getLowest() });
			}
		}

		if (items.length == 0) {
			Label label = new Label(sashForm, SWT.NONE);
			label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		}

    	needsRedraw = true;
    	if (horizontalScaleCanvas != null) {
    		horizontalScaleCanvas.setData(K_NEEDS_REDRAW, Boolean.TRUE);
    		horizontalScaleCanvas.redraw();
    	}

    	updateScrollbars();

    	int[] weights = new int[items.length];
    	if (weights.length != 0) {
    		weights[0] = 100;
    		for (int i = 1; i < weights.length; i++)
        		weights[i] = 25;
    		sashForm.setWeights(weights);
    	}
    	sashForm.layout();

    	if (items.length != 0 && verticalScaleVisible && !verticalScaleLabel.getVisible()) {
    		((GridData) verticalScaleLabel.getLayoutData()).exclude = false;
    		verticalScaleLabel.setVisible(true);
    	}
		if ((items.length == 0 || !verticalScaleVisible) && verticalScaleLabel.getVisible()) {
			((GridData) verticalScaleLabel.getLayoutData()).exclude = true;
			verticalScaleLabel.setVisible(false);
		}

    	if (items.length != 0 && horizontalScaleVisible && !horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = false;
			horizontalScaleCanvas.setVisible(true);
			composite.layout();
		}
		if ((items.length == 0 || !horizontalScaleVisible) && horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = true;
			horizontalScaleCanvas.setVisible(false);
			composite.layout();
		}
	}

	public Display getDisplay() {
		return sashForm.getDisplay();
	}

	public boolean isDisposed() {
		return sashForm.isDisposed();
	}

	private void onPaintHorizontalScale(PaintEvent event) {
		if (!(renderer instanceof IScaleRenderer))
			return;

		Rectangle clientArea = horizontalScaleCanvas.getClientArea();
		ScrollBar hScroll = composite.getHorizontalBar();
		Object firstElement = horizontalAxis.mapToValue(hScroll != null ? hScroll.getSelection() : 0);
		Object lastElement = horizontalAxis.mapToValue((hScroll != null ? hScroll.getSelection() : 0) + clientArea.width);

		if (getInput() != null && needsRedraw) {
    		Object[] elements = contentProvider.getElements(getInput());
    		if (elements != null && elements.length != 0) {
    			RenderTarget graphics = new RenderTarget();
    			try {
    				Rectangle canvasArea = horizontalScaleCanvas.getClientArea();
    	        	if (horizontalScaleImage == null)
    	        		horizontalScaleImage = new Image(horizontalScaleCanvas.getDisplay(), canvasArea.width, canvasArea.height);

    	        	graphics.gc = new GC(horizontalScaleImage);
    				graphics.display = horizontalScaleCanvas.getDisplay();
    				graphics.x = hScroll != null ? -hScroll.getSelection() : 0;
    				graphics.y = 0;
    				graphics.width = canvasArea.width;
    				graphics.height = canvasArea.height;
    				graphics.widget = horizontalScaleCanvas;
    				graphics.horizontalAxis = horizontalAxis;
    				graphics.verticalAxis = verticalAxis;

    				graphics.firstValue = new AdaptableWrapper(firstElement);
    				graphics.lastValue = new AdaptableWrapper(lastElement);
    				graphics.input = input;

    				graphics.gc.setAntialias(SWT.OFF);
    				graphics.gc.setForeground(horizontalScaleCanvas.getForeground());
    				graphics.gc.setBackground(horizontalScaleCanvas.getBackground());

   					((IScaleRenderer) renderer).renderHorizontalScale(graphics);
    			} catch(Error e) {
    				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartViewer_HorizontalScaleRenderingError);
    				ChartsUIActivator.log(status);
    			} finally {
    				if (graphics.gc != null)
    					graphics.gc.dispose();
    			}
    		}
		}

		if (horizontalScaleImage != null && !horizontalScaleImage.isDisposed())
			event.gc.drawImage(horizontalScaleImage, 0, 0);
		else {
			event.gc.setForeground(horizontalScaleCanvas.getForeground());
			event.gc.setBackground(horizontalScaleCanvas.getBackground());
			event.gc.fillRectangle(event.x, event.y, event.width, event.height);
		}
	}

	protected void updateScrollbars() {
		if (items != null && items.length != 0) {
			Point chartSize = new Point(0, 0);
			Rectangle clientArea = items[0].getCanvas().getClientArea();
			ScrollBar hScroll = composite.getHorizontalBar();
			boolean wasVisible = hScroll.getVisible();

			for (int i = 0; i < 2; i++) {
				if (horizontalAxis != null) {
					chartSize.x = horizontalAxis.computeSize(clientArea.width);
					if (hScroll != null) {
						if (chartSize.x > clientArea.width)
							hScroll.setVisible(true);
						else {
			                hScroll.setVisible(false);
			                hScroll.setValues(0, 0, 1, 1, 1, 1);
						}
					}
				}
				clientArea = items[0].getCanvas().getClientArea();
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
	}

	private void onPaintItem(PaintEvent event) {
    	ChartItem item = (ChartItem) event.widget.getData();
    	Canvas canvas = item.getCanvas();
    	Image image = item.getImage();
    	boolean needsRedraw = Boolean.TRUE.equals(canvas.getData(K_NEEDS_REDRAW));

		Rectangle clientArea = canvas.getClientArea();
		ScrollBar hScroll = composite.getHorizontalBar();

		if (image != null && !image.isDisposed()) {
			if (image.getBounds().width != clientArea.width || image.getBounds().height != clientArea.height)
				image.dispose();
		}

		if (image == null || image.isDisposed()) {
			image = new Image(canvas.getDisplay(), clientArea.width, clientArea.height);
			item.setImage(image);
			needsRedraw = true;
		}

		if (needsRedraw && image != null && !image.isDisposed()) {
			RenderTarget target = new RenderTarget();
			try {
				Object firstElement = horizontalAxis.mapToValue(hScroll.getSelection());
				Object lastElement = horizontalAxis.mapToValue(hScroll.getSelection() + clientArea.width);

				target.gc = new GC(image);
				target.display = event.display;
				target.x = -hScroll.getSelection();
				target.y = 0;
				target.width = clientArea.width;
				target.height = clientArea.height;
				target.widget = event.widget;
				target.horizontalAxis = horizontalAxis;
				target.verticalAxis = verticalAxis;

				target.input = item.getData();
				target.firstValue = firstElement instanceof IAdaptable ? firstElement : new AdaptableWrapper(firstElement);
				target.lastValue = lastElement instanceof IAdaptable ? lastElement : new AdaptableWrapper(lastElement);

				//target.gc.setAntialias(SWT.ON);
				target.gc.setForeground(canvas.getForeground());
				target.gc.setBackground(canvas.getBackground());

				IChartRenderer renderer = getRenderer();
				renderer.renderBackground(target);

				Object[] elements = getContentProvider().getChildren(item.getData());
				if (elements != null) {
					for (int i = 0; i < elements.length; i++) {
						target.gc.setForeground(canvas.getForeground());
						target.gc.setBackground(canvas.getBackground());
						target.gc.setLineWidth(1);
						renderer.renderElement(target, elements[i]);
					}
				}
			} catch(Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartViewer_RenderingErrorMessage);
				ChartsUIActivator.log(status);
			} finally {
				if (target.gc != null)
					target.gc.dispose();
			}
			canvas.setData(K_NEEDS_REDRAW, Boolean.FALSE);
		}

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

	private void onPaintItemVerticalScale(PaintEvent event) {
    	ChartItem item = (ChartItem) event.widget.getData();

    	Canvas canvas = item.getVerticalScaleCanvas();
    	Image image = item.getVerticalScaleImage();
    	boolean needsRedraw = Boolean.TRUE.equals(canvas.getData(K_NEEDS_REDRAW));

		Rectangle clientArea = canvas.getClientArea();
		ScrollBar hScroll = composite.getHorizontalBar();

		if (image != null && !image.isDisposed()) {
			if (image.getBounds().width != clientArea.width || image.getBounds().height != clientArea.height)
				image.dispose();
		}

		if (image == null || image.isDisposed()) {
			image = new Image(canvas.getDisplay(), clientArea.width, clientArea.height);
			item.setVerticalScaleImage(image);
			needsRedraw = true;
		}

		if (needsRedraw && image != null && !image.isDisposed()) {
			RenderTarget target = new RenderTarget();
			try {
				Object firstElement = horizontalAxis.mapToValue(hScroll.getSelection());
				Object lastElement = horizontalAxis.mapToValue(hScroll.getSelection() + item.getCanvas().getClientArea().width);

				target.gc = new GC(image);
				target.display = event.display;
				target.x = -hScroll.getSelection();
				target.y = 0;
				target.width = clientArea.width;
				target.height = clientArea.height;
				target.widget = event.widget;
				target.horizontalAxis = horizontalAxis;
				target.verticalAxis = verticalAxis;

				target.input = item.getData();
				target.firstValue = firstElement instanceof IAdaptable ? firstElement : new AdaptableWrapper(firstElement);
				target.lastValue = lastElement instanceof IAdaptable ? lastElement : new AdaptableWrapper(lastElement);

				target.gc.setAntialias(SWT.OFF);
				target.gc.setForeground(canvas.getForeground());
				target.gc.setBackground(canvas.getBackground());

				IScaleRenderer renderer = (IScaleRenderer) getRenderer();
				renderer.renderVerticalScale(target);
			} catch(Error e) {
				Status status = new Status(IStatus.ERROR, ChartsUIActivator.PLUGIN_ID, Messages.ChartViewer_RenderingErrorMessage);
				ChartsUIActivator.log(status);
			} finally {
				if (target.gc != null)
					target.gc.dispose();
			}
			canvas.setData(K_NEEDS_REDRAW, Boolean.FALSE);
		}

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

	public Control getControl() {
		return composite;
	}
}
