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

package org.eclipsetrader.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

public class BoxItem extends Item {
	private Canvas canvas;
	private Composite row1;
	private Composite row2;
	private Label[] columns = new Label[5];
	private Color background = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	private Color foreground = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	private Font boldFont;

	private Listener eventListener = new Listener() {
        public void handleEvent(Event event) {
        	Point p = canvas.getParent().toControl(((Control) event.widget).toDisplay(event.x, event.y));
        	event.x = p.x;
        	event.y = p.y;
			event.item = BoxItem.this;
			canvas.getParent().notifyListeners(event.type, event);
        }
	};

	private PaintListener paintListener = new PaintListener() {
        public void paintControl(PaintEvent e) {
    		e.gc.setForeground(foreground);
    		Rectangle rect = canvas.getClientArea();
    		e.gc.drawRectangle(0, 0, rect.width - 1, rect.height - 1);
        }
	};

	private DisposeListener disposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
        	if (boldFont != null)
        		boldFont.dispose();
        }
	};

	public BoxItem(Composite parent, int style) {
		super(parent, style);

		FontData[] fontData = parent.getFont().getFontData();
		for (int i = 0; i < fontData.length; i++)
			fontData[i].setStyle(fontData[i].getStyle() | SWT.BOLD);
		boldFont = new Font(parent.getDisplay(), fontData);

		canvas = new Canvas(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 3;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 2;
		canvas.setLayout(gridLayout);
		canvas.setBackground(background);
		canvas.addPaintListener(paintListener);
		canvas.addDisposeListener(disposeListener);
		canvas.setBackgroundMode(SWT.INHERIT_NONE);

		row1 = new Composite(canvas, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		row1.setLayout(gridLayout);
		row1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		row1.setBackground(background);

		columns[0] = new Label(row1, SWT.NONE);
		columns[0].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		columns[0].setForeground(foreground);
		columns[0].setBackground(background);

		columns[1] = new Label(row1, SWT.NONE);
		columns[1].setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		columns[1].setForeground(foreground);
		columns[1].setBackground(background);

		row2 = new Composite(canvas, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		row2.setLayout(gridLayout);
		row2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		row2.setBackground(background);

		columns[2] = new Label(row2, SWT.NONE);
		columns[2].setFont(boldFont);
		columns[2].setForeground(foreground);
		columns[2].setBackground(background);

		Composite composite = new Composite(row2, SWT.NONE);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setBackground(background);

		columns[3] = new Label(composite, SWT.NONE);
		columns[3].setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		columns[3].setForeground(foreground);
		columns[3].setBackground(background);

		columns[4] = new Label(composite, SWT.NONE);
		columns[4].setBackground(background);

		canvas.addListener(SWT.MouseDown, eventListener);
		canvas.addListener(SWT.MouseUp, eventListener);
		row1.addListener(SWT.MouseDown, eventListener);
		row1.addListener(SWT.MouseUp, eventListener);
		row2.addListener(SWT.MouseDown, eventListener);
		row2.addListener(SWT.MouseUp, eventListener);
		for (int i = 0; i < columns.length; i++) {
			columns[i].addListener(SWT.MouseDown, eventListener);
			columns[i].addListener(SWT.MouseUp, eventListener);
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
    	if (canvas != null)
    		canvas.dispose();
    	if (boldFont != null)
    		boldFont.dispose();
	    super.dispose();
    }

	public Canvas getCanvas() {
    	return canvas;
    }

	public void setText(int index, String string) {
		columns[index].setText(string.replaceAll("[&]", "&&"));
	}

	public String getText(int index) {
		return columns[index].getText();
	}

    public Color getBackground(int index) {
		return columns[index].getBackground();
    }

    public void setBackground(int index, Color color) {
		columns[index].setBackground(color);
    }

    public Color getForeground(int index) {
		return columns[index].getForeground();
    }

    public void setForeground(int index, Color color) {
		columns[index].setForeground(color);
    }

    public Rectangle getBounds() {
    	return canvas.getBounds();
    }

    public Rectangle getBounds(int index) {
    	return columns[index].getBounds();
    }

    public Font getFont(int index) {
		return columns[index].getFont();
    }

    public void setFont(int index, Font font) {
		columns[index].setFont(font);
    }

    public Image getImage(int index) {
		return columns[index].getImage();
    }

    public void setImage(int index, Image image) {
		columns[index].setImage(image);
    }

    public Composite getParent() {
    	return canvas.getParent();
    }

	/* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#toString()
     */
    @Override
    public String toString() {
	    return "[BoxItem] " + columns[0].getText();
    }

	public void setMenu(Menu menu) {
		canvas.setMenu(menu);
		row1.setMenu(menu);
		row2.setMenu(menu);
		for (int i = 0; i < columns.length; i++)
			columns[i].setMenu(menu);
    }

	public Menu getMenu() {
		return canvas.getMenu();
	}
}
