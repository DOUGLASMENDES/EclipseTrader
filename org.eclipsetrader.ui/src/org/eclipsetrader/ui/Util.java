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

package org.eclipsetrader.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Util {

    /**
     * Returns a date format object that is locale-specific but composed of all
     * digits and 4-digits year.
     *
     * @return the date format instance
     */
    public static DateFormat getDateFormat() {
        return new SimpleDateFormat(getDateFormatPattern());
    }

    /**
     * Returns a date format pattern that is locale-specific but composed of all
     * digits and 4-digits year.
     *
     * @return the date format pattern
     */
    public static String getDateFormatPattern() {
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        if (format instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) format).toPattern();
            if (pattern.indexOf("yyyy") == -1) {
                pattern = pattern.replaceAll("yy", "yyyy"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pattern.indexOf("d") != -1 && pattern.indexOf("dd") == -1) {
                pattern = pattern.replaceAll("d", "dd"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pattern.indexOf("MMM") != -1) {
                pattern = pattern.replaceAll("MMM", "MM"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if (pattern.indexOf("M") != -1 && pattern.indexOf("MM") == -1) {
                pattern = pattern.replaceAll("M", "MM"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return pattern;
        }
        return null;
    }

    /**
     * Returns a simple hour:minutes time format object.
     *
     * @return the time format object
     */
    public static DateFormat getTimeFormat() {
        return new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
    }
}
