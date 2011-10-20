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

package org.eclipsetrader.core.feed;

import org.eclipse.osgi.util.NLS;

public class TimeSpan {

    public enum Units {
        Minutes("min", "Minutes"), Days("d", "Days"), Months("mn", "Monts"), Years("yr", "Years");

        private final String name;
        private final String key;

        private Units(String key, String name) {
            this.name = name;
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    private Units units;
    private int length;

    public static TimeSpan minutes(int length) {
        return new TimeSpan(Units.Minutes, length);
    }

    public static TimeSpan days(int length) {
        return new TimeSpan(Units.Days, length);
    }

    public static TimeSpan months(int length) {
        return new TimeSpan(Units.Months, length);
    }

    public static TimeSpan years(int length) {
        return new TimeSpan(Units.Years, length);
    }

    public static TimeSpan fromString(String s) {
        if (s == null) {
            return null;
        }
        Units[] u = Units.values();
        for (int i = 0; i < u.length; i++) {
            if (s.endsWith(u[i].getKey())) {
                int length = Integer.parseInt(s.substring(0, s.length() - u[i].getKey().length()));
                return new TimeSpan(u[i], length);
            }
        }
        try {
            int length = Integer.parseInt(s);
            return new TimeSpan(Units.Minutes, length);
        } catch (Exception e) {
            // Do nothing
        }
        return null;
    }

    public TimeSpan(Units units, int length) {
        this.units = units;
        this.length = length;
    }

    public Units getUnits() {
        return units;
    }

    public int getLength() {
        return length;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return length * 11 + units.hashCode() * 7;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeSpan)) {
            return false;
        }
        return ((TimeSpan) obj).getUnits() == getUnits() && ((TimeSpan) obj).getLength() == getLength();
    }

    public boolean lowerThan(TimeSpan timeSpan) {
        if (units.ordinal() < timeSpan.units.ordinal()) {
            return true;
        }
        else if (units.ordinal() == timeSpan.units.ordinal()) {
            if (length < timeSpan.length) {
                return true;
            }
        }
        return false;
    }

    public boolean higherThan(TimeSpan timeSpan) {
        if (units.ordinal() > timeSpan.units.ordinal()) {
            return true;
        }
        else if (units.ordinal() == timeSpan.units.ordinal()) {
            if (length > timeSpan.length) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return NLS.bind("{0} {1}", new Object[] {
            String.valueOf(length), units.toString()
        });
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return NLS.bind("{0}{1}", new Object[] {
            String.valueOf(length), units.getKey()
        });
    }
}
