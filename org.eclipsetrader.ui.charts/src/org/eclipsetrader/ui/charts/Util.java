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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class Util {

	public Util() {
	}

	public static RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

    private static int blend(int v1, int v2, int ratio) {
		return (ratio * v1 + (100 - ratio) * v2) / 100;
	}

	public static void paintImage(PaintEvent event, Image image) {
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
}
