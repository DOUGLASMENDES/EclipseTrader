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

package org.eclipsetrader.ui.internal.charts.views;

import junit.framework.TestCase;

import org.eclipsetrader.core.feed.Book;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IBookEntry;

public class CurrentBookTest extends TestCase {

    IBookEntry[] bid;
    IBookEntry[] ask;
    IBook book;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        bid = new IBookEntry[] {
                new BookEntry(null, 20.57, 3500L, 1L, null),
                new BookEntry(null, 20.56, 7800L, 1L, null),
                new BookEntry(null, 20.55, 1200L, 1L, null),
                new BookEntry(null, 20.54, 2200L, 1L, null),
                new BookEntry(null, 20.53, 5500L, 1L, null),
        };
        ask = new IBookEntry[] {
                new BookEntry(null, 20.58, 500L, 1L, null),
                new BookEntry(null, 20.59, 7500L, 1L, null),
                new BookEntry(null, 20.60, 2500L, 1L, null),
                new BookEntry(null, 20.61, 200L, 1L, null),
                new BookEntry(null, 20.62, 500L, 1L, null),
        };
        book = new Book(bid, ask);
    }

    public void testCalculateBiggestQuantity() throws Exception {
        CurrentBook o = new CurrentBook();
        o.setBook(book);
        o.calculateBiggestQuantity();
        assertEquals(7800L, o.biggestQuantity);
    }

    public void testGetBarWidth() throws Exception {
        CurrentBook o = new CurrentBook();
        o.setBook(book);
        o.calculateBiggestQuantity();
        assertEquals(44, o.getBarWidth(bid[0]));
    }
}
