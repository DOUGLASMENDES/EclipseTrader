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

package org.eclipsetrader.ui.internal.trading;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.internal.trading.OrderDialog;

public class OrderDialogTest extends TestCase {

    Shell shell;
    Security security;
    IAccount account;
    IBroker broker1;
    IBroker broker2;
    ITradingService tradingService;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        shell = new Shell(Display.getDefault());

        security = new Security("TEST", new FeedIdentifier("TST", null));

        account = EasyMock.createNiceMock(IAccount.class);
        EasyMock.expect(account.getDescription()).andStubReturn("Demo");

        broker1 = EasyMock.createNiceMock(IBroker.class);
        EasyMock.expect(broker1.getName()).andStubReturn("Test Broker 1");
        EasyMock.expect(broker1.getAllowedSides()).andStubReturn(new IOrderSide[] {
                IOrderSide.Buy,
                IOrderSide.Sell,
                IOrderSide.BuyCover,
                IOrderSide.SellShort
        });
        EasyMock.expect(broker1.getAllowedTypes()).andStubReturn(new IOrderType[] {
                IOrderType.Limit, IOrderType.Market
        });
        EasyMock.expect(broker1.getAllowedValidity()).andStubReturn(new IOrderValidity[] {
            IOrderValidity.Day
        });
        EasyMock.expect(broker1.getAllowedRoutes()).andStubReturn(new IOrderRoute[0]);
        EasyMock.expect(broker1.getAccounts()).andStubReturn(new IAccount[] {
            account
        });
        EasyMock.expect(broker1.getSymbolFromSecurity(security)).andStubReturn(security.getIdentifier().getSymbol());

        broker2 = EasyMock.createNiceMock(IBroker.class);
        EasyMock.expect(broker2.getName()).andStubReturn("Test Broker 2");
        EasyMock.expect(broker2.getAllowedSides()).andStubReturn(new IOrderSide[] {
                IOrderSide.Buy, IOrderSide.Sell,
        });
        EasyMock.expect(broker2.getAllowedTypes()).andStubReturn(new IOrderType[] {
                IOrderType.Limit, IOrderType.Market
        });
        EasyMock.expect(broker2.getAllowedValidity()).andStubReturn(new IOrderValidity[] {
            IOrderValidity.Day
        });
        EasyMock.expect(broker2.getAllowedRoutes()).andStubReturn(new IOrderRoute[0]);
        EasyMock.expect(broker2.getAccounts()).andStubReturn(new IAccount[] {
            account
        });
        EasyMock.expect(broker2.getSymbolFromSecurity(security)).andStubReturn(security.getIdentifier().getSymbol());

        tradingService = EasyMock.createNiceMock(ITradingService.class);
        EasyMock.expect(tradingService.getBrokers()).andStubReturn(new IBroker[] {
                broker1, broker2
        });

        EasyMock.replay(tradingService, broker1, broker2, account);
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

    public void testSetInitialQuantity() throws Exception {
        OrderDialog dlg = new OrderDialog(shell, tradingService);
        dlg.setPosition(200L);
        dlg.create();
        assertEquals(dlg.numberFormat.format(200), dlg.quantity.getText());
    }

    public void testSetSecuritySymbol() throws Exception {
        OrderDialog dlg = new OrderDialog(shell, tradingService);
        dlg.setSecurity(new Security("TEST", new FeedIdentifier("TST", null)));
        dlg.create();
        assertEquals("TST", dlg.symbol.getText());
    }

    public void testKeepSideWhenSwitchingBroker() throws Exception {
        OrderDialog dlg = new OrderDialog(shell, tradingService);
        dlg.setOrderSide(IOrderSide.Sell);

        dlg.create();
        assertSame(IOrderSide.Sell, ((IStructuredSelection) dlg.sideCombo.getSelection()).getFirstElement());
        assertSame(broker1, ((IStructuredSelection) dlg.brokerCombo.getSelection()).getFirstElement());

        dlg.brokerCombo.setSelection(new StructuredSelection(broker2));

        assertSame(IOrderSide.Sell, ((IStructuredSelection) dlg.sideCombo.getSelection()).getFirstElement());
        assertSame(broker2, ((IStructuredSelection) dlg.brokerCombo.getSelection()).getFirstElement());
    }
}
