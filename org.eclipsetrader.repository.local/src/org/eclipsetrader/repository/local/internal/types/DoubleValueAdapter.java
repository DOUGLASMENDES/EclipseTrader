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

package org.eclipsetrader.repository.local.internal.types;

import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DoubleValueAdapter extends XmlAdapter<String, Double> {

    private NumberFormat formatter = NumberFormat.getInstance(Locale.US);

    public DoubleValueAdapter() {
        formatter.setGroupingUsed(false);
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(4);
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Double v) throws Exception {
        return v != null ? formatter.format(v) : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Double unmarshal(String v) throws Exception {
        return v != null ? formatter.parse(v).doubleValue() : null;
    }
}
