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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ChartItem {
	public static final int VERTICAL_SCALE_WIDTH = 86;
	public static final int HORIZONTAL_SCALE_HEIGHT = 26;

	private Composite composite;
	private Canvas canvas;
	private Canvas verticalScaleCanvas;
	private Canvas horizontalScaleCanvas;

	private Image image;
	private Image verticalScaleImage;
	private Image horizontalScaleImage;

	private Map<String, Object> dataMap = new HashMap<String, Object>();

	public ChartItem(Composite parent, int style) {
		composite = new Composite(parent, style);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 3;
		composite.setLayout(gridLayout);

		canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		canvas.setData(this);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		verticalScaleCanvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		verticalScaleCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		verticalScaleCanvas.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
		((GridData) verticalScaleCanvas.getLayoutData()).widthHint = VERTICAL_SCALE_WIDTH;
		((GridData) verticalScaleCanvas.getLayoutData()).exclude = true;
		verticalScaleCanvas.setVisible(false);
		verticalScaleCanvas.setData(this);
		verticalScaleCanvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (verticalScaleImage != null) {
            		verticalScaleImage.dispose();
            		verticalScaleImage = null;
            	}
            	verticalScaleCanvas.redraw();
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

		horizontalScaleCanvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		horizontalScaleCanvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		horizontalScaleCanvas.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		((GridData) horizontalScaleCanvas.getLayoutData()).heightHint = HORIZONTAL_SCALE_HEIGHT;
		((GridData) horizontalScaleCanvas.getLayoutData()).exclude = true;
		horizontalScaleCanvas.setVisible(false);
		horizontalScaleCanvas.setData(this);
		horizontalScaleCanvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
            	if (horizontalScaleImage != null) {
            		horizontalScaleImage.dispose();
            		horizontalScaleImage = null;
            	}
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

		canvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
            	if (image != null) {
            		image.dispose();
            		image = null;
            	}
            }
		});
	}

	public void dispose() {
		canvas.dispose();
	}

	public void setHorizontalScaleVisible(boolean visible) {
		if (visible && !horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = false;
			horizontalScaleCanvas.setVisible(true);
			composite.layout();
		}
		if (!visible && horizontalScaleCanvas.getVisible()) {
			((GridData) horizontalScaleCanvas.getLayoutData()).exclude = true;
			horizontalScaleCanvas.setVisible(false);
			composite.layout();
		}
	}

	public void setVerticalScaleVisible(boolean visible) {
		if (visible && !verticalScaleCanvas.getVisible()) {
			((GridData) verticalScaleCanvas.getLayoutData()).exclude = false;
			verticalScaleCanvas.setVisible(true);
			composite.layout();
		}
		if (!visible && verticalScaleCanvas.getVisible()) {
			((GridData) verticalScaleCanvas.getLayoutData()).exclude = true;
			verticalScaleCanvas.setVisible(false);
			composite.layout();
		}
	}

	public Display getDisplay() {
		return canvas.getDisplay();
	}

	public boolean isDisposed() {
		return canvas.isDisposed();
	}

	public Control getControl() {
		return composite;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public Image getImage() {
    	return image;
    }

	public void setImage(Image image) {
    	this.image = image;
    }

	public Canvas getVerticalScaleCanvas() {
		return verticalScaleCanvas;
	}

	public Image getVerticalScaleImage() {
    	return verticalScaleImage;
    }

	public void setVerticalScaleImage(Image verticalScaleImage) {
    	this.verticalScaleImage = verticalScaleImage;
    }

	public Canvas getHorizontalScaleCanvas() {
		return horizontalScaleCanvas;
	}

	public Image getHorizontalScaleImage() {
    	return horizontalScaleImage;
    }

	public void setHorizontalScaleImage(Image horizontalScaleImage) {
    	this.horizontalScaleImage = horizontalScaleImage;
    }

	public Object getData() {
		return dataMap.get(null);
	}

	public void setData(Object data) {
		dataMap.put(null, data);
	}

	public Object getData(String key) {
		return dataMap.get(key);
	}

	public void setData(String key, Object data) {
		dataMap.put(key, data);
	}

	public void redraw() {
		canvas.redraw();
		if (horizontalScaleCanvas != null)
			horizontalScaleCanvas.redraw();
		if (verticalScaleCanvas != null)
			verticalScaleCanvas.redraw();
	}

	public void layout() {
		composite.layout();
	}
}
