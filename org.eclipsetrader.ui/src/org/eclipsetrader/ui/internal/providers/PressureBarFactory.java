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

package org.eclipsetrader.ui.internal.providers;

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
	private Color bidColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private Color bidFillColor = new Color(Display.getDefault(), blend(bidColor.getRGB(), new RGB(0, 0, 0), 75));
	private Color askColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private Color askFillColor = new Color(Display.getDefault(), blend(askColor.getRGB(), new RGB(0, 0, 0), 75));
	private Color backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

	private Image image = new Image(Display.getDefault(), 128, 15);

	public class DataProvider implements IDataProvider {
		private ISecurity security;
		private MarketPricingEnvironment pricingEnvironment;
		private ImageValue value;
		private IBook book;

		public DataProvider() {
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        public void init(IAdaptable adaptable) {
       		security = (ISecurity) adaptable.getAdapter(ISecurity.class);

       		pricingEnvironment = (MarketPricingEnvironment) adaptable.getAdapter(MarketPricingEnvironment.class);
    		if (pricingEnvironment != null)
    			pricingEnvironment.addLevel2Security(security);
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        public void dispose() {
    		if (security != null) {
        		if (pricingEnvironment != null)
        			pricingEnvironment.removeLevel2Security(security);

        		if (value != null)
            		value.dispose();
    		}
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        public IDataProviderFactory getFactory() {
	        return PressureBarFactory.this;
        }

		/* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        public IAdaptable getValue(IAdaptable adaptable) {
        	IBook newBook = (IBook) adaptable.getAdapter(IBook.class);
        	if (newBook != null && (book == null || (book != null && !book.equals(newBook)))) {
        		if (value != null)
        			value.dispose();
        		value = buildValue(newBook);
        		book = newBook;
        	}
        	return value;
        }
	}

	class ImageValue implements IAdaptable {
		Image value;

		public ImageValue(Image value) {
	        this.value = value;
        }

		public void dispose() {
			value.dispose();
		}

		/* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
        	if (adapter.isAssignableFrom(Image.class) && !value.isDisposed())
        		return value;
	        return null;
        }

        @Override
        public boolean equals(Object obj) {
        	if (!(obj instanceof IAdaptable))
        		return false;
        	Image s = (Image) ((IAdaptable) obj).getAdapter(Image.class);
        	return s == value || (value != null && value.equals(s));
        }
	}

	public PressureBarFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
	 */
	public IDataProvider createProvider() {
		return new DataProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
	 */
    @SuppressWarnings("unchecked")
	public Class[] getType() {
	    return new Class[] {
	    		Image.class,
	    	};
	}

    protected ImageValue buildValue(IBook book) {
		int level = 0;
		double currentPrice = 0.0;

		double bidPressure = 0L;
		for (IBookEntry entry : book.getBidProposals()) {
			if (currentPrice != entry.getPrice().doubleValue()) {
				currentPrice = entry.getPrice().doubleValue();
				level++;
				if (level > 5)
					break;
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
				if (level > 5)
					break;
			}
			askPressure += entry.getQuantity();
		}

		int bidPixels = (int) (bidPressure / (bidPressure + askPressure) * 64);
		int askPixels = (int) (askPressure / (bidPressure + askPressure) * 64);

		GC gc = new GC(image);
		try {
			gc.setBackground(backgroundColor);
			gc.fillRectangle(0, 0, 128, 15);

			gc.setBackground(bidFillColor);
			gc.fillRectangle(64 - bidPixels, 0, bidPixels, 15);
			gc.setBackground(askFillColor);
			gc.fillRectangle(64, 0, askPixels, 15);

			gc.setLineWidth(2);

			gc.setForeground(bidColor);
			gc.drawLine(64 - bidPixels - 1, 1, 64, 1);
			gc.drawLine(64 - bidPixels - 1, 0, 64 - bidPixels, 15 - 1);
			gc.drawLine(64 - bidPixels - 1, 15 - 1, 64, 15 - 1);

			gc.setForeground(askColor);
			gc.drawLine(64, 1, 64 + askPixels, 1);
			gc.drawLine(64 + askPixels - 1, 0, 64 + askPixels - 1, 15 - 1);
			gc.drawLine(64, 15 - 1, 64 + askPixels, 15 - 1);

		} finally {
			gc.dispose();
		}

		ImageData imageData = image.getImageData();
		imageData.transparentPixel = imageData.palette.getPixel(backgroundColor.getRGB());
		return new ImageValue(new Image(image.getDevice(), imageData));
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
