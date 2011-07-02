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

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.repository.local.internal.IdentifiersCollection;

public class FeedIdentifierAdapterTest extends TestCase {

    private IdentifiersCollection identifiers;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        identifiers = new IdentifiersCollection();
    }

    public void testMarshal() throws Exception {
        FeedIdentifier feedIdentifier = new FeedIdentifier("MSFT", null);
        FeedIdentifierAdapter adapter = new FeedIdentifierAdapter();
        assertEquals("MSFT", adapter.marshal(feedIdentifier));
    }

    public void testMarshalAddsIdentifierToCollection() throws Exception {
        assertEquals(0, identifiers.getList().size());
        FeedIdentifierAdapter adapter = new FeedIdentifierAdapter();
        adapter.marshal(new FeedIdentifier("MSFT", null));
        assertEquals(1, identifiers.getList().size());
    }

    public void testUnmarshal() throws Exception {
        FeedIdentifierAdapter adapter = new FeedIdentifierAdapter();
        IFeedIdentifier feedIdentifier = adapter.unmarshal("MSFT");
        assertEquals("MSFT", feedIdentifier.getSymbol());
    }

    public void testMarshalNull() throws Exception {
        FeedIdentifierAdapter adapter = new FeedIdentifierAdapter();
        assertNull(adapter.marshal(null));
    }

    public void testUnmarshalNull() throws Exception {
        FeedIdentifierAdapter adapter = new FeedIdentifierAdapter();
        assertNull(adapter.unmarshal(null));
    }
}
