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

package org.eclipsetrader.core.internal.charts.repository;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.charts.repository.IElementSection;

public class ElementSectionAdapter extends XmlAdapter<ElementSection, IElementSection> {

    public ElementSectionAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public ElementSection marshal(IElementSection v) throws Exception {
        return v != null ? new ElementSection(v) : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IElementSection unmarshal(ElementSection v) throws Exception {
        return v;
    }
}
