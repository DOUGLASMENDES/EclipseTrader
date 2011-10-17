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

package org.eclipsetrader.core.ats.engines;

import java.util.Collection;

import org.eclipsetrader.core.feed.IBar;

public class Util {

    public static final int TYPE_SMA = 0;
    public static final int TYPE_EMA = 1;

    public static final int FIELD_OPEN = 0;
    public static final int FIELD_HIGH = 1;
    public static final int FIELD_LOW = 2;
    public static final int FIELD_CLOSE = 3;
    public static final int FIELD_VOLUME = 4;

    private Util() {
    }

    public static com.tictactec.ta.lib.MAType getTALib_MAType(int type) {
        com.tictactec.ta.lib.MAType maType = com.tictactec.ta.lib.MAType.Sma;
        switch (type) {
            case TYPE_SMA:
                maType = com.tictactec.ta.lib.MAType.Sma;
                break;
            case TYPE_EMA:
                maType = com.tictactec.ta.lib.MAType.Ema;
                break;
        }
        return maType;
    }

    public static void copyValuesTo(Collection<IBar> bars, int field, double[] dest) {
        int i = 0;
        switch (field) {
            case FIELD_OPEN:
                for (IBar bar : bars) {
                    dest[i++] = bar.getOpen();
                }
                break;

            case FIELD_HIGH:
                for (IBar bar : bars) {
                    dest[i++] = bar.getHigh();
                }
                break;

            case FIELD_LOW:
                for (IBar bar : bars) {
                    dest[i++] = bar.getLow();
                }
                break;

            case FIELD_CLOSE:
                for (IBar bar : bars) {
                    dest[i++] = bar.getClose();
                }
                break;

            case FIELD_VOLUME:
                for (IBar bar : bars) {
                    dest[i++] = bar.getVolume();
                }
                break;
        }
    }
}
