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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipsetrader.core.feed.ISplit;

public class SplitAdapter extends XmlAdapter<SplitType, ISplit> {

    public SplitAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public SplitType marshal(ISplit v) throws Exception {
        return v != null ? new SplitType(v) : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public ISplit unmarshal(SplitType v) throws Exception {
        return v != null ? v.getSplit() : null;
    }
}
