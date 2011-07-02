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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class BookTest extends TestCase {

    public void testGetBidProposals() throws Exception {
        IBookEntry[] bid = new IBookEntry[0];
        Book book = new Book(bid, null);
        assertSame(bid, book.getBidProposals());
    }

    public void testGetAskProposals() throws Exception {
        IBookEntry[] ask = new IBookEntry[0];
        Book book = new Book(null, ask);
        assertSame(ask, book.getAskProposals());
    }

    public void testEqualsWithSameEntries() throws Exception {
        IBookEntry[] bid = new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        };
        IBookEntry[] ask = new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        };
        Book book = new Book(bid, ask);

        assertTrue(book.equals(new Book(bid, ask)));
    }

    public void testEqualsWithNewInstanceEntries() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        assertTrue(newBook.equals(book));
    }

    public void testNotEqualsBid() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(new IBookEntry[] {
            new BookEntry(null, 1.41, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        assertFalse(newBook.equals(book));
    }

    public void testNotEqualsAsk() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.61, 100L, null, null),
        });

        assertFalse(newBook.equals(book));
    }

    public void testNotEqualsWithDifferentEntries() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(new IBookEntry[] {
                new BookEntry(null, 1.4, 100L, null, null),
                new BookEntry(null, 1.41, 200L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        assertFalse(newBook.equals(book));
    }

    public void testNotEqualsWithNullBid() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(null, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        assertFalse(newBook.equals(book));
    }

    public void testNotEqualsWithNullAsk() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        Book newBook = new Book(new IBookEntry[] {
                new BookEntry(null, 1.4, 100L, null, null),
                new BookEntry(null, 1.41, 200L, null, null),
        }, null);

        assertFalse(newBook.equals(book));
    }

    public void testEqualsWithOtherObjects() throws Exception {
        Book book = new Book(null, null);
        assertFalse(book.equals(new Object()));
    }

    public void testSerializable() throws Exception {
        Book book = new Book(new IBookEntry[] {
            new BookEntry(null, 1.4, 100L, null, null),
        }, new IBookEntry[] {
            new BookEntry(null, 1.6, 100L, null, null),
        });

        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(book);
        os.close();
    }
}
