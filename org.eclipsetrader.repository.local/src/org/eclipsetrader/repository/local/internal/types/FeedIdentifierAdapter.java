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

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.repository.local.internal.IdentifiersCollection;

public class FeedIdentifierAdapter extends XmlAdapter<String, IFeedIdentifier> {

    public FeedIdentifierAdapter() {
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IFeedIdentifier v) throws Exception {
        if (v == null) {
            return null;
        }
        IdentifiersCollection.getInstance().putFeedIdentifier(v);
        return v.getSymbol();
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IFeedIdentifier unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }
        return IdentifiersCollection.getInstance().getFeedIdentifierFromSymbol(v);
    }
}
