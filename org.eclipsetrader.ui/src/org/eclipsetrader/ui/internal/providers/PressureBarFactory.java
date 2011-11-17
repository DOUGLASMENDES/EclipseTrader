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

package org.eclipsetrader.ui.internal.providers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;

public class PressureBarFactory extends AbstractProviderFactory {

    private static final int IMAGE_WIDTH = 128;
    private static final int IMAGE_HEIGHT = 16;
    private static final int IMAGE_HALF_WIDTH = IMAGE_WIDTH / 2;

    private Color bidColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    private Color bidFillColor = new Color(Display.getDefault(), blend(bidColor.getRGB(), new RGB(0, 0, 0), 75));
    private Color askColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private Color askFillColor = new Color(Display.getDefault(), blend(askColor.getRGB(), new RGB(0, 0, 0), 75));
    private Color backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    private class Data {

        IBook book;
        Image image;
        ImageDataValue value;
    }

    public class DataProvider implements IDataProvider {

        private MarketPricingEnvironment pricingEnvironment;

        private Map<ISecurity, Data> map = new HashMap<ISecurity, Data>();

        public DataProvider() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public void init(IAdaptable adaptable) {
            ISecurity security = (ISecurity) adaptable.getAdapter(ISecurity.class);
            if (!map.containsKey(security)) {
                pricingEnvironment = (MarketPricingEnvironment) adaptable.getAdapter(MarketPricingEnvironment.class);
                if (pricingEnvironment != null) {
                    pricingEnvironment.addLevel2Security(security);
                }
                Data data = new Data();
                data.image = new Image(Display.getDefault(), IMAGE_WIDTH, IMAGE_HEIGHT);
                map.put(security, data);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
            for (Data data : map.values()) {
                data.image.dispose();
            }
            map.clear();
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        @Override
        public IDataProviderFactory getFactory() {
            return PressureBarFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            ISecurity security = (ISecurity) adaptable.getAdapter(ISecurity.class);
            Data data = map.get(security);
            if (data == null) {
                return null;
            }

            IBook newBook = (IBook) adaptable.getAdapter(IBook.class);
            if (newBook != null && !newBook.equals(data.book)) {
                buildValue(newBook, data.image);
                data.book = newBook;
                data.value = new ImageDataValue(data.image.getImageData());
            }

            return data.value;
        }
    }

    public static class ImageDataValue implements IAdaptable {

        private final ImageData value;

        public ImageDataValue(ImageData value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @Override
        @SuppressWarnings({
            "unchecked", "rawtypes"
        })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(ImageData.class)) {
                return value;
            }
            return null;
        }
    }

    public PressureBarFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
     */
    @Override
    public IDataProvider createProvider() {
        return new DataProvider();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getType() {
        return new Class[] {
            Image.class,
        };
    }

    protected void buildValue(IBook book, Image image) {
        int level = 0;
        double currentPrice = 0.0;

        double bidPressure = 0L;
        for (IBookEntry entry : book.getBidProposals()) {
            if (currentPrice != entry.getPrice().doubleValue()) {
                currentPrice = entry.getPrice().doubleValue();
                level++;
                if (level > 5) {
                    break;
                }
            }
            bidPressure += entry.getQuantity();
        }

        level = 0;
        currentPrice = 0.0;

        double askPressure = 0L;
        for (IBookEntry entry : book.getAskProposals()) {
            if (currentPrice != entry.getPrice().doubleValue()) {
                currentPrice = entry.getPrice().doubleValue();
                level++;
                if (level > 5) {
                    break;
                }
            }
            askPressure += entry.getQuantity();
        }

        int bidPixels = (int) (bidPressure / (bidPressure + askPressure) * IMAGE_HALF_WIDTH);
        int askPixels = (int) (askPressure / (bidPressure + askPressure) * IMAGE_HALF_WIDTH);

        GC gc = new GC(image);
        try {
            gc.setBackground(backgroundColor);
            gc.fillRectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

            gc.setBackground(bidFillColor);
            gc.fillRectangle(IMAGE_HALF_WIDTH - bidPixels, 0, bidPixels, IMAGE_HEIGHT);
            gc.setBackground(askFillColor);
            gc.fillRectangle(IMAGE_HALF_WIDTH, 0, askPixels, IMAGE_HEIGHT);

            gc.setLineWidth(2);

            gc.setForeground(bidColor);
            gc.drawLine(IMAGE_HALF_WIDTH - bidPixels - 1, 1, IMAGE_HALF_WIDTH, 1);
            gc.drawLine(IMAGE_HALF_WIDTH - bidPixels - 1, 0, IMAGE_HALF_WIDTH - bidPixels, IMAGE_HEIGHT - 1);
            gc.drawLine(IMAGE_HALF_WIDTH - bidPixels - 1, IMAGE_HEIGHT - 1, IMAGE_HALF_WIDTH, IMAGE_HEIGHT - 1);

            gc.setForeground(askColor);
            gc.drawLine(IMAGE_HALF_WIDTH, 1, IMAGE_HALF_WIDTH + askPixels, 1);
            gc.drawLine(IMAGE_HALF_WIDTH + askPixels - 1, 0, IMAGE_HALF_WIDTH + askPixels - 1, IMAGE_HEIGHT - 1);
            gc.drawLine(IMAGE_HALF_WIDTH, IMAGE_HEIGHT - 1, IMAGE_HALF_WIDTH + askPixels, IMAGE_HEIGHT - 1);

        } finally {
            gc.dispose();
        }
    }

    private RGB blend(RGB c1, RGB c2, int ratio) {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    private int blend(int v1, int v2, int ratio) {
        return (ratio * v1 + (100 - ratio) * v2) / 100;
    }
}
