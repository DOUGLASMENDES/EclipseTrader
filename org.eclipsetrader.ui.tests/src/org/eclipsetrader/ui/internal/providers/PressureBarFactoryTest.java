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

package org.eclipsetrader.ui.internal.providers;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipsetrader.core.feed.Book;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.views.IDataProvider;

public class PressureBarFactoryTest extends TestCase {

    private Security security;
    private IBook book;
    private IAdaptable sourceAdaptable;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        security = new Security("Test", null);
        sourceAdaptable = new IAdaptable() {

            @Override
            @SuppressWarnings({
                "unchecked", "rawtypes"
            })
            public Object getAdapter(Class adapter) {
                if (adapter.isAssignableFrom(Security.class)) {
                    return security;
                }
                if (adapter.isAssignableFrom(IBook.class)) {
                    return book;
                }
                return null;
            }
        };
    }

    public void testGetValueWithNullBook() throws Exception {
        IDataProvider provider = new PressureBarFactory().createProvider();

        IAdaptable value = provider.getValue(sourceAdaptable);

        assertNull(value);
    }

    public void testGetBookPressure() throws Exception {
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });

        IDataProvider provider = new PressureBarFactory().createProvider();
        provider.init(sourceAdaptable);

        IAdaptable value = provider.getValue(sourceAdaptable);

        assertNotNull(value);
        assertNotNull(value.getAdapter(ImageData.class));
    }

    public void testGetBidOnlyBookPressure() throws Exception {
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[0]);

        IDataProvider provider = new PressureBarFactory().createProvider();
        provider.init(sourceAdaptable);

        IAdaptable value = provider.getValue(sourceAdaptable);

        assertNotNull(value);
        assertNotNull(value.getAdapter(ImageData.class));
    }

    public void testGetAskOnlyBookPressure() throws Exception {
        book = new Book(new BookEntry[0], new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });

        IDataProvider provider = new PressureBarFactory().createProvider();
        provider.init(sourceAdaptable);

        IAdaptable value = provider.getValue(sourceAdaptable);

        assertNotNull(value);
        assertNotNull(value.getAdapter(ImageData.class));
    }

    public void testGetSameValueInstance() throws Exception {
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });

        IDataProvider provider = new PressureBarFactory().createProvider();
        provider.init(sourceAdaptable);

        IAdaptable value = provider.getValue(sourceAdaptable);

        assertSame(value, provider.getValue(sourceAdaptable));
    }

    public void testGetNewValueInstanceWithBookUpdate() throws Exception {
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });

        IDataProvider provider = new PressureBarFactory().createProvider();
        provider.init(sourceAdaptable);

        IAdaptable value1 = provider.getValue(sourceAdaptable);

        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 75L, null, null),
        });

        IAdaptable value2 = provider.getValue(sourceAdaptable);

        assertNotSame(value1, value2);
    }
}
