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

package org.eclipsetrader.internal.ui.trading;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.ITradingService;

public class OrderDialogTest extends TestCase {
	Shell shell;
	IAccount account;
	ITradingService tradingService;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		shell = new Shell(Display.getDefault());

		account = EasyMock.createNiceMock(IAccount.class);
		EasyMock.expect(account.getDescription()).andStubReturn("Demo");

		IBroker broker = EasyMock.createNiceMock(IBroker.class);
		EasyMock.expect(broker.getName()).andStubReturn("Test Broker");
		EasyMock.expect(broker.getAllowedSides()).andStubReturn(new IOrderSide[] {
		    IOrderSide.Buy,
		    IOrderSide.Sell,
		    IOrderSide.BuyCover,
		    IOrderSide.SellShort
		});
		EasyMock.expect(broker.getAllowedTypes()).andStubReturn(new IOrderType[] {
		    IOrderType.Limit, IOrderType.Market
		});
		EasyMock.expect(broker.getAllowedValidity()).andStubReturn(new IOrderValidity[] {
			IOrderValidity.Day
		});
		EasyMock.expect(broker.getAllowedRoutes()).andStubReturn(new IOrderRoute[0]);
		EasyMock.expect(broker.getAccounts()).andStubReturn(new IAccount[] {
			account
		});

		tradingService = EasyMock.createNiceMock(ITradingService.class);
		EasyMock.expect(tradingService.getBrokers()).andStubReturn(new IBroker[] {
			broker
		});

		EasyMock.replay(tradingService, broker, account);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
	}

	public void testSetDefaultOrderSideToFirstElement() throws Exception {
		OrderDialog dlg = new OrderDialog(shell, tradingService);
		dlg.create();
		assertSame(IOrderSide.Buy, ((IStructuredSelection) dlg.sideCombo.getSelection()).getFirstElement());
	}

	public void testSetInitialOrderSide() throws Exception {
		OrderDialog dlg = new OrderDialog(shell, tradingService);
		dlg.setOrderSide(IOrderSide.Sell);
		dlg.create();
		assertSame(IOrderSide.Sell, ((IStructuredSelection) dlg.sideCombo.getSelection()).getFirstElement());
	}

	public void testSetInitialLimitPrice() throws Exception {
		OrderDialog dlg = new OrderDialog(shell, tradingService);
		dlg.setLimitPrice(1.35);
		dlg.create();
		assertEquals(dlg.priceFormat.format(1.35), dlg.price.getText());
	}
}
