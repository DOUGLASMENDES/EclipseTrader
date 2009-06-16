/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.markets;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipsetrader.core.internal.markets.Market;
import org.eclipsetrader.core.internal.markets.MarketService;
import org.eclipsetrader.core.internal.markets.MarketTime;

public class MarketsViewTest extends TestCase {
	Shell shell;
	IWorkbenchPartSite site;
	MarketService service;
	Market market;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getDefault());

		ISelectionProvider selectionProvider = EasyMock.createNiceMock(ISelectionProvider.class);
		site = EasyMock.createNiceMock(IWorkbenchPartSite.class);
		EasyMock.expect(site.getSelectionProvider()).andStubReturn(selectionProvider);
		site.registerContextMenu(EasyMock.isA(MenuManager.class), EasyMock.isA(ISelectionProvider.class));
		EasyMock.replay(site);

		service = new MarketService();
		service.addMarket(market = new Market("New York", new ArrayList<MarketTime>()));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		while (Display.getDefault().readAndDispatch());
	}

	public void testShowExistingMarkets() throws Exception {
		MarketsView view = new MyMarketsView();
		view.createPartControl(shell);
		assertEquals(1, view.viewer.getTable().getItemCount());
	}

	public void testAddNewMarkets() throws Exception {
		MarketsView view = new MyMarketsView();
		view.createPartControl(shell);

		service.addMarket(new Market("Milan", new ArrayList<MarketTime>()));
		while (Display.getDefault().readAndDispatch());

		assertEquals(2, view.viewer.getTable().getItemCount());
	}

	public void testRemoveDeletedMarkets() throws Exception {
		MarketsView view = new MyMarketsView();
		view.createPartControl(shell);

		service.deleteMarket(market);
		while (Display.getDefault().readAndDispatch());

		assertEquals(0, view.viewer.getTable().getItemCount());
	}

	public void testAddServiceObserverOnCreate() throws Exception {
		MarketsView view = new MyMarketsView();
		view.createPartControl(shell);
		assertEquals(1, service.countObservers());
	}

	public void testRemoveServiceObserverOnDispose() throws Exception {
		MarketsView view = new MyMarketsView();
		view.createPartControl(shell);

		view.dispose();

		assertEquals(0, service.countObservers());
	}

	class MyMarketsView extends MarketsView {

		public MyMarketsView() {
			marketService = MarketsViewTest.this.service;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.part.WorkbenchPart#getSite()
		 */
		@Override
		public IWorkbenchPartSite getSite() {
			return site;
		}
	}
}
