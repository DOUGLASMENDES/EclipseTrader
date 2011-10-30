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
import org.eclipse.swt.graphics.Image;
import org.eclipsetrader.core.feed.Book;
import org.eclipsetrader.core.feed.BookEntry;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.ui.internal.providers.PressureBarFactory.ImageValue;

public class PressureBarFactoryTest extends TestCase {

    private Security security = new Security("Test", null);
    private IBook book;
    private IAdaptable sourceAdaptable = new IAdaptable() {

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

    public void testGetValueWithNullBook() throws Exception {
        IDataProvider provider = new PressureBarFactory().createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        assertNull(value);
    }

    public void testGetBookPressure() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        assertNotNull(value);
        assertNotNull(value.getAdapter(Image.class));
    }

    public void testGetBidOnlyBookPressure() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[0]);
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        assertNotNull(value);
        assertNotNull(value.getAdapter(Image.class));
    }

    public void testGetAskOnlyBookPressure() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        book = new Book(new BookEntry[0], new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        assertNotNull(value);
        assertNotNull(value.getAdapter(Image.class));
    }

    public void testGetSameValueInstance() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        Book book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });
        factory.buildValue(book);
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        assertSame(value, provider.getValue(sourceAdaptable));
    }

    public void testGetNewValueInstanceWithBookUpdate() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 75L, null, null),
        });
        assertNotSame(value, provider.getValue(sourceAdaptable));
    }

    public void testDontDisposeOldImageWithBookUpdate() throws Exception {
        PressureBarFactory factory = new PressureBarFactory();
        book = new Book(new BookEntry[] {
            new BookEntry(null, 10.0, 100L, null, null),
        }, new BookEntry[] {
            new BookEntry(null, 10.0, 50L, null, null),
        });
        IDataProvider provider = factory.createProvider();
        IAdaptable value = provider.getValue(sourceAdaptable);
        provider.getValue(sourceAdaptable);
        assertFalse(((ImageValue) value).isDisposed());
    }
}
