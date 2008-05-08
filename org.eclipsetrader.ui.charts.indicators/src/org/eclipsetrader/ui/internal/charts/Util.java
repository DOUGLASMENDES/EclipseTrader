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

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.feed.IOHLC;
import org.eclipsetrader.ui.charts.OHLCField;

/**
 * Generic utilities to handle TA-Lib and indicators.
 *
 * @since 1.0
 */
public class Util {

	private Util() {
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

		switch(field) {
			case Open: {
				for (int i = 0; i < values.length; i++) {
					IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
					if (ohlc != null)
						inReal[i] = ohlc.getOpen();
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
					if (ohlc != null)
						inReal[i] = ohlc.getHigh();
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
					if (ohlc != null)
						inReal[i] = ohlc.getLow();
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
					if (ohlc != null)
						inReal[i] = ohlc.getClose();
					else {
						Number number = (Number) values[i].getAdapter(Number.class);
						inReal[i] = number.doubleValue();
					}
				}
				break;
			}

			case Volume: {
				for (int i = 0; i < values.length; i++) {
					IOHLC ohlc = (IOHLC) values[i].getAdapter(IOHLC.class);
					if (ohlc != null)
						inReal[i] = ohlc.getVolume();
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
