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

package org.eclipsetrader.core.internal.charts.repository;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.charts.repository.IIndicatorSection;

public class IndicatorSectionAdapter extends XmlAdapter<IndicatorSection, IIndicatorSection> {

	public IndicatorSectionAdapter() {
	}

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public IndicatorSection marshal(IIndicatorSection v) throws Exception {
	    return v != null ? new IndicatorSection(v) : null;
    }

	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IIndicatorSection unmarshal(IndicatorSection v) throws Exception {
	    return v;
    }
}
