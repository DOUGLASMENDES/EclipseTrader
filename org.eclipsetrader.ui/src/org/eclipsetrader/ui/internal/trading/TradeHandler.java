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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IPosition;
import org.eclipsetrader.core.trading.ITradingService;
import org.eclipsetrader.ui.internal.UIActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradeHandler extends AbstractHandler {

    public static final String PARAM_BROKER = "broker"; //$NON-NLS-1$
    public static final String PARAM_LIMIT_PRICE = "limitPrice"; //$NON-NLS-1$
    public static final String PARAM_SIDE = "side"; //$NON-NLS-1$

    BundleContext context;
    ServiceReference serviceReference;
    ITradingService tradingService;

    public TradeHandler() {
        context = UIActivator.getDefault().getBundle().getBundleContext();
        serviceReference = context.getServiceReference(ITradingService.class.getName());
        tradingService = (ITradingService) context.getService(serviceReference);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#dispose()
     */
    @Override
    public void dispose() {
        context.ungetService(serviceReference);
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        IWorkbenchSite site = HandlerUtil.getActiveSite(event);

        if (selection != null && !selection.isEmpty()) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                IAdaptable target = (IAdaptable) iter.next();
                openDialog(event, site, target);
            }
        }

        return null;
    }

    private void openDialog(ExecutionEvent event, IWorkbenchSite site, IAdaptable target) {
        OrderDialog dlg = new OrderDialog(site.getShell(), tradingService);

        ISecurity security = (ISecurity) target.getAdapter(ISecurity.class);
        dlg.setSecurity(security);

        String brokerId = event.getParameter(PARAM_BROKER);
        if (brokerId != null) {
            dlg.setBroker(tradingService.getBroker(brokerId));
        }
        else {
            IBroker broker = (IBroker) target.getAdapter(IBroker.class);
            if (broker == null) {
                broker = tradingService.getBrokerForSecurity(security);
            }
            dlg.setBroker(broker);

            IAccount account = (IAccount) target.getAdapter(IAccount.class);
            dlg.setAccount(account);
        }

        IPosition position = (IPosition) target.getAdapter(IPosition.class);
        if (position != null) {
            dlg.setPosition(position.getQuantity());
        }

        String limitPrice = event.getParameter(PARAM_LIMIT_PRICE);
        if (limitPrice != null) {
            dlg.setLimitPrice(Double.parseDouble(limitPrice));
        }
        else {
            ITrade trade = (ITrade) target.getAdapter(ITrade.class);
            if (trade != null && trade.getPrice() != null) {
                dlg.setLimitPrice(trade.getPrice());
            }
        }

        String side = event.getParameter(PARAM_SIDE);
        if (side != null) {
            if (IOrderSide.Buy.getId().equals(side)) {
                dlg.setOrderSide(IOrderSide.Buy);
            }
            else if (IOrderSide.Sell.getId().equals(side)) {
                dlg.setOrderSide(IOrderSide.Sell);
            }
            else if (IOrderSide.BuyCover.getId().equals(side)) {
                dlg.setOrderSide(IOrderSide.BuyCover);
            }
            else if (IOrderSide.SellShort.getId().equals(side)) {
                dlg.setOrderSide(IOrderSide.SellShort);
            }
        }
        else {
            if (position != null) {
                dlg.setOrderSide(position.getQuantity() > 0 ? IOrderSide.Sell : IOrderSide.Buy);
            }
        }

        dlg.open();
    }
}
