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

package org.eclipsetrader.ui.internal.navigator;

import java.util.Currency;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipsetrader.core.instruments.IStock;
import org.eclipsetrader.core.instruments.Stock;
import org.eclipsetrader.core.views.IViewItem;
import org.eclipsetrader.ui.navigator.INavigatorContentGroup;

public class NavigatorViewTest extends TestCase {

	public void testGetGroupedElements() throws Exception {
		IStock stock1 = new Stock("Stock 1", null, Currency.getInstance("USD"));
		IStock stock2 = new Stock("Stock 2", null, Currency.getInstance("USD"));

		IStructuredContentProvider contentProvider = EasyMock.createNiceMock(IStructuredContentProvider.class);
		EasyMock.expect(contentProvider.getElements(EasyMock.anyObject())).andStubReturn(new Object[] {
		    stock1, stock2
		});
		EasyMock.replay(contentProvider);

		NavigatorView view = new NavigatorView();
		view.setContentProviders(new IStructuredContentProvider[] {
			contentProvider
		});
		view.setGroups(new INavigatorContentGroup[] {
			new InstrumentTypeGroup(),
		});
		view.update();

		IViewItem[] rootItems = view.getItems();
		assertEquals(1, rootItems.length);
		assertEquals("Stocks", rootItems[0].toString());

		IViewItem[] childItems = rootItems[0].getItems();
		assertEquals(2, childItems.length);
	}

	public void testUpdateAddedElements() throws Exception {
		IStock stock1 = new Stock("Stock 1", null, Currency.getInstance("USD"));
		IStock stock2 = new Stock("Stock 2", null, Currency.getInstance("USD"));

		NavigatorView view = new NavigatorView();
		view.setGroups(new INavigatorContentGroup[] {
			new InstrumentTypeGroup(),
		});

		IStructuredContentProvider contentProvider = EasyMock.createNiceMock(IStructuredContentProvider.class);
		EasyMock.expect(contentProvider.getElements(EasyMock.anyObject())).andStubReturn(new Object[] {
			stock1
		});
		EasyMock.replay(contentProvider);
		view.setContentProviders(new IStructuredContentProvider[] {
			contentProvider
		});
		view.update();

		contentProvider = EasyMock.createNiceMock(IStructuredContentProvider.class);
		EasyMock.expect(contentProvider.getElements(EasyMock.anyObject())).andStubReturn(new Object[] {
		    stock1, stock2
		});
		EasyMock.replay(contentProvider);
		view.setContentProviders(new IStructuredContentProvider[] {
			contentProvider
		});
		view.update();

		IViewItem[] rootItems = view.getItems();
		assertEquals(1, rootItems.length);
		assertEquals("Stocks", rootItems[0].toString());

		IViewItem[] childItems = rootItems[0].getItems();
		assertEquals(2, childItems.length);
	}

	public void testUpdateRemovedElements() throws Exception {
		IStock stock1 = new Stock("Stock 1", null, Currency.getInstance("USD"));
		IStock stock2 = new Stock("Stock 2", null, Currency.getInstance("USD"));

		NavigatorView view = new NavigatorView();
		view.setGroups(new INavigatorContentGroup[] {
			new InstrumentTypeGroup(),
		});

		IStructuredContentProvider contentProvider = EasyMock.createNiceMock(IStructuredContentProvider.class);
		EasyMock.expect(contentProvider.getElements(EasyMock.anyObject())).andStubReturn(new Object[] {
		    stock1, stock2
		});
		EasyMock.replay(contentProvider);
		view.setContentProviders(new IStructuredContentProvider[] {
			contentProvider
		});
		view.update();

		contentProvider = EasyMock.createNiceMock(IStructuredContentProvider.class);
		EasyMock.expect(contentProvider.getElements(EasyMock.anyObject())).andStubReturn(new Object[] {
			stock1
		});
		EasyMock.replay(contentProvider);
		view.setContentProviders(new IStructuredContentProvider[] {
			contentProvider
		});
		view.update();

		IViewItem[] rootItems = view.getItems();
		assertEquals(1, rootItems.length);
		assertEquals("Stocks", rootItems[0].toString());

		IViewItem[] childItems = rootItems[0].getItems();
		assertEquals(1, childItems.length);
	}
}
