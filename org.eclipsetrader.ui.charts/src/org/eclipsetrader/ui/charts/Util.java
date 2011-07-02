/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.charts;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipsetrader.core.feed.IOHLC;

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
            if (event.x + width > bounds.width) {
                width = bounds.width - event.x;
            }
            int height = event.height;
            if (event.y + height > bounds.height) {
                height = bounds.height - event.y;
            }
            if (width != 0 && height != 0) {
                event.gc.drawImage(image, event.x, event.y, width, height, event.x, event.y, width, height);
            }
        }
    }

    /**
     * Returns an array of values representing the field passed as argument.
     * <p>If the adaptables can't adapt to <code>IOHLC</code> objects the default <code>Numeric</code>
     * value is read.</p>
     *
     * @param values the adaptable values to read.
     * @param field the field to return.
     * @return the array of values.
     */
    public static double[] getValuesForField(IAdaptable[] values, OHLCField field) {
        double[] inReal = new double[values.length];

        switch (field) {
            case Open: {
                for (int i = 0; i < values.length; i++) {
                    IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                    if (ohlc != null) {
                        inReal[i] = ohlc.getOpen();
                    }
                    else {
                        Number number = (Number) values[i].getAdapter(Number.class);
                        inReal[i] = number.doubleValue();
                    }
                }
                break;
            }

            case High: {
                for (int i = 0; i < values.length; i++) {
                    IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                    if (ohlc != null) {
                        inReal[i] = ohlc.getHigh();
                    }
                    else {
                        Number number = (Number) values[i].getAdapter(Number.class);
                        inReal[i] = number.doubleValue();
                    }
                }
                break;
            }

            case Low: {
                for (int i = 0; i < values.length; i++) {
                    IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                    if (ohlc != null) {
                        inReal[i] = ohlc.getLow();
                    }
                    else {
                        Number number = (Number) values[i].getAdapter(Number.class);
                        inReal[i] = number.doubleValue();
                    }
                }
                break;
            }

            case Close: {
                for (int i = 0; i < values.length; i++) {
                    IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
                    if (ohlc != null) {
                        inReal[i] = ohlc.getClose();
                    }
                    else {
                        Number number = (Number) values[i].getAdapter(Number.class);
                        inReal[i] = number.doubleValue();
                    }
                }
                break;
            }
        }

        return inReal;
    }
}
