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

package org.eclipsetrader.core.internal.markets;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class WeekdaysAdapter extends XmlAdapter<String, Set<Integer>> {

    public WeekdaysAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Set<Integer> v) throws Exception {
        if (v == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        result.append(v.contains(Calendar.SUNDAY) ? 'S' : '-');
        result.append(v.contains(Calendar.MONDAY) ? 'M' : '-');
        result.append(v.contains(Calendar.TUESDAY) ? 'T' : '-');
        result.append(v.contains(Calendar.WEDNESDAY) ? 'W' : '-');
        result.append(v.contains(Calendar.THURSDAY) ? 'T' : '-');
        result.append(v.contains(Calendar.FRIDAY) ? 'F' : '-');
        result.append(v.contains(Calendar.SATURDAY) ? 'S' : '-');
        return result.toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Set<Integer> unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }
        Set<Integer> result = new HashSet<Integer>();
        if (v.charAt(0) != '-') {
            result.add(Calendar.SUNDAY);
        }
        if (v.charAt(1) != '-') {
            result.add(Calendar.MONDAY);
        }
        if (v.charAt(2) != '-') {
            result.add(Calendar.TUESDAY);
        }
        if (v.charAt(3) != '-') {
            result.add(Calendar.WEDNESDAY);
        }
        if (v.charAt(4) != '-') {
            result.add(Calendar.THURSDAY);
        }
        if (v.charAt(5) != '-') {
            result.add(Calendar.FRIDAY);
        }
        if (v.charAt(6) != '-') {
            result.add(Calendar.SATURDAY);
        }
        return result;
    }
}
